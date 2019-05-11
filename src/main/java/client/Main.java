package client;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import shared.*;

public class Main {

    private static Argon2 argon2 = Argon2Factory.create();

    public static void main(String[] args) throws Exception {
        String address;
        int port = 1335;
        if (args.length == 0) {
            address = "localhost";

        } else {
            address = args[0];

        }

        label1:
        while (true) {
            try (Socket socket = new Socket(address, port);
                 DataInputStream socketIn = new DataInputStream(socket.getInputStream());
                 DataOutputStream socketOut = new DataOutputStream(socket.getOutputStream())) {

                System.out.println(TextColours.CYAN_BOLD_BRIGHT + " ------------------------------------" + TextColours.ANSI_RESET);
                System.out.println(TextColours.CYAN_BOLD_BRIGHT + "|             TO DO LIST             |" + TextColours.ANSI_RESET);
                System.out.println(TextColours.CYAN_BOLD_BRIGHT + " ------------------------------------" + TextColours.ANSI_RESET + "\r\n");

                label2:
                while (true) {

                    System.out.println(TextColours.ANSI_RED + "Erinevad võimalused" + TextColours.ANSI_RESET);
                    System.out.println(
                                    "Registreeri:                        1" + "\r\n" +
                                    "Logi sisse:                         2" + "\r\n" +
                                    "Vaheta parooli:                     3" + "\r\n" +
                                    "Sulge programm:                     4" + "\r\n" +
                                    "-------------------------------------");
                    System.out.print("Valige tegevus: ");
                    Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
                    String initialCommand = scanner.nextLine();
                    System.out.println();

                    switch (initialCommand) {
                        case "1":
                            //Kasutaja loomise meetod
                            UserMethodsClient.userCreation(argon2, socketIn, socketOut, scanner);
                            break;
                        case "2":
                            //Kasutaja tuvastamise meetod
                            if (UserMethodsClient.userVerification(socketOut, socketIn, scanner)) {
                                while (true) {
                                    List<Integer> possibleCommands = new ArrayList<>(Arrays.asList(Commands.DO_ADD_TASK.getValue(),
                                            Commands.DO_DISPLAY_TASK.getValue(), Commands.DO_ADD_COMMENT.getValue(),
                                            Commands.DO_PUSH_DEADLINE.getValue(), Commands.DO_COMPLETE_TASK.getValue(),
                                            Commands.DO_ADD_TASK_TO_OTHER_USER.getValue(), Commands.DO_SEARCH_TASKS.getValue(),
                                            Commands.DO_FOLLOW_TASK.getValue(), Commands.DO_CLOSE_TODO_LIST_2.getValue(),
                                            Commands.DO_LOG_OUT.getValue(), Commands.DO_DELETE_USER.getValue()));

                                    List<Integer> commandsThatNeedList = new ArrayList<>(Arrays.asList(Commands.DO_ADD_COMMENT.getValue(),
                                            Commands.DO_PUSH_DEADLINE.getValue(), Commands.DO_COMPLETE_TASK.getValue(),
                                            Commands.DO_FOLLOW_TASK.getValue()));

                                    System.out.println(TextColours.ANSI_RED + "\r\n" +
                                            "Erinevad võimalused" + TextColours.ANSI_RESET + "\r\n" +
                                            "Lisa ülesanne:                     11" + "\r\n" +
                                            "Vaata ülesandeid:                  12" + "\r\n" +
                                            "Lisa ülesandele kommentaar:        13" + "\r\n" +
                                            "Muuda ülesande tähtaega:           14" + "\r\n" +
                                            "Märgi ülesanne lõpetatuks:         15" + "\r\n" +
                                            "Lisa ülesanne teisele kasutajale:  16" + "\r\n" +
                                            "Otsi ülesannet:                    17" + "\r\n" +
                                            "Jälgi mingit ülesannet:            18" + "\r\n" +
                                            "Sulge programm:                    19" + "\r\n" +
                                            "Logi välja:                        20" + "\r\n" +
                                            "-------------------------------------");
                                    //Kustuta kasutaja:               5482 // tavakasutajale seda ei kuvata, aga meil on see võimalus olemas

                                    System.out.print("Valige tegevus: ");
                                    String command = scanner.nextLine();
                                    System.out.println();
                                    boolean backToMainMenu = false;
                                    try {
                                        int commandInt = Integer.parseInt(command);
                                        String usernameForFollowTask = "";
                                        if (possibleCommands.contains(commandInt)) {
                                            if (commandInt != Commands.DO_CLOSE_TODO_LIST_2.getValue() && commandInt != Commands.DO_LOG_OUT.getValue()) {
                                                if (commandsThatNeedList.contains(commandInt)) {
                                                    usernameForFollowTask = showTaskListBeforeCommand(socketIn, socketOut, commandInt, scanner, usernameForFollowTask);
                                                    if (usernameForFollowTask == null) {
                                                        continue;
                                                    }
                                                }
                                                System.out.println(TextColours.ANSI_PURPLE + "Kui soovite mingil hetkel tagasi peamenüüsse, siis vajutage sisestusel ainult ENTER-it." + TextColours.ANSI_RESET);
                                                backToMainMenu = processServerMessageType(socketIn, socketOut, commandInt, usernameForFollowTask);

                                            } else {
                                                socketOut.writeInt(Commands.DO_CLOSE_TODO_LIST_2.getValue());
                                            }
                                            if (!backToMainMenu) {
                                                if (socketIn.readBoolean()) {
                                                    if (commandInt == Commands.DO_CLOSE_TODO_LIST_2.getValue()) {
                                                        System.out.println(TextColours.ANSI_RED + "Programm sulgub!" + TextColours.ANSI_RESET);
                                                        return;
                                                    }
                                                    break label2;
                                                }
                                            }
                                        } else {
                                            if (commandInt == 5482) { // kasutaja kustutamine
                                                processServerMessageType(socketIn, socketOut, commandInt, usernameForFollowTask);
                                            } else {
                                                // valiti käsk, mida pole valikus
                                                System.out.println(TextColours.ANSI_YELLOW + "Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17, 18, 19, 20)" + TextColours.ANSI_RESET);
                                            }
                                        }
                                    } catch (NumberFormatException e) {
                                        // ei sisestatud täisarvu
                                        System.out.println(TextColours.ANSI_YELLOW + "Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17, 18, 19, 20)" + TextColours.ANSI_RESET);
                                    }
                                }
                            }
                            break;
                        case "3" :
                            UserMethodsClient.changePassword(socketOut, socketIn, argon2);
                            System.out.println();
                            break;
                        case "4":
                            socketOut.writeInt(Commands.DO_CLOSE_TODO_LIST_1.getValue());
                            if (socketIn.readBoolean()) {
                                System.out.println(TextColours.ANSI_RED + "Programm sulgub!" + TextColours.ANSI_RESET);
                                break label1;
                            }
                            break;
                        //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                        default:
                            System.out.println(TextColours.ANSI_YELLOW + "Sisestage korrektne käsk (1, 2, 3)" + TextColours.ANSI_RESET);
                            break;
                    }
                }
            }
        }
    }

