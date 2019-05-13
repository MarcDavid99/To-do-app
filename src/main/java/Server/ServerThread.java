package Server;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import shared.*;

public class ServerThread implements Runnable {

    private final Socket socket;
    public final ServerContext sctx;
    public User currentUser;
    private Argon2 argon2 = Argon2Factory.create();
    private String verificationCode;

    public List<User> allUsers = new ArrayList<>();

    public ServerThread(Socket socket, ServerContext sctx) {
        this.socket = socket;
        this.sctx = sctx;
    }

    public void run() {
        try (socket;
             DataInputStream socketIn = new DataInputStream(socket.getInputStream());
             DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("DEBUG: Uue kliendi jaoks luuakse uus thread");

            // Enne töötamist võetakse sctx-st värske allUsers list, mida
            // värskendavad Server.ServerThread ise ja Server.DeadlineThread
            synchronized (sctx) {
                allUsers = sctx.getAllUsers();
            }
            boolean closeProgramme;
            while (true) {
                System.out.println("DEBUG: ServerThread teeb tööd");
                closeProgramme = detectClientRequest(socketIn, socketOut);

                if (closeProgramme) {
                    // Värskendatakse faili sisu
                    sctx.writeExistingUsersToFile();
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
            UserMethodsServer.saveNewUser(socketIn, sctx, allUsers);
        }
        if (requestType == Commands.DO_SEND_USER_CREATION_MAIL.getValue()) {
           verificationCode = UserMethodsServer.doSendNewUserMail(socketIn, socketOut,"Verification code for your To-Do list account");
        }
        if (requestType == Commands.DO_VERIFY_CLIENT.getValue()) {
            currentUser = UserMethodsServer.verifyClient(socketIn, socketOut, sctx, currentUser, argon2, allUsers);
        }
        if (requestType == Commands.DO_CHECK_FOR_USERNAME.getValue()) {
            boolean checkUsername = ServerThreadTaskCommands.checkForUsernameInList(socketIn.readUTF(), this);
            socketOut.writeBoolean(checkUsername);
        }
        if (requestType == Commands.DO_ADD_TASK.getValue()) {
            ServerThreadTaskCommands.addTask(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_DISPLAY_TASK.getValue()) {
            ServerThreadTaskCommands.displayTasks(socketOut, this);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_CERTAIN.getValue()) {
            ServerThreadTaskCommands.displayCertainUserTasks(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue()) {
            ServerThreadTaskCommands.displayTaskByTopic(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_ADD_COMMENT.getValue()) {
            ServerThreadTaskCommands.addComment(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_PUSH_DEADLINE.getValue()) {
            ServerThreadTaskCommands.pushDeadline(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_COMPLETE_TASK.getValue()) {
            ServerThreadTaskCommands.completeTask(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            ServerThreadTaskCommands.addTaskToOtherUser(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()) {
            ServerThreadTaskCommands.searchTaskByDescription(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()) {
            ServerThreadTaskCommands.searchTaskByUsername(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()) {
            ServerThreadTaskCommands.searchTaskByDeadline(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_FOLLOW_TASK.getValue()) {
            ServerThreadTaskCommands.followTask(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_DELETE_USER.getValue()) {
            ServerThreadTaskCommands.deleteUser(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_SEARCH_TASKS_BY_TOPIC.getValue()) {
            ServerThreadTaskCommands.searchTaskByTopic(socketIn, socketOut, this);
        }
        if (requestType == Commands.DO_CLOSE_TODO_LIST_1.getValue() || requestType == Commands.DO_CLOSE_TODO_LIST_2.getValue()) {
            return ServerThreadTaskCommands.closeTodoList(socketIn, socketOut);
        }
        if (requestType == Commands.DO_SEND_PASSWORD_CHANGE_MAIL.getValue()) {
            verificationCode = UserMethodsServer.doSendPasswordChangeMail(socketIn, socketOut, this, "Changing your To Do List account's password");
        }
        if (requestType == Commands.DO_CHANGE_PASSWORD.getValue()) {
            UserMethodsServer.doChangePassword(socketIn,socketOut,sctx,allUsers);
        }
        if (requestType == Commands.DO_CONFIRM_VERIFICATION_CODE.getValue()) {
            UserMethodsServer.checkVerificationCode(socketOut, socketIn, verificationCode);
        }
        return false;
    }


}