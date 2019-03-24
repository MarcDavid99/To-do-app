import com.google.gson.Gson;

import javax.xml.crypto.Data;
import java.io.Console;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            while (true) {
                System.out.print("Kui soovite registreerida kasutajat, kirjutage 1" + "\r\n" +
                        "Kui teil on kasutaja olemas, kirjutage 2" + "\r\n" +
                        "Kui soovite programmi sulgeda, kirjutage 3" + "\r\n" +
                        "Valige tegevus: ");
                Scanner scanner = new Scanner(System.in);
                String initialCommand = scanner.nextLine();

                if (initialCommand.equals("1")) {
                    //Kasutaja loomise meetod
                    userCreation(input, out, scanner);
                } else if (initialCommand.equals("2")) {
                    //Kasutaja tuvastamise meetod
                    if (userVerification(out, input, scanner)) {
                        while (true) {
                            String[] possibleCommands = {"11", "12", "13", "14","15"};
                            System.out.println("Erinevad võimalused: " + "\r\n" +
                                    "11 - lisa ülesanne" + "\r\n" +
                                    "12 - vaata ülesannet" + "\r\n" +
                                    "13 - muuda ülesannet" + "\r\n" +
                                    "14 - märgi ülesanne lõpetatuks" + "\r\n" +
                                    "15 - sulge programm");

                            System.out.print("Valige sobiv tegevus: ");
                            String command = scanner.nextLine();
                            if (Arrays.asList(possibleCommands).contains(command)) {
                                commandToServer(out, command);
                                if(input.readBoolean()){
                                    System.out.println("Programm sulgub");
                                    return;
                                }
                            }
                            //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
                            else {
                                System.out.println("Sisestage korrektne käsk (11, 12, 13, 14)");
                            }
                        }
                    }
                } else if (initialCommand.equals("3")) {
                    out.writeInt(3);
                    if (input.readBoolean()) {
                        System.out.println("Programm sulgub!");
                        break;
                    }
                }
                //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                else {
                    System.out.println("Sisestage korrektne käsk (1 või 2)");
                }
            }
        }
    }

    private static void userCreation(DataInputStream socketIn, DataOutputStream socketOut, Scanner scanner) throws IOException {

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
            if (isequiredPassword(password)) {
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
                            //siin võiks enne useri loomist passwordi ära hashida
                            User newUser = new User(firstName, lastName, username, mailAddress, password.hashCode());

                            System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);
                            System.out.println();

                            socketOut.writeInt(91);
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

    private static void addToMainUserList(List<User> list) {

    }

    private static void detectMessageFromServer(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

    }

    private static boolean checkIfUsernameExists(DataInputStream socketIn, DataOutputStream socketOut, String username) throws IOException {

        socketOut.writeInt(93);
        socketOut.writeUTF(username);
        boolean usernameAlreadyExists = socketIn.readBoolean();
        return usernameAlreadyExists;
    }


    private static boolean userVerification(DataOutputStream socketOut, DataInputStream input, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();


        int hashedPassword = existingPassword.hashCode();

        socketOut.writeInt(92);
        socketOut.writeUTF(existingUsername);
        socketOut.writeInt(hashedPassword);

        //tuleks saada serverilt tagasi kinnitus, et kasutaja on olemas ja parool õige
        int type = input.readInt();
        if (type == 93) {
            System.out.println();
            System.out.println("Olete sisselogitud.");
            return true;
        }
        if (type == 94) {
            System.out.println("Sisestatud parool on vale. Proovige uuesti.");
            return false;
        }
        if (type == 95) {
            System.out.println("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
            return false;
        }
        return false;
        //vb sellest typeist piisakski, et nt kui parool vale siis mingi kindel type
        //kui kõik korras siis kindel, kui kasutaja sellise
        //nimega puudub siis ka mingi kindel message type
    }

    private static void commandToServer(DataOutputStream socketOut, String command) throws IOException {
        int commandAsInt = Integer.parseInt(command);
        socketOut.writeInt(commandAsInt);
    }

    private static boolean isequiredPassword(String password) {
        return password.length() >= 8;
    }
}