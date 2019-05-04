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

            System.out.println(TextColours.CYAN_BOLD_BRIGHT + " ----------------------------------" + TextColours.ANSI_RESET);
            System.out.println(TextColours.CYAN_BOLD_BRIGHT + "|            TO DO LIST            |" + TextColours.ANSI_RESET);
            System.out.println(TextColours.CYAN_BOLD_BRIGHT + " ----------------------------------" + TextColours.ANSI_RESET + "\r\n");

            label:
            while (true) {

                System.out.println(TextColours.ANSI_RED + "Erinevad võimalused" + TextColours.ANSI_RESET);
                System.out.print(
                                "Registreerimiseks kirjutage:        1" + "\r\n" +
                                "Sisse logimiseks kirjutage:         2" + "\r\n" +
                                "Programmi sulgemiseks kirjutage:    3" + "\r\n");
                System.out.println(
                                "-------------------------------------");
                System.out.print("Valige sobiv tegevus: ");
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
                                String[] possibleCommands = {"11", "12", "13", "14", "15", "16", "17", "18", "19"};
                                String[] commandsThatNeedList = {"13", "14", "15", "18"};
                                System.out.println(TextColours.ANSI_RED + "Erinevad võimalused" + TextColours.ANSI_RESET + "\r\n" +
                                        "Lisa ülesanne:                     11" + "\r\n" +
                                        "Vaata ülesandeid:                  12" + "\r\n" +
                                        "Lisa ülesandele kommentaar:        13" + "\r\n" +
                                        "Muuda ülesande tähtaega:           14" + "\r\n" +
                                        "Märgi ülesanne lõpetatuks:         15" + "\r\n" +
                                        "Lisa ülesanne teisele kasutajale:  16" + "\r\n" +
                                        "Otsi ülesannet:                    17" + "\r\n" +
                                        "Jälgi mingit ülesannet:            18" + "\r\n" +
                                        "Sulge programm:                    19");
                                System.out.println(
                                        "-------------------------------------");

                                System.out.print("Valige sobiv tegevus: ");
                                String command = scanner.nextLine();
                                System.out.println();

                                if (Arrays.asList(possibleCommands).contains(command)) {
                                    int commandInt = Integer.parseInt(command);

                                    if (commandInt != Commands.DO_CLOSE_TODO_LIST_2.getValue()) {

                                        int messageType = 0;
                                        if (Arrays.asList(commandsThatNeedList).contains(command)) {
                                            if (commandInt == (Commands.DO_FOLLOW_TASK.getValue())) {
                                                System.out.print("Sisesta kasutajanimi, kelle ülesannet jälgida tahad: ");
                                                String username = scanner.nextLine();
                                                out.writeInt(Commands.DO_DISPLAY_TASK_CERTAIN.getValue());
                                                out.writeUTF(username);
                                            } else {
                                                out.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                                            }
                                            //messageType loeb sisse, sest server saadab displayTasksi korral message type
                                            messageType = input.readInt();
                                            ClientProcessCommands.displayTasks(input, "Ülesanded:");
                                            //loeb siin ikkagi sisse booleani, kuigi see pole oluline, aga ma ei hakka uut meetodit tegema
                                            //kui saab kasutada displayTasksi
                                            boolean notImportant = input.readBoolean();

                                        }
                                        if(messageType == Commands.ERROR_OCCURED.getValue()) {
                                            System.out.println(input.readUTF());
                                            System.out.println();
                                        }
                                        else {
                                            processServerMessageType(input, out, commandInt);
                                        }

                                    } else {
                                        out.writeInt(Commands.DO_CLOSE_TODO_LIST_2.getValue());
                                    }
                                    if (input.readBoolean()) {
                                        System.out.println(TextColours.ANSI_RED + "Programm sulgub!" + TextColours.ANSI_RESET);
                                        return;
                                    }
                                }
                                //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
                                else {
                                    System.out.println(TextColours.ANSI_YELLOW + "Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17, 18)" + TextColours.ANSI_RESET);
                                }
                            }
                        }
                        break;
                    case "3":
                        out.writeInt(Commands.DO_CLOSE_TODO_LIST_1.getValue());
                        if (input.readBoolean()) {
                            System.out.println(TextColours.ANSI_RED + "Programm sulgub!" + TextColours.ANSI_RESET);
                            break label;
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

    private static void processServerMessageType(DataInputStream input, DataOutputStream out, int command) throws IOException {

        if (command == Commands.DO_COMPLETE_TASK.getValue()) {
            ClientProcessCommands.processCompleteTask(input, out);
        } else if (command == Commands.DO_DISPLAY_TASK.getValue()) {
            ClientProcessCommands.processDisplayTasks(input, out);
        } else if (command == Commands.DO_ADD_COMMENT.getValue()) {
            ClientProcessCommands.processAddComment(input, out);
        } else if (command == Commands.DO_PUSH_DEADLINE.getValue()) {
            ClientProcessCommands.processPushDeadline(input, out);
        } else if (command == Commands.DO_ADD_TASK.getValue()) {
            ClientProcessCommands.processAddTask(input, out);
        } else if (command == Commands.DO_ADD_TASK_TO_OTHER_USER.getValue()) {
            ClientProcessCommands.processAddTaskToOtherUsers(input, out);
        } else if (command == Commands.ERROR_OCCURED.getValue()) {
            ClientProcessCommands.processErrorOccured(input, out);
        } else if (command == Commands.DO_SEARCH_TASKS.getValue()) {
            ClientProcessCommands.processShowSearchedTasks(input, out);
        } else if (command == Commands.DO_FOLLOW_TASK.getValue()) {
            ClientProcessCommands.processFollowTask(input, out);
        }
    }
}