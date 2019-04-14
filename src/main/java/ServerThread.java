import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
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

            readExistingUsersFromFile();

            boolean closeProgramme;
            while (true) {
                System.out.println("DEBUG: ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out);

                // Värskendab sctx-is olevat Userite listi
                sctx.setAllUsers(allUsers);

                if (closeProgramme) {

                    System.out.println(allUsers);
                    writeExistingUsersToFile();

                    System.out.println("DEBUG: ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readExistingUsersFromFile() throws IOException {

        if (new File("users.txt").exists() && new File("users.txt").length() > 0) {
            String jsonAllUsers;
            Path pathToFile = Path.of("users.txt");
            jsonAllUsers = Files.readString(pathToFile);
            Gson gson = new Gson();
            List<User> usersFromFile = gson.fromJson(jsonAllUsers, UserList.class);
            allUsers.addAll(usersFromFile);
        }
        else {
            try {
                Files.createFile(Path.of("users.txt"));
            }
            catch (Exception e) {
                System.out.println("File already exists");
            }
        }
    }

    private void writeExistingUsersToFile() throws IOException {

        Path pathToFile = Path.of("users.txt");
        Gson gson = new Gson();
        String jsonAllUsers = gson.toJson(allUsers);
        Files.writeString(pathToFile, jsonAllUsers);
    }

    private boolean detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {

        int requestType = socketIn.readInt();
        if (requestType == Commands.doSaveNewUser) {
            saveNewUser(socketIn);
        }
        if (requestType == Commands.doVerifyClient) {
            verifyClient(socketIn, socketOut);
        }
        if (requestType == Commands.doCheckForUsername) {

            boolean checkUsername = checkForUsernameInList(socketIn.readUTF());
            socketOut.writeBoolean(checkUsername);
        }
        if (requestType == Commands.doAddTask) {
            addTask(socketIn, socketOut);
        }
        if (requestType == Commands.doDisplayTasks) {
            displayTasks(socketOut);
        }
        if (requestType == Commands.doAddComment) {
            addComment(socketIn, socketOut);
        }
        if (requestType == Commands.doPushDeadline) {
            pushDeadline(socketIn, socketOut);
        }
        if (requestType == Commands.doCompleteTask) {
            completeTask(socketIn, socketOut);
        }
        if (requestType == Commands.doAddTaskToOtherUser) {
            addTaskToOtherUser(socketIn, socketOut);
        }
        if (requestType == Commands.doCloseTodoList1 || requestType == Commands.doCloseTodoList2) {
            return closeTodoList(socketIn, socketOut);
        }
        return false;
    }

    private void saveNewUser(DataInputStream socketIn) throws IOException {
        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        allUsers.add(newUser);
        writeExistingUsersToFile();
    }

    private void verifyClient(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        boolean responseSent = false;

        for (User user : allUsers) {
            if (user.getUsername().equals(username)) {
                if (argon2.verify(user.getPassword(), password)) { // Kontrollib, kas sisse logides sisestatud pass on sama mis failis olev password.
                    currentUser = user;
                    socketOut.writeInt(Commands.doConfirmLogin); // kui sisselogimine õnnestub
                    socketOut.writeUTF("Olete sisselogitud.");
                    responseSent = true;
                } else {
                    socketOut.writeInt(Commands.doNotConfirmLogin); // kui sisselogimine ei õnnestu
                    socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti.");
                    responseSent = true;
                }
            }
        }
        if (!responseSent) {
            socketOut.writeInt(Commands.doNotConfirmLogin); // sisselogimine ei õnnestunud
            socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
        }
    }

    //Vajab parandusi
    private String readHashedPasswordFromFile(String username) throws Exception {
        List<String> fileContent = Files.readAllLines(Path.of("users.txt"));
        if (fileContent.size() == 1) { //kui fail on tühi
            return argon2.hash(10, 65536, 1, allUsers.get(0).getPassword().toCharArray());
        }
        for (String user :
                fileContent) {
            if (user.contains(username)) {
                return user.split(";;")[5];
            }
        }
        return null;
    }

    private boolean checkForUsernameInList(String username) throws IOException {
        boolean usernameAlreadyExists = false;
        for (User user : allUsers) {
            if (user.getUsername().equals(username)) {
                usernameAlreadyExists = true;
            }
        }
        return usernameAlreadyExists;
    }


    // TODO: listiga seotud meetodid

    private boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    private void addComment(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt()-1;
        if(indeks >= 0 && indeks < todoList.size()){
            String comment = socketIn.readUTF();
            todoList.get(indeks).addComments(comment);
            socketOut.writeInt(Commands.doAddComment);
            socketOut.writeUTF("Kommentaar lisatud.");
            writeExistingUsersToFile();
        }
        else {
            socketOut.writeInt(Commands.errorOccured);
            socketOut.writeUTF("Sisestatud järjekorranumbriga taski sinu todo listis ei leidu.");
        }

        socketOut.writeBoolean(false);
    }

    private void pushDeadline(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt()-1;
        if(indeks >= 0 && indeks < todoList.size()){
            int pushDeadline = socketIn.readInt();
            todoList.get(indeks).setDeadline(pushDeadline);
            socketOut.writeInt(Commands.doPushDeadline);
            socketOut.writeUTF("Deadline edasi lükatud.");
            writeExistingUsersToFile();
        }
        else {
            socketOut.writeInt(Commands.errorOccured);
            socketOut.writeUTF("Sisestatud järjekorranumbriga taski sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    private void addTaskToOtherUser(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
            String username = socketIn.readUTF();
            String description = socketIn.readUTF();
            if (checkForUsernameInList(username)) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        String taskID = UUID.randomUUID().toString();
                        user.addTask(new Task(description, taskID, currentUser.getUserID(), user.getUserID()));
                    }
                }
                socketOut.writeInt(Commands.doAddTaskToOtherUser);
                socketOut.writeUTF("Kasutajale " + username + " on lisatud ülesanne kirjeldusega " + description);
                writeExistingUsersToFile();
            }
            else {
                socketOut.writeInt(Commands.errorOccured);
                socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
            }
        socketOut.writeBoolean(false);

    }

    private void addTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String taskDescription = socketIn.readUTF();
        // siia peaks mõtlema, kuidas unique task id teha, hetkel kõigil 0.
        String taskID = UUID.randomUUID().toString();
        currentUser.addTask(new Task(taskDescription, taskID, currentUser.getUserID(), currentUser.getUserID()));

        socketOut.writeInt(Commands.doAddTask);
        socketOut.writeUTF("Task loodud.");
        socketOut.writeBoolean(false);
        writeExistingUsersToFile();
    }

    private void displayTasks(DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.doDisplayTasks);
        int commentsAmount = 0;
        for (Task task : todoList) {
            commentsAmount += task.getComments().size();
        }
        socketOut.writeInt(todoList.size() * 2 + commentsAmount);
        int taskNumber = 1;
        for (Task task : todoList) {
            socketOut.writeUTF(taskNumber + ") " + task.getTaskDescription());
            if (task.getComments().size() == 1) {
                socketOut.writeUTF("   *Kommentaar: " + task.getComments().get(0));
            }
            else {
                int commentNumber = 1;
                for (String comment : task.getComments()) {
                    if (commentNumber == 1) {
                        socketOut.writeUTF("   *Kommentaarid:" + "\r\n" +
                                "      " + commentNumber + ". " + comment);
                    }
                    else {
                        socketOut.writeUTF("      " + commentNumber + ". " + comment);
                    }
                    commentNumber += 1;
                }
            }
            socketOut.writeUTF("   *Tähtaeg: " + task.getTaskDeadline().getDeadlineDate());
            taskNumber += 1;
        }

        socketOut.writeBoolean(false);
    }

    private void completeTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            todoList.get(indeks).setTaskFinished();
            todoList.remove(indeks);
            socketOut.writeInt(Commands.doCompleteTask);
            socketOut.writeUTF("Task edukalt eemaldatud");
            writeExistingUsersToFile();
        } else {
            socketOut.writeInt(Commands.errorOccured);
            socketOut.writeUTF("Sisestatud järjekorranumbriga taski sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }
}