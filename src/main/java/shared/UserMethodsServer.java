package shared;

import Server.*;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class UserMethodsServer {



    public static void doChangePassword(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers) throws IOException {
        String username = socketIn.readUTF();
        String newPassword = socketIn.readUTF();
        Argon2 argon2 = Argon2Factory.create();
        String hashedPassword = argon2.hash(10, 65536, 1, newPassword);
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

    public static String doSendNewUserMail(DataInputStream socketIn, DataOutputStream socketOut, String subject) throws Exception {
        String email = socketIn.readUTF();
        String verificationCode = "";

            SendMail sendMail = new SendMail();
            verificationCode = UserMethodsClient.generateVerificationCode();
            if (sendMail.sendMail(email,
                    subject,
                    "Hello!" +
                            "\r\n" + "\r\n" +
                            "Your verification code is: " + verificationCode + "." +
                            "\r\n" + "\r\n" +
                            "Thank you for using our to-do app!")) {
                socketOut.writeInt(Commands.DO_SEND_PASSWORD_CHANGE_MAIL.getValue());
            }
            else {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Emaili saatmine ebaõnnestus.");
            }

        return verificationCode;
    }

    public static String doSendPasswordChangeMail(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread, String subject) throws Exception {
        String username = socketIn.readUTF();
        String email = socketIn.readUTF();
        String verificationCode = "";

        if (ServerThreadTaskCommands.checkForUsernameInList(username, serverThread)) {
            SendMail sendMail = new SendMail();
            verificationCode = UserMethodsClient.generateVerificationCode();
            if (sendMail.sendMail(email,
                    subject,
                    "Hello!" +
                            "\r\n" + "\r\n" +
                            "Your verification code is: " + verificationCode + "." +
                            "\r\n" + "\r\n" +
                            "Thank you for using our to-do app!")) {
                socketOut.writeInt(Commands.DO_SEND_PASSWORD_CHANGE_MAIL.getValue());
            }
            else {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Emaili saatmine ebaõnnestus.");
            }
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellist kasutajat ei eksisteeri.");
        }

        return verificationCode;
    }

    public static void checkVerificationCode(DataOutputStream socketOut, DataInputStream socketIn, String sentVerificationCode) throws IOException{
        String verificationCode = socketIn.readUTF();
        if (sentVerificationCode.equals(verificationCode)) {
            socketOut.writeInt(Commands.DO_CONFIRM_VERIFICATION_CODE.getValue());
        }
        else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kinnituskood oli ebakorrektne.");
        }
    }

    public static void saveNewUser(DataInputStream socketIn,ServerContext sctx, List<User> allUsers) throws IOException {
        String userID = UUID.randomUUID().toString();
        String firstName = socketIn.readUTF();
        String lastName = socketIn.readUTF();
        String username = socketIn.readUTF();
        String mailAddress = socketIn.readUTF();
        String password = socketIn.readUTF();

        Argon2 argon2 = Argon2Factory.create();
        String hashedPassword = argon2.hash(10, 65536, 1, password);

        synchronized (sctx) {
            allUsers.add(new User(userID, firstName, lastName, username, mailAddress, hashedPassword));
        }
    }
    public static User verifyClient(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, User currentUser, Argon2 argon2, List<User> allUsers) throws Exception {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        boolean usernameExists = false;
        currentUser = null;

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    usernameExists = true;
                    currentUser = user;
                    break;
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
            socketOut.writeUTF("Sellise kasutajanimega kasutajat ei leidu. Proovige uuesti." + "\r\n");
        }
        return currentUser;
    }
}
