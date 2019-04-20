import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    private static Argon2 argon2 = Argon2Factory.create();

    // https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            label:
            while (true) {
                System.out.println(ANSI_RED + "Erinevad võimalused: " + ANSI_RESET);
                System.out.print(
                        "Registreerimiseks kirjutage:       1" + "\r\n" +
                        "Sisse logimiseks kirjutage:        2" + "\r\n" +
                        "Programmi sulgemiseks kirjutage:   3" + "\r\n" +
                        "Valige sobiv tegevus: ");
                Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
                String initialCommand = scanner.nextLine();
                System.out.println();

                switch (initialCommand) {
                    case "1":
                        //Kasutaja loomise meetod
                        UserCreationVerification.userCreation(argon2, input, out, scanner);
                        break;
                    case "2":
                        //Kasutaja tuvastamise meetod
                        if (UserCreationVerification.userVerification(out, input, scanner)) {
                            while (true) {
                                String[] possibleCommands = {"11", "12", "13", "14", "15", "16", "17", "18","19"};
                                String[] commandsThatNeedList = {"13", "14", "15"};
                                System.out.println(ANSI_RED + "Erinevad võimalused: " + ANSI_RESET + "\r\n" +
                                        "Lisa ülesanne:                     11" + "\r\n" +
                                        "Vaata ülesandeid:                  12" + "\r\n" +
                                        "Lisa üleasndele kommentaar:        13" + "\r\n" +
                                        "Muuda ülesande tähtaega:           14" + "\r\n" +
                                        "Märgi ülesanne lõpetatuks:         15" + "\r\n" +
                                        "Lisa ülesanne teisele kasutajale:  16" + "\r\n" +
                                        "Otsi ülesannet:                    17" + "\r\n" +
                                        "Jälgi mingit ülesannet:            18" + "\r\n" +
                                        "Sulge programm:                    19");

                                System.out.print("Valige sobiv tegevus: ");
                                String command = scanner.nextLine();
                                System.out.println();
                                if (Arrays.asList(possibleCommands).contains(command)) {
                                    int commandInt = Integer.parseInt(command);
                                    if (commandInt != Commands.DO_CLOSE_TODO_LIST_2.getValue()) {
                                        if (Arrays.asList(commandsThatNeedList).contains(command)) {
                                            out.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                                            //messageType loeb sisse, sest server saadab displayTasksi korral message type
                                            int messageType = input.readInt();
                                            ClientProcessCommands.processDisplayTasks(input);
                                            //loeb siin ikkagi sisse booleani, kuigi see pole oluline, aga ma ei hakka uut meetodit tegema
                                            //kui saab kasutada displayTasksi
                                            boolean notImportant = input.readBoolean();
                                        }
                                        //serverile vajaliku info saatmine
                                        commandToServer(out, commandInt);

                                        //serverilt saadud info vastuvõtmine
                                        int serverMessageType = input.readInt();
                                        processServerMessageType(input, serverMessageType);

                                    } else {
                                        out.writeInt(Commands.DO_CLOSE_TODO_LIST_2.getValue());
                                    }
                                    if (input.readBoolean()) {
                                        System.out.println("Programm sulgubm!");
                                        return;
                                    }
                                }
                                //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
                                else {
                                    System.out.println(ANSI_YELLOW + "Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17, 18)" + ANSI_RESET);
                                }
                            }
                        }
                        break;
                    case "3":
                        out.writeInt(Commands.DO_CLOSE_TODO_LIST_1.getValue());
                        if (input.readBoolean()) {
                            System.out.println("Programm sulgub!");
                            break label;
                        }
                        break;
                    //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                    default:
                        System.out.println(ANSI_YELLOW + "Sisestage korrektne käsk (1, 2, 3)" + ANSI_RESET);
                        break;
                }
            }
        }

    }


    private static void commandToServer(DataOutputStream out, int command) throws IOException {
        if (command == Commands.DO_COMPLETE_TASK.getValue()) {
            ClientSendMessage.sendCompleteTask(out);
        } else if (command == Commands.DO_DISPLAY_TASK.getValue()) {
            out.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        } else if (command == Commands.DO_ADD_COMMENT.getValue()) {
            ClientSendMessage.sendAddComment(out);
        } else if (command == Commands.DO_PUSH_DEADLINE.getValue()) {
            ClientSendMessage.sendPushDeadline(out);
        } else if (command == Commands.DO_ADD_TASK.getValue()) {
            ClientSendMessage.sendAddTask(out);
        } else if (command == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            ClientSendMessage.sendAddTaskToOtherUsers(out);
        } else if (command == Commands.DO_SEARCH_TASKS.getValue()) {
            ClientSendMessage.sendSearchTasks(out);
        } else if (command == Commands.DO_FOLLOW_TASK.getValue()) {
            ClientSendMessage.sendFollowTask(out);
        }
    }

    private static void processServerMessageType(DataInputStream input, int command) throws IOException {
        if (command == Commands.DO_COMPLETE_TASK.getValue()) {
            ClientProcessCommands.processCompleteTask(input);
        } else if (command == Commands.DO_DISPLAY_TASK.getValue()) {
            ClientProcessCommands.processDisplayTasks(input);
        } else if (command == Commands.DO_ADD_COMMENT.getValue()) {
            ClientProcessCommands.processAddComment(input);
        } else if (command == Commands.DO_PUSH_DEADLINE.getValue()) {
            ClientProcessCommands.processPushDeadline(input);
        } else if (command == Commands.DO_ADD_TASK.getValue()) {
            ClientProcessCommands.processAddTask(input);
        } else if (command == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            ClientProcessCommands.processAddTaskToOtherUsers(input);
        } else if (command == Commands.ERROR_OCCURED.getValue()) {
            ClientProcessCommands.processErrorOccured(input);
        } else if (command == Commands.DO_SEARCH_TASKS.getValue()) {
            ClientProcessCommands.processShowSearchedTasks(input);
        } else if (command == Commands.DO_FOLLOW_TASK.getValue()) {
            ClientProcessCommands.processFollowTask(input);
        }
    }
}