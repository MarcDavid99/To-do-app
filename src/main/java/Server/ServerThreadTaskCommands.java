package Server;

import com.google.gson.Gson;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import shared.*;

public class ServerThreadTaskCommands {

    public static void deleteUser(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        Argon2 argon2 = Argon2Factory.create();

        boolean usernameExists = false;
        boolean passwordMatches = false;
        boolean deletingYourself = false;
        User userToDelete = null;
        synchronized (sctx) {
            userToDelete = allUsers.get(0);
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    if (user.equals(currentUser)) {
                        deletingYourself = true;
                        break;
                    }
                    else {
                        usernameExists = true;
                        userToDelete = user;
                        break;
                    }
                }
            }
        }

        if (argon2.verify(userToDelete.getPassword(), password)) {
            passwordMatches = true;
        }

        boolean userAlreadyDeleted = false;
        if (usernameExists && passwordMatches && !deletingYourself) { // kasutajanimi ja password klapivad ja ei kustutata iseennast
            synchronized (sctx) {
                if (allUsers.contains(userToDelete)) { // kasutaja on veel kasutajate listis
                    allUsers.remove(userToDelete);
                    sctx.writeExistingUsersToFile();
                }
                else { // kasutaja on juba kustutatud
                    userAlreadyDeleted = true;
                }
            }
            if (userAlreadyDeleted) { // kasutaja on juba kustutatud
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Kasutaja on juba kustutatud!");
            }
            else { // kasutaja kustutati ära
                socketOut.writeInt(Commands.DO_DELETE_USER.getValue());
                socketOut.writeUTF("Kasutaja on edukalt kustutatud!");
            }
        }
        else { // kasutajanimi või password oli vale või üritati ennast kustutada
            if (deletingYourself) {
                socketOut.writeInt(Commands.DO_CLOSE_TODO_LIST_1.getValue()); // Main ei oska sellega tegelikult midagi peale hakata, aga tuleb saata ikka
                socketOut.writeUTF("Kustutasite oma kasutaja, teie sessioon katkeb sulgub!");
            }
            else { // ei üritatud ennast kustutada
                if (usernameExists) { // kasutajanimi eksisteeris
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Password on ebakorrektne!");
                }
                else { // kõik antud info oli vale
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Sellist kasutajat ei leidu!");
                }

            }
        }
        if (deletingYourself) {
            synchronized (sctx) {
                allUsers.remove(userToDelete);
                sctx.writeExistingUsersToFile();
            }
            socketOut.writeBoolean(true); // Sulge to do list
        }
        else {
            socketOut.writeBoolean(false); // Ära sulge
        }
    }

    public static boolean checkForUsernameInList(String username, ServerContext sctx, List<User> allUsers) throws IOException {
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

    public static void followTask(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        int taskIndex = socketIn.readInt();
        boolean tryToFollowTask = false;
        User userWhoseTaskToFollow = null;

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    userWhoseTaskToFollow = user;
                    tryToFollowTask = true;
                }
            }
        }
        if (!tryToFollowTask) { // kasutajanime ei eksisteeri
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellist kasutajanime pole olemas.");
            socketOut.writeBoolean(false);
        } else {  // kasutajanimi eksisteerib
            if (userWhoseTaskToFollow.getToDoList().size() >= taskIndex) {
                if (userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).getTaskFollowers().contains(currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Seda ülesannet sa juba jälgid.");
                    socketOut.writeBoolean(false);
                } else if (userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).isPrivateTask() && !userWhoseTaskToFollow.getUserID().equals(currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Ülesande jälgimine pole võimalik, sest see on privaatne.");
                    socketOut.writeBoolean(false);
                } else {
                    userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).addFollower(currentUser.getUserID());
                    socketOut.writeInt(Commands.DO_FOLLOW_TASK.getValue());
                    socketOut.writeUTF("Ülesande jälgimine toimis.");
                    socketOut.writeBoolean(false);
                }
            } else {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Sellise indeksiga ülesannet ei eksisteeri.");
                socketOut.writeBoolean(false);
            }
        }

    }

    public static boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    public static void addComment(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
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

    public static void pushDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
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

    public static void addTaskToOtherUser(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
        String username = socketIn.readUTF();
        String description = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        if (checkForUsernameInList(username, sctx, allUsers)) {
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

    public static void addTask(DataInputStream socketIn, DataOutputStream socketOut, User currentUser) throws IOException {
        String taskDescription = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        String taskID = UUID.randomUUID().toString();
        currentUser.addTask(new Task(taskDescription, taskID, currentUser.getUserID(), currentUser.getUserID(), isPrivateTask, topic));
        socketOut.writeInt(Commands.DO_ADD_TASK.getValue());
        socketOut.writeUTF("Ülesanne loodud.");
        socketOut.writeBoolean(false);
    }

    public static void displayTasks(DataOutputStream socketOut, User currentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoList, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void displayTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut, User currentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();
        for (Task task : currentUser.getToDoList()) {
            if (task.getTaskTopic().equals(topic)) {
                todoListByTopic.add(task);
            }
        }
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoListByTopic, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void displayCertainUserTasks(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    todoList = user.getToDoList();
                }
            }
        }
        if (!checkForUsernameInList(username, sctx, allUsers)) {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellise kasutajanimega kasutajat ei leidu.");
        } else {
            if (username.equals(currentUser.getUsername())) {
                socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                sendTasks(todoList, socketOut, true, sctx, allUsers);
                socketOut.writeBoolean(false);
            } else {
                socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                sendTasks(todoList, socketOut, false, sctx, allUsers);
                socketOut.writeBoolean(false);
            }
        }
    }


    public static void completeTask(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
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

    public static void searchTaskByDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String deadline = socketIn.readUTF();
        List<Task> suitableTasksArray = new ArrayList<>();

        synchronized (sctx) {
            for (User user : allUsers) {
                List<Task> todoList = user.getToDoList();
                if (user.equals(currentUser)) { //kui tegu on kasutaja endaga, siis tema private taske võib kuvada
                    for (Task task : todoList) {
                        if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline)) {
                            suitableTasksArray.add(task);
                        }
                    }
                } else {
                    for (Task task : todoList) {

                        if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline) && !task.isPrivateTask()) { //võõra private taske ei tohiks kuvada
                            suitableTasksArray.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasksArray, socketOut, true, sctx, allUsers);

        socketOut.writeBoolean(false);
    }

    public static void searchTaskByUsername(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();
        if (checkForUsernameInList(username, sctx, allUsers)) {
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        todoList.addAll(user.getToDoList());
                        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
                        if (user.equals(currentUser)) {
                            sendTasks(todoList, socketOut, true, sctx, allUsers);
                            break;
                        } else {
                            sendTasks(todoList, socketOut, false, sctx, allUsers);
                            break;
                        }
                    }
                }
            }
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByDescription(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        List<Task> suitableTasks = new ArrayList<>();
        String description = socketIn.readUTF();

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.equals(currentUser)) {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskDescription().contains(description)) {
                            suitableTasks.add(task);
                        }
                    }
                } else {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskDescription().contains(description) && !task.isPrivateTask()) {
                            suitableTasks.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasks, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut, User currentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();
        synchronized (sctx) {
            for (User user : allUsers) {
                if (currentUser.equals(user)) {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskTopic().equals(topic)) {
                            todoListByTopic.add(task);
                        }
                    }
                } else {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskTopic().equals(topic) && !task.isPrivateTask()) {
                            todoListByTopic.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(todoListByTopic, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void sendTasks(List<Task> list, DataOutputStream socketOut, boolean justShowToCurrentUser, ServerContext sctx, List<User> allUsers) throws IOException {
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
