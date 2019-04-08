import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class UserCreationVerification {
    static void userCreation(Argon2 argon2, DataInputStream socketIn, DataOutputStream socketOut, Scanner scanner) throws IOException {

        /*Console console = System.console();
        if(console != null){
            String firstName = console.readLine("Sisestage oma eesnimi: ");
            String lastName = console.readLine("Sisestage oma perenimi: ");
            String username = console.readLine("Sisestage soovitud kasutajanimi: ");
            String mailAddress = console.readLine("Sisestage soovitud meiliaadress: ");
            char[] pswd = console.readPassword("Sisestage soovitud salasõna: ");
            String password = new String(pswd);
        }*/

        String firstName;
        String lastName;
        String username;
        String mailAddress;
        String password;
        while (true) {
            System.out.print("Sisestage oma eesnimi(2-40 tähemärki): ");
            firstName = scanner.nextLine();
            if (firstName.length() >= 2 && firstName.length() <= 40) {
                break;
            } else {
                System.out.println("Sisestasite ebakorrektse pikkusega eesnime, proovige uuesti!");
            }
        }
        while (true) {
            System.out.print("Sisestage oma perenimi(2-40 tähemärki): ");
            lastName = scanner.nextLine();
            if (lastName.length() >= 2 && lastName.length() <= 40) {
                break;
            } else {
                System.out.println("Sisestasite ebakorrektse pikkusega perenime, proovige uuesti!");
            }
        }
        while (true) {
            System.out.print("Sisestage soovitud kasutajanimi(5-20 tähemärki): ");
            username = scanner.nextLine();
            if (username.length() >= 5 && username.length() <= 20) {
                if (!checkIfUsernameExists(socketIn, socketOut, username)) {
                    break;
                } else {
                    System.out.println("Kasutajanimi juba eksisteerib, valige uus!");
                }
            } else {
                System.out.println("Sisestasite ebakorrektse pikkusega kasutajanime, proovige uuesti!");
            }
        }
        while (true) {
            System.out.print("Sisestage soovitud meiliaadress: ");
            mailAddress = scanner.nextLine();
            System.out.print("Sisestage soovitud salasõna: ");
            password = scanner.nextLine();
            String hashedPassword = argon2.hash(10, 65536, 1, password);
            if (isRequiredPassword(password)) {
                //genereeritakse suvaline täisarv
                int verificationCode = (int) Math.floor(Math.random() * 100000 + 1);
                //saadetakse kood sisestatud meilile
                SendMail verificationmail = new SendMail();
                if (verificationmail.sendMail(mailAddress, "Verification code for your To-Do list account",
                        "Hello!" + "\r\n" + "\r\n" + "Your verification code is: " + verificationCode + "." + "\r\n" + "\r\n" +
                                "Thank you for using our to-do app!")) {
                    System.out.print("Sisestage sisestatud meiliaadressile saadetud verification code: ");
                    try {
                        int inputCode = Integer.parseInt(scanner.nextLine());
                        if (inputCode == verificationCode) {


                            User newUser = new User(firstName, lastName, username, mailAddress, hashedPassword);

                            System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);
                            System.out.println();

                            socketOut.writeInt(Commands.doSaveNewUser);
                            Gson gsonUser = new Gson();
                            String jsonUser = gsonUser.toJson(newUser);
                            socketOut.writeUTF(jsonUser);
                            break;
                        } else {
                            System.out.println("Sisestatud kood ei ole õige, palun proovige uuesti registreerida.");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("Sisestasite koodi valesti.");
                    }
                } else {
                    System.out.println("Sisestatud meiliaadressile meili saatmine ebaõnnestus, palun proovige uuesti registreerida.");
                }
            } else {
                System.out.println("Salasõna peab olema vähemalt 8 tähemärki pikk. Palun proovige uuesti registreerida.");
            }
        }
    }

    static boolean userVerification(DataOutputStream socketOut, DataInputStream input, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();


        socketOut.writeInt(Commands.doVerifyClient);
        socketOut.writeUTF(existingUsername);
        socketOut.writeUTF(existingPassword);

        //tuleks saada serverilt tagasi kinnitus, et kasutaja on olemas ja parool õige
        int type = input.readInt();
        if (type == Commands.doConfirmLogin) {
            String message = input.readUTF();
            System.out.println(message);
            System.out.println();
            return true;
        }
        if (type == Commands.doNotConfirmLogin) {
            String message = input.readUTF();
            System.out.println(message);
            return false;
        }
        return false;
    }


    private static boolean checkIfUsernameExists(DataInputStream socketIn, DataOutputStream socketOut, String username) throws IOException {
        socketOut.writeInt(Commands.doCheckForUsername);
        socketOut.writeUTF(username);
        return socketIn.readBoolean();
    }

    private static boolean isRequiredPassword(String password) {
        return password.length() >= 8;
    }
}