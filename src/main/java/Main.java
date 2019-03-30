import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static final int doAddTask = 11;
    public static final int doDisplayTasks = 12;
    public static final int doEditTask = 13;
    public static final int doCompleteTask = 14;
    public static final int doCloseTodoList1 = 3;
    public static final int doCheckForUsername = 95;

    // Neid hetkel pole vaja, aga äkki tulevikus on
    /*
    public static final int doCloseTodoList2 = 15;
    public static final int doAddComment = 16;
    public static final int doPushDeadline = 17;
    */

    public static final int doSaveNewUser = 91;
    public static final int doVerifyClient = 92;
    public static final int doConfirmLogin = 93;
    public static final int doNotConfirmLogin = 94;

    private static Argon2 argon2 = Argon2Factory.create();

    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            label:
            while (true) {
                System.out.print("Kui soovite registreerida kasutajat, kirjutage 1" + "\r\n" +
                        "Kui teil on kasutaja olemas, kirjutage 2" + "\r\n" +
                        "Kui soovite programmi sulgeda, kirjutage 3" + "\r\n" +
                        "Valige tegevus: ");
                Scanner scanner = new Scanner(System.in);
                String initialCommand = scanner.nextLine();

                switch (initialCommand) {
                    case "1":
                        //Kasutaja loomise meetod
                        userCreation(input, out, scanner);
                        break;
                    case "2":
                        //Kasutaja tuvastamise meetod
                        if (userVerification(out, input, scanner)) {
                            while (true) {
                                String[] possibleCommands = {"11", "12", "13", "14", "15"};
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
                                    int messageTypeFromServer = input.readInt();
                                    processCommand(input, out, messageTypeFromServer);
                                    if (input.readBoolean()) {
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
                        break;
                    case "3":
                        out.writeInt(doCloseTodoList1);
                        if (input.readBoolean()) {
                            System.out.println("Programm sulgub!");
                            break label;
                        }
                        break;
                    //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                    default:
                        System.out.println("Sisestage korrektne käsk (1,2 või 3)");
                        break;
                }
            }
        }
    }

    private static void processCommand(DataInputStream input, DataOutputStream out, int messageTypeFromServer) throws IOException {
        Scanner scanner = new Scanner(System.in);
        if (messageTypeFromServer == doCompleteTask) {
            //message removemine
            System.out.println(input.readUTF());
            int taskIndex = scanner.nextInt();
            out.writeInt(taskIndex);
            String message = input.readUTF();
            System.out.println(message);
        } else if (messageTypeFromServer == doDisplayTasks) {
            //task listi kuvamine kasutajale
            int tasksAmount = input.readInt();
            for (int i = 0; i < tasksAmount; i++) {
                System.out.println(input.readUTF());
            }
        } else if (messageTypeFromServer == doEditTask) {
            System.out.println(input.readUTF());
            int taskIndex = scanner.nextInt();
            out.writeInt(taskIndex);
            //annab valiku, kuidas soovid messaget muuta
            for (int i = 0; i < 2; i++) {
                System.out.println(input.readUTF());
            }
            System.out.print("Valige sobiv tegevus: ");
            String command = scanner.nextLine();
            if (command.equals("16")) {
                System.out.println("Sisestage kommentaar: ");
                String comment = scanner.nextLine();
                out.writeUTF(comment);
                System.out.println(input.readUTF());
            } else if (command.equals("17")) {
                System.out.println(input.readUTF());
                try {
                    int days = scanner.nextInt();
                    out.writeInt(days);
                    System.out.println(input.readUTF());
                } catch (InputMismatchException e) {
                    System.out.println("Te ei sisestanud päevade arvu korrektselt.");
                }
            }
        } else if (messageTypeFromServer == doAddTask) {
            System.out.println(input.readUTF());
            String taskDescription = scanner.nextLine();
            out.writeUTF(taskDescription);
            System.out.println(input.readUTF());
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

                            String hashedPassword = argon2.hash(10,65536,1,password);

                            User newUser = new User(firstName, lastName, username, mailAddress, hashedPassword);

                            System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);
                            System.out.println();

                            socketOut.writeInt(doSaveNewUser);
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
        socketOut.writeInt(doCheckForUsername);
        socketOut.writeUTF(username);
        return socketIn.readBoolean();
    }


    private static boolean userVerification(DataOutputStream socketOut, DataInputStream input, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();


        socketOut.writeInt(doVerifyClient);
        socketOut.writeUTF(existingUsername);
        socketOut.writeUTF(existingPassword);

        //tuleks saada serverilt tagasi kinnitus, et kasutaja on olemas ja parool õige
        int type = input.readInt();
        //93 tähendab, et sisselogimine õnnestus
        if (type == doConfirmLogin) {
            String message = input.readUTF();
            System.out.println(message);
            return true;
        }
        //94 tähendab, et ilmnes probleem sisselogimisel
        if (type == doNotConfirmLogin) {
            String message = input.readUTF();
            System.out.println(message);
            return false;
        }
        return false;
    }

    private static void commandToServer(DataOutputStream socketOut, String command) throws IOException {
        int commandAsInt = Integer.parseInt(command);
        socketOut.writeInt(commandAsInt);
    }

    private static boolean isRequiredPassword(String password) {
        return password.length() >= 8;
    }
}