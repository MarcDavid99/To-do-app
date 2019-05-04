import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerThread implements Runnable {

    private final Socket socket;
    private final ServerContext sctx;
    private User currentUser;
    private Argon2 argon2 = Argon2Factory.create();

    private List<User> allUsers = new ArrayList<>();

    public ServerThread(Socket socket, ServerContext sctx) {

        this.socket = socket;
        this.sctx = sctx;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("DEBUG: Uue kliendi jaoks luuakse uus thread");

            // TODO: Synchronized plokid selleks, et andmete listi erinevates kohtades
            // TODO: muteerimisel midagi kaotsi ei läheks

            // Enne töötamist võetakse sctx-st värske allUsers list, mida
            // värskendavad ServerThread ise ja DeadlineThread

            synchronized (sctx) {
                allUsers = sctx.getAllUsers();
            }

            boolean closeProgramme;
            while (true) {
                System.out.println("DEBUG: ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out);

                if (closeProgramme) {

                    // Värskendatakse faili sisu
                    synchronized (sctx) {
                        sctx.writeExistingUsersToFile();
                    }

                    System.out.println("DEBUG: ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {

        int requestType = socketIn.readInt();
        if (requestType == Commands.DO_SAVE_NEW_USER.getValue()) {
            saveNewUser(socketIn);
        }
        if (requestType == Commands.DO_VERIFY_CLIENT.getValue()) {
            verifyClient(socketIn, socketOut);
        }
        if (requestType == Commands.DO_CHECK_FOR_USERNAME.getValue()) {

            boolean checkUsername = checkForUsernameInList(socketIn.readUTF());
            socketOut.writeBoolean(checkUsername);
        }
        if (requestType == Commands.DO_ADD_TASK.getValue()) {
            addTask(socketIn, socketOut);
        }
        if (requestType == Commands.DO_DISPLAY_TASK.getValue()) {
            displayTasks(socketOut);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_CERTAIN.getValue()) {
            displayCertainUserTasks(socketIn, socketOut);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue()){
            displayTaskByTopic(socketIn,socketOut);
        }
        if (requestType == Commands.DO_ADD_COMMENT.getValue()) {
            addComment(socketIn, socketOut);
        }
        if (requestType == Commands.DO_PUSH_DEADLINE.getValue()) {
            pushDeadline(socketIn, socketOut);
        }
        if (requestType == Commands.DO_COMPLETE_TASK.getValue()) {
            completeTask(socketIn, socketOut);
        }
        if (requestType == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            addTaskToOtherUser(socketIn, socketOut);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()) {
            searchTaskByDescription(socketIn, socketOut);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()) {
            searchTaskByUsername(socketIn, socketOut);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()) {
            searchTaskByDeadline(socketIn, socketOut);
        }

        if (requestType == Commands.DO_FOLLOW_TASK.getValue()) {
            followTask(socketIn, socketOut);
        }
        if (requestType == Commands.DO_CLOSE_TODO_LIST_1.getValue() || requestType == Commands.DO_CLOSE_TODO_LIST_2.getValue()) {
            return closeTodoList(socketIn, socketOut);
        }
        return false;
    }

    private void saveNewUser(DataInputStream socketIn) throws IOException {
        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        synchronized (sctx) {
            allUsers.add(newUser);
        }
    }

    private void verifyClient(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        boolean responseSent = false;

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    if (argon2.verify(user.getPassword(), password)) { // Kontrollib, kas sisse logides sisestatud pass on sama mis failis olev password.
                        currentUser = user;
                        socketOut.writeInt(Commands.DO_CONFIRM_LOGIN.getValue()); // kui sisselogimine õnnestub
                        socketOut.writeUTF("Olete sisselogitud.");
                        responseSent = true;
                    } else {
                        socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // kui sisselogimine ei õnnestu
                        socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti.");
                        responseSent = true;
                    }
                }
            }
            if (!responseSent) {
                socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // sisselogimine ei õnnestunud
                socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
            }
        }
    }


    private boolean checkForUsernameInList(String username) throws IOException {
        boolean usernameAlreadyExists = false;
        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    usernameAlreadyExists = true;
                }
            }
        }
        return usernameAlreadyExists;
    }


    // TODO: Interface Commandide jaoks

    private void followTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        int taskIndex = Integer.parseInt(socketIn.readUTF());

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    try {
                        if (user.getToDoList().contains(user.getToDoList().get(taskIndex - 1))) {//taskIndex - 1 sest kasutaja saadab inimkeeles mitmenda taskiga tegemist on.
                            if (user.getToDoList().get(taskIndex - 1).getTaskFollowers().contains(currentUser.getUserID())) {
                                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                                socketOut.writeUTF("Seda ülesannet sa juba jälgid.");
                                socketOut.writeBoolean(false);
                            } else {
                                user.getToDoList().get(taskIndex - 1).addFollower(currentUser.getUserID());
                            }
                            socketOut.writeInt(Commands.DO_FOLLOW_TASK.getValue());
                            socketOut.writeUTF("Ülesande jälgimine toimis.");
                            socketOut.writeBoolean(false);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                        socketOut.writeUTF("Sellise indeksiga ülesannet ei eksisteeri.");
                        socketOut.writeBoolean(false);
                    }
                }
            }
        }
        if (!checkForUsernameInList(username)) {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellist kasutajanime pole olemas.");
            socketOut.writeBoolean(false);
        }

    }

    private boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    private void addComment(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            String comment = socketIn.readUTF();
            synchronized (sctx) {
                todoList.get(indeks).addComments(comment, allUsers);
            }
            socketOut.writeInt(Commands.DO_ADD_COMMENT.getValue());
            socketOut.writeUTF("Kommentaar lisatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }

        socketOut.writeBoolean(false);
    }

    private void pushDeadline(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            int pushDeadline = socketIn.readInt();
            synchronized (sctx) {
                todoList.get(indeks).setDeadline(pushDeadline, allUsers);
            }
            socketOut.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
            socketOut.writeUTF("Tähtaeg edasi lükatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    private void addTaskToOtherUser(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        String username = socketIn.readUTF();
        String description = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        if (checkForUsernameInList(username)) {
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        String taskID = UUID.randomUUID().toString();
                        user.addTask(new Task(description, taskID, currentUser.getUserID(), user.getUserID(), isPrivateTask, topic));
                    }
                }
            }
            socketOut.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
            socketOut.writeUTF("Kasutajale " + username + " on lisatud ülesanne kirjeldusega " + description);
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);

    }

    private void addTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String taskDescription = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        String taskID = UUID.randomUUID().toString();
        currentUser.addTask(new Task(taskDescription, taskID, currentUser.getUserID(), currentUser.getUserID(), isPrivateTask, topic));
        socketOut.writeInt(Commands.DO_ADD_TASK.getValue());
        socketOut.writeUTF("Ülesanne loodud.");
        socketOut.writeBoolean(false);
    }

    private void displayTasks(DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoList, socketOut, true);
        socketOut.writeBoolean(false);
    }

    private void displayTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();
        for (Task task : currentUser.getToDoList()) {
            if(task.getTaskTopic().equals(topic)){
                todoListByTopic.add(task);
            }
        }
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoListByTopic, socketOut, true);
        socketOut.writeBoolean(false);
    }

    private void displayCertainUserTasks(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();
        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    todoList = user.getToDoList();
                }
            }
        }
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoList, socketOut, true);
        socketOut.writeBoolean(false);
    }


    private void completeTask(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        List<Task> todoList = currentUser.getToDoList();
        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            synchronized (sctx) {
                todoList.get(indeks).setTaskFinished(allUsers);
            }
            todoList.remove(indeks);
            socketOut.writeInt(Commands.DO_COMPLETE_TASK.getValue());
            socketOut.writeUTF("Ülesanne edukalt eemaldatud");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    private void searchTaskByDeadline(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String deadline = socketIn.readUTF();
        List<Task> suitableTasksArray = new ArrayList<>();

        synchronized (sctx) {
            for (User user : allUsers) {
                List<Task> todoList = user.getToDoList();
                for (Task task : todoList) {
                    if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline) && !task.isPrivateTask()) {
                        suitableTasksArray.add(task);
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasksArray, socketOut, false);

        socketOut.writeBoolean(false);
    }

    private void searchTaskByUsername(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();
        if (checkForUsernameInList(username)) {
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        todoList.addAll(user.getToDoList());
                        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
                        sendTasks(todoList, socketOut, false);
                        break;
                    }
                }
            }
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);
    }

    private void searchTaskByDescription(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> suitableTasks = new ArrayList<>();
        String description = socketIn.readUTF();

        synchronized (sctx) {
            for (User user : allUsers) {
                for (Task task : user.getToDoList()) {
                    if (task.getTaskDescription().contains(description) && !task.isPrivateTask()) {
                        suitableTasks.add(task);
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasks, socketOut, false);
        socketOut.writeBoolean(false);
    }

    private void sendTasks(List<Task> list, DataOutputStream socketOut, boolean justShowToCurrentUser) throws IOException {
        socketOut.writeInt(list.size());
        socketOut.writeBoolean(justShowToCurrentUser);
        for (Task task : list) {
            String taskCreator = "";
            String taskUser = "";
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUserID().equals(task.getTaskCreatorID())) {
                        taskCreator = user.getUsername();
                    }
                    if (user.getUserID().equals(task.getTaskUserID())) {
                        taskUser = user.getUsername();
                    }
                }
            }
            Gson gson = new Gson();
            String jsonTask = gson.toJson(task);
            socketOut.writeUTF(jsonTask);
            socketOut.writeUTF(taskCreator);
            socketOut.writeUTF(taskUser);
        }
    }
}