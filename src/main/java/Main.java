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
                Scanner scanner = new Scanner(System.in);
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
                                    commandToServer(out, command);
                                    if (!command.equals("17")) {
                                        int messageTypeFromServer = input.readInt();
                                        processCommand(input, out, messageTypeFromServer);
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

    private static void processCommand(DataInputStream input, DataOutputStream out, int messageTypeFromServer) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        if (messageTypeFromServer == Commands.doCompleteTask) {
            ClientProcessCommands.completeTask(scanner, out, input);
        }
        else if (messageTypeFromServer == Commands.doDisplayTasks) {
            ClientProcessCommands.doDisplayTasks(out, input);
        }
        else if (messageTypeFromServer == Commands.doAddComment) {
            ClientProcessCommands.addComment(scanner, out, input);
        }
        else if (messageTypeFromServer == Commands.doPushDeadline) {
            ClientProcessCommands.pushDeadline(scanner, out, input);
        }
        else if (messageTypeFromServer == Commands.doAddTask) {
            ClientProcessCommands.addTask(scanner, input, out);
        }
        else if (messageTypeFromServer == Commands.doAddTaskToOtherUser) {
            ClientProcessCommands.addTaskToOtherUsers(scanner, out, input);
        }
    }

    private static void commandToServer(DataOutputStream socketOut, String command) throws IOException {
        int commandAsInt = Integer.parseInt(command);
        socketOut.writeInt(commandAsInt);
    }

}