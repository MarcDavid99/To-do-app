package client;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import shared.*;

public class ClientProcessCommands {

    public static final String[] topicList = {"töö", "kodu", "vaba aeg", "üritused", "tervis", "varia"};

    public static boolean processDisplayTasks(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.println("" +
                    "Kõik ülesanded:                    12" + "\r\n" +
                    "Ülesanded teema järgi:             32" + "\r\n" +
                    "-------------------------------------");
            System.out.print("Valige sobiv tegevus: ");
            String choice = scanner.nextLine();
            if (backToMainMenu(choice)) {
                return true;
            }
            try {
                int command = Integer.parseInt(choice);
                if (command == Commands.DO_DISPLAY_TASK.getValue()) {
                    socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                    break;
                } else if (command == Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue()) {
                    String topic;
                    System.out.print("Sisestage teema, mille järgi tahad ülesandeid vaadata: ");
                    while (true) {
                        topic = scanner.nextLine();
                        if (backToMainMenu(topic)) {
                            return true;
                        }
                        if (Arrays.asList(topicList).contains(topic)) {
                            break;
                        }
                        System.out.print("Sellist teemat ei ole valikus, sisesta uuesti: ");
                    }
                    socketOut.writeInt(Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue());
                    socketOut.writeUTF(topic);
                    System.out.println();
                    break;
                } else {
                    System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Teie valitud ülesanne ei ole valikus. Proovige uuesti." + TextColours.ANSI_RESET + "\r\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET + "\r\n");
            }
        }

        int commandType = socketIn.readInt();

