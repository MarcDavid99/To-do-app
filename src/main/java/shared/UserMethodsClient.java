package shared;

import Server.SendMail;
import de.mkammerer.argon2.Argon2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class UserMethodsClient {

    public static void userCreation(DataInputStream socketIn, DataOutputStream socketOut, Scanner scanner) throws Exception {
        String firstName;
        String lastName;
        String username;
        String mailAddress;
        String password;

        while (true) {
            System.out.print("Eesnimi(2-40 tähemärki): ");
            firstName = scanner.nextLine();
            if (firstName.length() >= 2 && firstName.length() <= 40) {
                break;
            } else {
                System.out.println(TextColours.ANSI_YELLOW + "Sisestasite ebakorrektse pikkusega eesnime, proovige uuesti!" + TextColours.ANSI_RESET);
            }
        }
        while (true) {
            System.out.print("Perenimi(2-40 tähemärki): ");
            lastName = scanner.nextLine();
            if (lastName.length() >= 2 && lastName.length() <= 40) {
                break;
            } else {
                System.out.println(TextColours.ANSI_YELLOW + "Sisestasite ebakorrektse pikkusega perenime, proovige uuesti!" + TextColours.ANSI_RESET);
            }
        }
        while (true) {
            System.out.print("Kasutajanimi(5-20 tähemärki): ");
            username = scanner.nextLine();
            if (username.length() >= 5 && username.length() <= 20) {
                if (!checkIfUsernameExists(socketIn, socketOut, username)) {
                    break;
                } else {
                    System.out.println(TextColours.ANSI_YELLOW + "Kasutajanimi juba eksisteerib, valige uus!" + TextColours.ANSI_RESET);
                }
            } else {
                System.out.println(TextColours.ANSI_YELLOW + "Sisestasite ebakorrektse pikkusega kasutajanime, proovige uuesti!" + TextColours.ANSI_RESET);
            }
        }
        while (true) {
            System.out.print("Meiliaadress: ");
            mailAddress = scanner.nextLine();
            System.out.print("Salasõna: ");
            password = scanner.nextLine();
            System.out.println("Oodake mõned sekundid, kuni teie meiliaadressile tuleb kinnituskood.");

            if (isRequiredPassword(password)) {
                socketOut.writeInt(Commands.DO_SEND_USER_CREATION_MAIL.getValue());
                socketOut.writeUTF(mailAddress);

                int messageType = socketIn.readInt();
                if (messageType == Commands.ERROR_OCCURED.getValue()) {
                    System.out.println(socketIn.readUTF());
                }
                else {
                    System.out.print("Meiliaadressile saadetud kinnituskood: ");
                    String inputCode = scanner.nextLine();

                    socketOut.writeInt(Commands.DO_CONFIRM_VERIFICATION_CODE.getValue());
                    socketOut.writeUTF(inputCode);

                    messageType = socketIn.readInt();
                    if (messageType == Commands.ERROR_OCCURED.getValue()) {
                        System.out.println(socketIn.readUTF());
                    }
                    else {
                        socketOut.writeInt(Commands.DO_SAVE_NEW_USER.getValue());
                        socketOut.writeUTF(firstName);
                        socketOut.writeUTF(lastName);
                        socketOut.writeUTF(username);
                        socketOut.writeUTF(mailAddress);
                        socketOut.writeUTF(password);
                        System.out.println("Kasutaja " + username + " on edukalt loodud!" + "\r\n");
                        break;
                    }
                }
            } else {
                System.out.println(TextColours.ANSI_YELLOW + "Salasõna peab olema vähemalt 8 tähemärki pikk. Palun proovige uuesti registreerida." + TextColours.ANSI_RESET);
            }
        }
    }

    public static boolean userVerification(DataOutputStream socketOut, DataInputStream input, Scanner scanner) throws IOException {
        System.out.print("Kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Salasõna: ");
        String existingPassword = scanner.nextLine();

        socketOut.writeInt(Commands.DO_VERIFY_CLIENT.getValue());
        socketOut.writeUTF(existingUsername);
        socketOut.writeUTF(existingPassword);

        //tuleks saada serverilt tagasi kinnitus, et kasutaja on olemas ja parool õige
        int type = input.readInt();
        if (type == Commands.DO_CONFIRM_LOGIN.getValue()) {
            String message = input.readUTF();
            System.out.println(message);
            return true;
        }
        if (type == Commands.DO_NOT_CONFIRM_LOGIN.getValue()) {
            String message = input.readUTF();
            System.out.println(message);
            return false;
        }
        return false;
    }


    private static boolean checkIfUsernameExists(DataInputStream socketIn, DataOutputStream socketOut, String username) throws IOException {
        socketOut.writeInt(Commands.DO_CHECK_FOR_USERNAME.getValue());
        socketOut.writeUTF(username);
        return socketIn.readBoolean();
    }

    public static void changePassword(DataOutputStream socketOut, DataInputStream socketIn, Argon2 argon2) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Kasutajanimi: ");
        String username = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        socketOut.writeInt(Commands.DO_SEND_PASSWORD_CHANGE_MAIL.getValue());
        socketOut.writeUTF(username);
        socketOut.writeUTF(email);
        int messageType = socketIn.readInt();
        if (messageType == Commands.ERROR_OCCURED.getValue()) {
            System.out.println(socketIn.readUTF());
        }
        else {
            System.out.print("Meiliaadressile saadetud kinnituskood: ");
            String inputCode = scanner.nextLine();

            socketOut.writeInt(Commands.DO_CONFIRM_VERIFICATION_CODE.getValue());
            socketOut.writeUTF(inputCode);

            messageType = socketIn.readInt();
            if (messageType == Commands.ERROR_OCCURED.getValue()) {
                System.out.println(socketIn.readUTF());
            }
            else {
                while (true) {
                    System.out.print("Uus parool: ");
                    String newPassword = scanner.nextLine();
                    if (isRequiredPassword(newPassword)) {
                        socketOut.writeInt(Commands.DO_CHANGE_PASSWORD.getValue());
                        socketOut.writeUTF(username);
                        socketOut.writeUTF(newPassword);
                        messageType = socketIn.readInt();
                        System.out.println(socketIn.readUTF());
                        break;
                    } else {
                        System.out.println("Parool peab olema vähemalt 8 tähemärki pikk. Proovi uuesti.");
                    }
                }
            }
        }
    }

    public static String generateVerificationCode() {
        String possibleCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder verificationCode = new StringBuilder();
        Random random = new Random();
        while (verificationCode.length() < 7) {
            int elementIndex = random.nextInt(possibleCharacters.length());
            verificationCode.append(possibleCharacters.charAt(elementIndex));
        }
        String strVerificationCode = verificationCode.toString();
        return strVerificationCode;
    }

    private static boolean isRequiredPassword(String password) {
        return password.length() >= 8;
    }
}
