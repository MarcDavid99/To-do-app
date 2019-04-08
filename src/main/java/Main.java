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
                System.out.print("Kui soovite registreerida kasutajat, kirjutage 1" + "\r\n" +
                        "Kui teil on kasutaja olemas, kirjutage 2" + "\r\n" +
                        "Kui soovite programmi sulgeda, kirjutage 3" + "\r\n" +
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
                                String[] possibleCommands = {"11", "12", "13", "14", "15", "16", "17"};
                                String[] commandsThatNeedList = {"13", "14", "15"};
                                System.out.println("Erinevad võimalused: " + "\r\n" +
                                        "11 - lisa ülesanne" + "\r\n" +
                                        "12 - vaata ülesandeid" + "\r\n" +
                                        "13 - lisa ülesandele kommentaar" + "\r\n" +
                                        "14 - muuda ülesande deadline'i" + "\r\n" +
                                        "15 - märgi ülesanne lõpetatuks" + "\r\n" +
                                        "16 - lisa ülesanne teisele kasutajale" + "\r\n" +
                                        "17 - sulge programm" + "\r\n");


                                System.out.print("Valige sobiv tegevus: ");
                                String command = scanner.nextLine();
                                System.out.println();
                                if (Arrays.asList(possibleCommands).contains(command)) {
                                    int commandInt = Integer.parseInt(command);
                                    if (commandInt != Commands.doCloseTodoList2) {
                                        if (Arrays.asList(commandsThatNeedList).contains(command)) {
                                            out.writeInt(Commands.doDisplayTasks);
                                            //messageType loeb sisse, sest server saadab displayTasksi korral message type
                                            int messageType = input.readInt();
                                            ClientProcessCommands.processDisplayTasks(input);
                                            //loeb siin ikkagi sisse booleani, kuigi see pole oluline, aga ma ei hakka uut meetodit tegema
                                            //kui saab kasutada displayTasksi
                                            boolean notImportant = input.readBoolean();
                                        }
                                        //serverile vajaliku info saatmine
                                        commandToServer(out, input, commandInt, scanner);

                                        //serverilt saadud info vastuvõtmine
                                        int serverMessageType = input.readInt();
                                        processServerMessageType(out, input, serverMessageType, scanner);

                                    } else {
                                        out.writeInt(Commands.doCloseTodoList2);
                                    }
                                    if (input.readBoolean()) {
                                        System.out.println("Programm sulgub");
                                        return;
                                    }
                                }
                                //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
                                else {
                                    System.out.println("Sisestage korrektne käsk (11, 12, 13, 14, 15, 16, 17)");
                                }
                            }
                        }
                        break;
                    case "3":
                        out.writeInt(Commands.doCloseTodoList1);
                        if (input.readBoolean()) {
                            System.out.println("Programm sulgub!");
                            break label;
                        }
                        break;
                    //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                    default:
                        System.out.println("Sisestage korrektne käsk (1, 2 või 3)");
                        break;
                }
            }
        }

    }


    private static void commandToServer(DataOutputStream out, DataInputStream input, int command, Scanner scanner) throws IOException {
        if (command == Commands.doCompleteTask) {
            ClientSendMessage.sendCompleteTask(out);
        } else if (command == Commands.doDisplayTasks) {
            out.writeInt(Commands.doDisplayTasks);
        } else if (command == Commands.doAddComment) {
            ClientSendMessage.sendAddComment(out);
        } else if (command == Commands.doPushDeadline) {
            ClientSendMessage.sendPushDeadline(out);
        } else if (command == Commands.doAddTask) {
            ClientSendMessage.sendAddTask(out);
        } else if (command == Commands.doAddTaskToOtherUser) {
            ClientSendMessage.sendAddTaskToOtherUsers(out, input);
        }
    }

    private static void processServerMessageType(DataOutputStream out, DataInputStream input, int command, Scanner scanner) throws IOException {
        if (command == Commands.doCompleteTask) {
            ClientProcessCommands.processCompleteTask(input);
        } else if (command == Commands.doDisplayTasks) {
            ClientProcessCommands.processDisplayTasks(input);
        } else if (command == Commands.doAddComment) {
            ClientProcessCommands.processAddComment(input);
        } else if (command == Commands.doPushDeadline) {
            ClientProcessCommands.processPushDeadline(input);
        } else if (command == Commands.doAddTask) {
            ClientProcessCommands.processAddTask(input);
        } else if (command == Commands.doAddTaskToOtherUser) {
            ClientProcessCommands.processAddTaskToOtherUsers(input);
        } else if (command == Commands.errorOccured) {
            ClientProcessCommands.processErrorOccured(input);
        }
    }
}