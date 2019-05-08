package client;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import shared.*;

public class ClientProcessCommands {

    // TODO: Interface Commandide jaoks

    public static final String[] topicList = {"töö", "kodu", "vaba aeg", "kultuur", "söök"};

    public static boolean processDisplayTasks(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.println("" +
                    "Näita kõiki ülesandeid:        12" + "\r\n" +
                    "Näita ülesandeid teema järgi:  32" + "\r\n");
            System.out.print("Valige sobiv tegevus: ");
            String choice = scanner.nextLine();
            if(backToMainMenu(choice)){
                return true;
            }
            try {
                int command = Integer.parseInt(choice);
                if (command == Commands.DO_DISPLAY_TASK.getValue()) {
                    out.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                    break;
                } else if (command == Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue()) {
                    String topic;
                    System.out.print("Sisesta teema, mille järgi tahad ülesandeid vaadata: ");
                    while (true) {
                        topic = scanner.nextLine();
                        if(backToMainMenu(topic)){
                            return true;
                        }
                        if (Arrays.asList(topicList).contains(topic)) {
                            break;
                        }
                        System.out.print("Sellist teemat ei ole valikus, sisesta uuesti: ");
                    }
                    out.writeInt(Commands.DO_DISPLAY_TASK_BY_TOPIC.getValue());
                    out.writeUTF(topic);
                    System.out.println();
                    break;
                } else {
                    System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Teie valitud ülesanne ei ole valikus. Proovige uuesti." + TextColours.ANSI_RESET + "\r\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET + "\r\n");
            }
        }

        int commandType = input.readInt();