        displayTasks(socketIn, "Sinu ülesanded: ");
        return false;
    }

    public static boolean processAddTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Ülesande kijeldus: ");
        String taskDescription = scanner.nextLine();
        if (backToMainMenu(taskDescription)) {
            return true;
        }
        boolean isPrivateTask = chooseIsPrivate();
        String topic = chooseTopic("Ülesande teema: ");
        if (topic == null) {
            return true;
        }
        socketOut.writeInt(Commands.DO_ADD_TASK.getValue());
        socketOut.writeUTF(taskDescription);
        socketOut.writeBoolean(isPrivateTask);
        socketOut.writeUTF(topic);

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        return false;
    }

    public static boolean processPushDeadline(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        int taskIndex;
        while (true) {
            System.out.print("Ülesande järjekorranumber, mille tähtaega te muuta soovite: ");
            String line = scanner.nextLine();
            if (backToMainMenu(line)) {
                return true;
            }
            try {
                taskIndex = Integer.parseInt(line);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        while (true) {
            System.out.println(TextColours.ANSI_PURPLE + "Kui soovite tähtaega ette poole nihutada, kirjutage negatiivne arv, kui soovite edasi lükata, kirjutage positiivne arv." + TextColours.ANSI_RESET);
            System.out.print("Sisestage päevade arv, mille võrra soovite tähtaega muuta: ");
            String adjustDeadlineDays = scanner.nextLine();
            if (backToMainMenu(adjustDeadlineDays)) {
                return true;
            }
            try {
                int days = Integer.parseInt(adjustDeadlineDays);
                socketOut.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
                socketOut.writeInt(taskIndex);
                socketOut.writeInt(days);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud päevade arvu korrektselt." + TextColours.ANSI_RESET);
            }
        }

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        return false;
    }

    public static boolean processCompleteTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Ülesande järjekorranumber, mida te eemaldada soovite: ");
            String taskNumber = scanner.nextLine();
            if (backToMainMenu(taskNumber)) {
                return true;
            }
            try {
                int taskIndex = Integer.parseInt(taskNumber);
                socketOut.writeInt(Commands.DO_COMPLETE_TASK.getValue());
                socketOut.writeInt(taskIndex);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        return false;
    }

    public static boolean processAddTaskToOtherUsers(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Kasutajanimi, kellele tahate ülesande lisada: ");
        String username = scanner.nextLine();
        if (backToMainMenu(username)) {
            return true;
        }
        System.out.print("Ülesande kirjeldus: ");
        String description = scanner.nextLine();
        if (backToMainMenu(description)) {
            return true;
        }
        boolean isPrivate = chooseIsPrivate();
        String topic = chooseTopic("Sisestage, mis teema alla see ülesanne kuulub: ");
        if (topic == null) {
            return true;
        }
        socketOut.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
        socketOut.writeUTF(username);
        socketOut.writeUTF(description);
        socketOut.writeBoolean(isPrivate);
        socketOut.writeUTF(topic);

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        return false;
    }

    public static boolean processAddComment(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Ülesande järjekorranumber, millele te kommentaari lisada soovite: ");
            String taskNumber = scanner.nextLine();
            if (backToMainMenu(taskNumber)) {
                return true;
            }
            try {
                int taskIndex = Integer.parseInt(taskNumber);
                System.out.print("Kommentaar: ");
                String comment = scanner.next();
                if (backToMainMenu(comment)) {
                    return true;
                }
                socketOut.writeInt(Commands.DO_ADD_COMMENT.getValue());
                socketOut.writeInt(taskIndex);
                socketOut.writeUTF(comment);
                break;

            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        return false;
    }

    public static void processErrorOccured(DataInputStream socketIn) throws IOException {
        System.out.println(socketIn.readUTF());
        System.out.println();
    }

    public static boolean processShowSearchedTasks(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.println("" +
                    "Otsi kirjelduse järgi:             22" + "\r\n" +
                    "Otsi kasutajanime järgi:           23" + "\r\n" +
                    "Otsi tähtaja järgi:                24" + "\r\n" +
                    "Otsi teema järgi:                  25" + "\r\n" +
                    "-------------------------------------");
            System.out.print("Valige tegevus: ");
            String choice = scanner.nextLine();
            if (backToMainMenu(choice)) {
                return true;
            }
            try {
                int command = Integer.parseInt(choice);
                if (command == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()) {
                    System.out.print("Kirjeldus, mille järgi ülesandeid otsida soovite: ");
                    String description = scanner.nextLine();
                    if (backToMainMenu(description)) {
                        return true;
                    }
                    socketOut.writeInt(Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue());
                    socketOut.writeUTF(description);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()) {
                    System.out.print("Kasutajanimi, kelle ülesannete seast te otsida soovite: ");
                    String username = scanner.nextLine();
                    if (backToMainMenu(username)) {
                        return true;
                    }
                    socketOut.writeInt(Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue());
                    socketOut.writeUTF(username);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()) {
                    System.out.print("Kuupäev, mille järgi ülesandeid otsida soovite (kujul yyyy-MM-dd): ");
                    String deadline = scanner.nextLine();
                    if (backToMainMenu(deadline)) {
                        return true;
                    }
                    socketOut.writeInt(Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue());
                    socketOut.writeUTF(deadline);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_TOPIC.getValue()) {
                    String topic = chooseTopic("Sisestage teema, mille järgi ülesandeid otsida soovite: ");
                    if (topic == null) {
                        return true;
                    }
                    socketOut.writeInt(Commands.DO_SEARCH_TASKS_BY_TOPIC.getValue());
                    socketOut.writeUTF(topic);
                    break;
                } else {
                    System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Sellist valikut ei ole. Proovige uuesti." + TextColours.ANSI_RESET + "\r\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET + "\r\n");
            }
        }
        int commandType = socketIn.readInt();
        if (commandType == Commands.ERROR_OCCURED.getValue()) {
            processErrorOccured(socketIn);
        } else {
            displayTasks(socketIn, "Leitud ülesanded: ");
        }
        return false;
    }

    public static boolean processFollowTask(DataInputStream socketIn, DataOutputStream socketOut, String username) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Ülesande indeks, mida soovid jälgida: ");
        String taskNumber = scanner.nextLine();
        if (backToMainMenu(taskNumber)) {
            return true;
        }
        try {
            int taskIndex = Integer.parseInt(taskNumber);
            socketOut.writeInt(Commands.DO_FOLLOW_TASK.getValue());
            socketOut.writeUTF(username);
            socketOut.writeInt(taskIndex);

            int commandType = socketIn.readInt();
            System.out.println(socketIn.readUTF());
        } catch (NumberFormatException e) {
            System.out.println(TextColours.ANSI_YELLOW + "Te ei sisastanud indeksit õigel kujul." + TextColours.ANSI_RESET);
            return true;
        }
        return false;
    }

    public static String chooseTopic(String messageToShow) {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.println("Võimalikud teemad, kuhu alla ülesanne kuuluda saab: \r\n" + Arrays.asList(topicList));
        String topic;
        while (true) {
            System.out.print(messageToShow);
            topic = scanner.nextLine();
            if (topic.equals("")) {
                return null;
            }
            if (Arrays.asList(topicList).contains(topic)) {
                break;
            }
            System.out.println(TextColours.ANSI_YELLOW + "Sellist teemat ei ole valikus. Proovi uuesti." + TextColours.ANSI_RESET);
            System.out.println();
        }
        return topic;
    }

    public static boolean chooseIsPrivate() {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        boolean isPrivateTask;
        while (true) {
            System.out.print("Kas soovite, et ülesanne oleks privaatne (jah/ei)? ");
            String privacy = scanner.nextLine();
            if (privacy.equals("jah")) {
                isPrivateTask = true;
                break;
            } else if (privacy.equals("ei")) {
                isPrivateTask = false;
                break;
            }
            System.out.println(TextColours.ANSI_YELLOW + "Sisestus oli vigane, kirjutage (jah/ei)" + TextColours.ANSI_RESET);
        }
        return isPrivateTask;
    }

    public static void displayTasks(DataInputStream socketIn, String messageToShow) throws IOException {
        int tasksAmount = socketIn.readInt();
        boolean isForCurrentUser = socketIn.readBoolean();

        if (tasksAmount == 0) {
            System.out.println("Ei ole ülesandeid, mida kuvada \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + messageToShow + TextColours.ANSI_RESET);
            for (int i = 0; i < tasksAmount; i++) {
                Gson gson = new Gson();
                String json = socketIn.readUTF();
                String taskCreator = socketIn.readUTF();
                String taskUser = socketIn.readUTF();
                Task task = gson.fromJson(json, Task.class);
                if (task.isPrivateTask() && !isForCurrentUser) {
                    System.out.println(i + 1 + ") ülesanne on privaatne");
                } else {
                    System.out.println(i + 1 + ") " + task.getTaskDescription());
                    if (task.getComments().size() == 1) {
                        System.out.println("   *Kommentaar: " + task.getComments().get(0));
                    } else {
                        int commentNumber = 1;
                        for (String comment : task.getComments()) {
                            if (commentNumber == 1) {
                                System.out.println("   *Kommentaarid:" + "\r\n" +
                                        "      " + commentNumber + ". " + comment);
                            } else {
                                System.out.println("      " + commentNumber + ". " + comment);
                            }
                            commentNumber += 1;
                        }
                    }
                    System.out.println("   *Tähtaeg: " + task.getTaskDeadline().getDeadlineDate());
                    System.out.println("   *Teema: " + task.getTaskTopic());
                    System.out.println("   *Looja: " + taskCreator);
                    System.out.println("   *Täitja: " + taskUser);
                }
            }
            System.out.println();
        }
    }

    public static boolean processDeleteUser(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");

        System.out.print("Kasutajanimi, mida soovite kustutada: ");
        String username = scanner.nextLine();
        if (backToMainMenu(username)) {
            return true;
        }
        System.out.print("Kasutaja password: ");
        String password = scanner.nextLine();
        if (backToMainMenu(password)) {
            return true;
        }

        socketOut.writeInt(Commands.DO_DELETE_USER.getValue());
        socketOut.writeUTF(username);
        socketOut.writeUTF(password);

        int commandType = socketIn.readInt();
        System.out.println(socketIn.readUTF());
        System.out.println();
        return false;
    }

    public static boolean backToMainMenu(String string) {
        return string.equals("");
    }
}