    private static boolean processServerMessageType(DataInputStream socketIn, DataOutputStream socketOut, int command, String username) throws IOException {

        if (command == Commands.DO_COMPLETE_TASK.getValue()) {
            return ClientProcessCommands.processCompleteTask(socketIn, socketOut);
        } else if (command == Commands.DO_DISPLAY_TASK.getValue()) {
            return ClientProcessCommands.processDisplayTasks(socketIn, socketOut);
        } else if (command == Commands.DO_ADD_COMMENT.getValue()) {
            return ClientProcessCommands.processAddComment(socketIn, socketOut);
        } else if (command == Commands.DO_PUSH_DEADLINE.getValue()) {
            return ClientProcessCommands.processPushDeadline(socketIn, socketOut);
        } else if (command == Commands.DO_ADD_TASK.getValue()) {
            return ClientProcessCommands.processAddTask(socketIn, socketOut);
        } else if (command == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            return ClientProcessCommands.processAddTaskToOtherUsers(socketIn, socketOut);
        } else if (command == Commands.DO_SEARCH_TASKS.getValue()) {
            return ClientProcessCommands.processShowSearchedTasks(socketIn, socketOut);
        } else if (command == Commands.DO_FOLLOW_TASK.getValue()) {
            return ClientProcessCommands.processFollowTask(socketIn, socketOut, username);
        } else if (command == Commands.DO_DELETE_USER.getValue()) {
            return ClientProcessCommands.processDeleteUser(socketIn, socketOut);
        }
        return false;
    }

    public static String showTaskListBeforeCommand(DataInputStream socketIn, DataOutputStream socketOut, int commandInt, Scanner scanner, String usernameForFollowTask) throws IOException {
        int messageType = 0;
        if (commandInt == (Commands.DO_FOLLOW_TASK.getValue())) {
            // kuvatakse soovitud kasutajanime tasklist, et saaks valida sealt ülesande, mida jälgida
            System.out.print("Kasutajanimi, kelle ülesannet jälgida tahad: ");
            usernameForFollowTask = scanner.nextLine();
            socketOut.writeInt(Commands.DO_DISPLAY_TASK_CERTAIN.getValue());
            socketOut.writeUTF(usernameForFollowTask);
        } else {
            socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        }

        //messageType loeb sisse, sest server saadab displayTasksi korral message type
        messageType = socketIn.readInt();
        if (messageType == Commands.ERROR_OCCURED.getValue()) {
            ClientProcessCommands.processErrorOccured(socketIn);
            return null;
        }
        ClientProcessCommands.displayTasks(socketIn, "Ülesanded:");
        //loeb siin ikkagi sisse booleani, kuigi see pole oluline, aga ma ei hakka uut meetodit tegema
        //kui saab kasutada displayTasksi
        boolean notImportant = socketIn.readBoolean();
        return usernameForFollowTask;
    }
}