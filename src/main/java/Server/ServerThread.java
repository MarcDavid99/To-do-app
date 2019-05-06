package Server;

import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import shared.*;

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

            // Enne töötamist võetakse sctx-st värske allUsers list, mida
            // värskendavad Server.ServerThread ise ja Server.DeadlineThread
            synchronized (sctx) {
                allUsers = sctx.getAllUsers();
            }

            boolean closeProgramme;
            while (true) {
                System.out.println("DEBUG: Server.ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out, sctx, allUsers, currentUser);

                if (closeProgramme) {
                    // Värskendatakse faili sisu
                    sctx.writeExistingUsersToFile();
                    System.out.println("DEBUG: Server.ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {

        int requestType = socketIn.readInt();
        if (requestType == Commands.DO_SAVE_NEW_USER.getValue()) {
            saveNewUser(socketIn);
        }
        if (requestType == Commands.DO_VERIFY_CLIENT.getValue()) {
            verifyClient(socketIn, socketOut);
        }
        if (requestType == Commands.DO_CHECK_FOR_USERNAME.getValue()) {

            boolean checkUsername = ServerThreadTaskCommands.checkForUsernameInList(socketIn.readUTF(), sctx, allUsers);
            socketOut.writeBoolean(checkUsername);
        }
        if (requestType == Commands.DO_ADD_TASK.getValue()) {
            ServerThreadTaskCommands.addTask(socketIn, socketOut, currentUser);
        }
        if (requestType == Commands.DO_DISPLAY_TASK.getValue()) {
            ServerThreadTaskCommands.displayTasks(socketOut, currentUser, sctx, allUsers);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_CERTAIN.getValue()) {
            ServerThreadTaskCommands.displayCertainUserTasks(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue()){
            ServerThreadTaskCommands.displayTaskByTopic(socketIn,socketOut, currentUser, sctx, allUsers);
        }
        if (requestType == Commands.DO_ADD_COMMENT.getValue()) {
            ServerThreadTaskCommands.addComment(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_PUSH_DEADLINE.getValue()) {
            ServerThreadTaskCommands.pushDeadline(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_COMPLETE_TASK.getValue()) {
            ServerThreadTaskCommands.completeTask(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            ServerThreadTaskCommands.addTaskToOtherUser(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()) {
            ServerThreadTaskCommands.searchTaskByDescription(socketIn, socketOut, sctx, allUsers);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()) {
            ServerThreadTaskCommands.searchTaskByUsername(socketIn, socketOut, sctx, allUsers);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()) {
            ServerThreadTaskCommands.searchTaskByDeadline(socketIn, socketOut, sctx, allUsers);
        }

        if (requestType == Commands.DO_FOLLOW_TASK.getValue()) {
            ServerThreadTaskCommands.followTask(socketIn, socketOut, sctx, allUsers, currentUser);
        }
        if (requestType == Commands.DO_CLOSE_TODO_LIST_1.getValue() || requestType == Commands.DO_CLOSE_TODO_LIST_2.getValue()) {
            return ServerThreadTaskCommands.closeTodoList(socketIn, socketOut);
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
        boolean usernameExists = false;

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    usernameExists = true;
                    currentUser = user;
                }
            }
        }

        if (usernameExists) { // Kontrollib, kas sisse logides sisestatud pass on sama mis failis olev password.
            if (argon2.verify(currentUser.getPassword(), password)) {
                socketOut.writeInt(Commands.DO_CONFIRM_LOGIN.getValue()); // kui sisselogimine õnnestub
                socketOut.writeUTF("Olete sisselogitud.");
            }
            else {
                socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // kui sisselogimine ei õnnestu
                socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti." + "\r\n");
            }
        }
        else  {
            socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // sisselogimine ei õnnestunud
            socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti." + "\r\n");
        }
    }
}