        displayTasks(input, "Sinu ülesanded: ");
        return false;
    }

    public static boolean processAddTask(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage ülesande kirjeldus: ");
        String taskDescription = scanner.nextLine();
        if(backToMainMenu(taskDescription)){
            return true;
        }
        boolean isPrivateTask = chooseIsPrivate();
        String topic = chooseTopic("Sisestage, mis teema alla see ülesanne kuulub (sõnadega, mitte järjekorranumbri abil): ");
        if(topic == null){
            return true;
        }
        out.writeInt(Commands.DO_ADD_TASK.getValue());
        out.writeUTF(taskDescription);
        out.writeBoolean(isPrivateTask);
        out.writeUTF(topic);

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        return false;
    }

    public static boolean processPushDeadline(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        int taskIndex;
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, mille tähtaega te muuta soovite: ");
            String line = scanner.nextLine();
            if(backToMainMenu(line)){
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
            System.out.println("Kui soovite tähtaega ette poole nihutada, kirjutage negatiivne arv, kui soovite edasi lükata, kirjutage positiivne arv.");
            System.out.print("Sisestage päevade arv, mille võrra soovite tähtaega muuta: ");
            String line = scanner.nextLine();
            if(backToMainMenu(line)){
                return true;
            }
            try {
                int days = Integer.parseInt(line);
                out.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
                out.writeInt(taskIndex);
                out.writeInt(days);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud päevade arvu korrektselt." + TextColours.ANSI_RESET);
            }
        }

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        return false;
    }

    public static boolean processCompleteTask(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, mida te eemaldada soovite: ");
            String line = scanner.nextLine();
            if(backToMainMenu(line)){
                return true;
            }
            try {
                int taskIndex = Integer.parseInt(line);
                out.writeInt(Commands.DO_COMPLETE_TASK.getValue());
                out.writeInt(taskIndex);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        return false;
    }

    public static boolean processAddTaskToOtherUsers(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage kasutajanimi, kellele tahate ülesande lisada: ");
        String enteredUsername = scanner.nextLine();
        if(backToMainMenu(enteredUsername)){
            return true;
        }
        System.out.print("Lisa ülesande kirjeldus: ");
        String description = scanner.nextLine();
        if(backToMainMenu(description)){
            return true;
        }
        boolean isPrivate = chooseIsPrivate();
        String topic = chooseTopic("Sisestage, mis teema alla see ülesanne kuulub (sõnadega, mitte järjekorranumbri abil): ");
        if(topic == null){
            return true;
        }
        out.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
        out.writeUTF(enteredUsername);
        out.writeUTF(description);
        out.writeBoolean(isPrivate);
        out.writeUTF(topic);

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        return false;
    }

    public static boolean processAddComment(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, millele te kommentaari lisada soovite: ");
            String line = scanner.nextLine();
            if(backToMainMenu(line)){
                return true;
            }
            try {
                int taskIndex = Integer.parseInt(line);
                System.out.print("Sisestage kommentaar: ");
                String comment = scanner.next();
                if(backToMainMenu(comment)){
                     return true;
                }
                out.writeInt(Commands.DO_ADD_COMMENT.getValue());
                out.writeInt(taskIndex);
                out.writeUTF(comment);
                break;

            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        return false;
    }

    public static void processErrorOccured(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static boolean processShowSearchedTasks(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.println("" +
                    "Soovin ülesannet otsida kirjelduse järgi:     22" + "\r\n" +
                    "Soovin ülesannet otsida kasutajanime järgi:   23" + "\r\n" +
                    "Soovin ülesannet otsida tähtaja järgi:        24" + "\r\n" +
                    "Soovin ülesannet otsida teema järgi:          25" + "\r\n");
            System.out.print("Valige sobiv tegevus: ");
            String line = scanner.nextLine();
            try {
                int command = Integer.parseInt(line);
                if (command == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()) {
                    System.out.print("Sisestage kirjeldus, mille järgi te ülesannet/ülesandeid otsida soovite: ");
                    String description = scanner.nextLine();
                    if(backToMainMenu(description)){
                        return true;
                    }
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue());
                    out.writeUTF(description);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()) {
                    System.out.print("Sisestage kasutajanimi, kelle ülesannete seast te otsida soovite: ");
                    String username = scanner.nextLine();
                    if(backToMainMenu(username)){
                        return true;
                    }
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue());
                    out.writeUTF(username);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()) {
                    System.out.print("Sisestage kuupäev, mille järgi te ülesannet otsida soovite (kujul yyyy-MM-dd): ");
                    String deadline = scanner.nextLine();
                    if(backToMainMenu(deadline)){
                        return true;
                    }
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue());
                    out.writeUTF(deadline);
                    System.out.println();
                    break;
                } else if (command == Commands.DO_SEARCH_TASKS_BY_TOPIC.getValue()) {
                    String topic = chooseTopic("Sisestage, mis teema all olevaid ülesandeid soovite otsida (sõnadega, mitte järjekorranumbri abil): ");
                    if(topic == null){
                        return true;
                    }
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_TOPIC.getValue());
                    out.writeUTF(topic);
                    break;
                } else {
                    System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Teie valitud ülesanne ei ole valikus. Proovige uuesti." + TextColours.ANSI_RESET + "\r\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET + "\r\n");
            }
        }
        int commandType = input.readInt();
        if (commandType == Commands.ERROR_OCCURED.getValue()) {
            processErrorOccured(input);
        } else {
            displayTasks(input, "Leitud ülesanded: ");
        }
        return false;
    }

    public static boolean processFollowTask(DataInputStream input, DataOutputStream out, String username) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisesta ülesande indeks, mida soovid jälgida: ");
        String taskIndex = scanner.nextLine();
        if(backToMainMenu(taskIndex)){
            return true;
        }
        try{
            int index = Integer.parseInt(taskIndex);
            out.writeInt(Commands.DO_FOLLOW_TASK.getValue());
            out.writeUTF(username);
            out.writeInt(index);

            int commandType = input.readInt();
            System.out.println(input.readUTF());
        }
        catch (NumberFormatException e){
            System.out.println(TextColours.ANSI_YELLOW + "Te ei sisastanud indeksit õigel kujul." + TextColours.ANSI_RESET);
            return true;
        }
        return false;
    }

    public static String chooseTopic(String messageToShow) {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.println("Võimalikud teemad, kuhu alla ülesanne kuuluda saab: ");
        int index = 1;
        String topic;
        for (String currentTopic : topicList) {
            System.out.println(index + ") " + currentTopic);
            index++;
        }
        while (true) {
            System.out.print(messageToShow);
            topic = scanner.next();
            if(topic.equals("")){
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

    public static void displayTasks(DataInputStream input, String messageToShow) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        boolean isForCurrentUser = input.readBoolean();

        if (tasksAmount == 0) {
            System.out.println("Ei ole ülesandeid, mida kuvada \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + messageToShow + TextColours.ANSI_RESET);
            for (int i = 0; i < tasksAmount; i++) {
                Gson gson = new Gson();
                String json = input.readUTF();
                String taskCreator = input.readUTF();
                String taskUser = input.readUTF();
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

    public static boolean processDeleteUser(DataInputStream input, DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");

        System.out.print("Sisestage kasutajanimi, mida soovite kustutada: ");
        String username = scanner.nextLine();
        if(backToMainMenu(username)){
            return true;
        }
        System.out.print("Sisestage kasutaja password: ");
        String password = scanner.nextLine();
        if(backToMainMenu(password)){
            return true;
        }

        out.writeInt(Commands.DO_DELETE_USER.getValue());
        out.writeUTF(username);
        out.writeUTF(password);

        int commandType = input.readInt();
        System.out.println(input.readUTF());
        System.out.println();
        return false;
    }

    public static boolean backToMainMenu(String string){
       return string.equals("");
    }
}
