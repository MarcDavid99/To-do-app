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

    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 1337);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream input = new DataInputStream(socket.getInputStream())) {

            label:
            while (true) {
                System.out.print("Registreerimiseks kirjutage:       1" + "\r\n" +
                        "Sisse logimiseks kirjutage:        2" + "\r\n" +
                        "Programmi sulgemiseks kirjutage:   3" + "\r\n" +
                        "Valige tegevus: ");
                Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
                String initialCommand = scanner.nextLine();

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
                                System.out.println("Erinevad võimalused: " + "\r\n" +
                                        "11 - lisa ülesanne" + "\r\n" +
                                        "12 - vaata ülesandeid" + "\r\n" +
                                        "13 - lisa ülesandele kommentaar" + "\r\n" +
                                        "14 - muuda ülesande deadline'i" + "\r\n" +
                                        "15 - märgi ülesanne lõpetatuks" + "\r\n" +
                                        "16 - lisa ülesanne teisele kasutajale" + "\r\n" +
                                        "17 - otsi ülesannet" + "\r\n" +
                                        "18 - jälgi mingit ülesannet" + "\r\n" +
                                        "19 - sulge programm" + "\r\n");

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
                                        System.out.println("Programm sulgub!");
                                        return;
                                    }
                                }
                                //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
                                else {
                                    System.out.println("Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17, 18)");
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
                        System.out.println("Sisestage korrektne käsk (1, 2, 3)");
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