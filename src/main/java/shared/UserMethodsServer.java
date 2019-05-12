package shared;

import Server.*;
import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class UserMethodsServer {



    public static void doChangePassword(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers) throws IOException {
        String username = socketIn.readUTF();
        String hashedPassword = socketIn.readUTF();
        User currentUser = null;
        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    currentUser = user;
                    break;
                }
            }
            currentUser.setNewPassword(hashedPassword);
            currentUser = null;
        }

        socketOut.writeInt(Commands.DO_CHANGE_PASSWORD.getValue());
        socketOut.writeUTF("Parool sai edukalt muudetud.");
    }
    public static void doTryChangePassword(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws Exception {
        String username = socketIn.readUTF();
        String email = socketIn.readUTF();

        if (ServerThreadTaskCommands.checkForUsernameInList(username, serverThread)) {
            SendMail sendMail = new SendMail();
            String verificationCode = UserMethodsClient.generateVerificationCode();
            if (sendMail.sendMail(email,
                    "Changing your To Do List account's password",
                    "Hello!" +
                            "\r\n" + "\r\n" +
                            "Your verification code is: " + verificationCode + "." +
                            "\r\n" + "\r\n" +
                            "Thank you for using our to-do app!")) {
                socketOut.writeInt(Commands.DO_TRY_CHANGE_PASSWORD.getValue());
                socketOut.writeUTF(verificationCode);
            }
            else {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Emaili saatmine ebaõnnestus.");
            }
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellist kasutajat ei eksisteeri.");
        }
    }

    public static void saveNewUser(DataInputStream socketIn,ServerContext sctx, List<User> allUsers) throws IOException {
        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);

        synchronized (sctx) {
            allUsers.add(newUser);
        }
    }
    public static User verifyClient(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, User currentUser, Argon2 argon2, List<User> allUsers) throws Exception {
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
                return currentUser;
            } else {
                usernameExists = false;
                currentUser = null;
                socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // kui sisselogimine ei õnnestu
                socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti." + "\r\n");
            }
        } else {
            socketOut.writeInt(Commands.DO_NOT_CONFIRM_LOGIN.getValue()); // sisselogimine ei õnnestunud
            socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti." + "\r\n");
        }
        return currentUser;
    }
}
