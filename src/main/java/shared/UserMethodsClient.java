package shared;

import Server.SendMail;
import Server.User;
import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

public class UserMethodsClient {

    public static void userCreation(Argon2 argon2, DataInputStream socketIn, DataOutputStream socketOut, Scanner scanner) throws Exception {
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
            String hashedPassword = argon2.hash(10, 65536, 1, password);
            if (isRequiredPassword(password)) {
                //genereeritakse suvaline täisarv
                String verificationCode = generateVerificationCode();
                //saadetakse kood sisestatud meilile
                SendMail verificationmail = new SendMail();
                if (verificationmail.sendMail(mailAddress,
                        "Verification code for your To-Do list account",
                        "Hello!" +
                                "\r\n" + "\r\n" +
                                "Your verification code is: " + verificationCode + "." +
                                "\r\n" + "\r\n" +
                                "Thank you for using our to-do app!")) {
                    System.out.print("Meiliaadressile saadetud kinnituskood: ");
                    String inputCode = scanner.nextLine();
                    if (inputCode.equals(verificationCode)) {

                        String userID = UUID.randomUUID().toString();
                        User newUser = new User(userID, firstName, lastName, username, mailAddress, hashedPassword);

                        System.out.println("Kasutaja " + username + " on edukalt loodud!");
                        System.out.println();

                        socketOut.writeInt(Commands.DO_SAVE_NEW_USER.getValue());
                        Gson gsonUser = new Gson();
                        String jsonUser = gsonUser.toJson(newUser);
                        socketOut.writeUTF(jsonUser);
                        break;
                    } else {
                        System.out.println(TextColours.ANSI_YELLOW + "Sisestatud kood ei ole õige, palun proovige uuesti registreerida." + TextColours.ANSI_RESET);
                    }
                } else {
                    System.out.println(TextColours.ANSI_YELLOW + "Sisestatud meiliaadressile meili saatmine ebaõnnestus, palun proovige uuesti registreerida." + TextColours.ANSI_RESET);
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
        socketOut.writeInt(Commands.DO_TRY_CHANGE_PASSWORD.getValue());
        socketOut.writeUTF(username);
        socketOut.writeUTF(email);
        int messageType = socketIn.readInt();
        if (messageType == Commands.ERROR_OCCURED.getValue()) {
            System.out.println(socketIn.readUTF());
        }
        else {
            String verificationCode = socketIn.readUTF();
            System.out.print("Meiliaadressile saadetud kinnituskood: ");
            String inputCode = scanner.nextLine();
            if(inputCode.equals(verificationCode)){
                while (true) {
                    System.out.print("Uus parool: ");
                    String newPassword = scanner.nextLine();
                    if (isRequiredPassword(newPassword)) {
                        String hashedPassword = argon2.hash(10, 65536, 1, newPassword);
                        socketOut.writeInt(Commands.DO_CHANGE_PASSWORD.getValue());
                        socketOut.writeUTF(username);
                        socketOut.writeUTF(hashedPassword);
                        messageType = socketIn.readInt();
                        System.out.println(socketIn.readUTF());
                        break;
                    } else {
                        System.out.println("Parool peab olema vähemalt 8 tähemärki pikk. Proovi uuesti.");
                    }
                }
            }
            else {
                System.out.println("Sisestasite vale kinnituskoodi.");
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
