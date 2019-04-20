import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class ClientSendMessage {

    public static final String[] topicList = {"töö", "kodu", "vaba aeg", "kultuur", "söök"};

    public static void sendAddComment(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, millele te kommentaari lisada soovite: ");
            String line = scanner.nextLine();
            try {
                int taskIndex = Integer.parseInt(line);
                System.out.print("Sisestage kommentaar: ");
                String comment = scanner.next();
                out.writeInt(Commands.DO_ADD_COMMENT.getValue());
                out.writeInt(taskIndex);
                out.writeUTF(comment);
                return;

            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

    }

    public static void sendCompleteTask(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, mida te eemaldada soovite: ");
            String line = scanner.nextLine();
            try {
                int taskIndex = Integer.parseInt(line);
                out.writeInt(Commands.DO_COMPLETE_TASK.getValue());
                out.writeInt(taskIndex);
                return;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }
    }

    public static void sendPushDeadline(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        int taskIndex;
        while (true) {
            System.out.print("Sisestage ülesande järjekorranumber, mille tähtaega te muuta soovite: ");
            String line = scanner.nextLine();
            try {
                taskIndex = Integer.parseInt(line);
                break;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET);
            }
        }

        while (true) {
            System.out.print("Sisestage päevade arv, mille võrra soovite tähtaega edasi lükata: ");
            String line = scanner.nextLine();
            try {
                int days = Integer.parseInt(line);
                out.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
                out.writeInt(taskIndex);
                out.writeInt(days);
                return;
            } catch (NumberFormatException e) {
                System.out.println(TextColours.ANSI_YELLOW + "Te ei sisestanud päevade arvu korrektselt." + TextColours.ANSI_RESET);
            }
        }
    }

    public static void sendAddTask(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage ülesande kirjeldus: ");
        String taskDescription = scanner.nextLine();
        boolean isPrivateTask = chooseIsPrivate();
        String topic = chooseTopic();
        out.writeInt(Commands.DO_ADD_TASK.getValue());
        out.writeUTF(taskDescription);
        out.writeBoolean(isPrivateTask);
        out.writeUTF(topic);
    }

    public static void sendAddTaskToOtherUsers(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage kasutaja nimi, kellele tahate ülesande lisada: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Lisa ülesande kirjeldus: ");
        String description = scanner.nextLine();
        boolean isPrivate = chooseIsPrivate();
        String topic = chooseTopic();
        out.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
        out.writeUTF(enteredUsername);
        out.writeUTF(description);
        out.writeBoolean(isPrivate);
        out.writeUTF(topic);

    }

    public static void sendSearchTasks(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while(true){
            System.out.println("" +
                    "Soovin ülesannet otsida kirjelduse järgi:     21" + "\r\n" +
                    "Soovin ülesannet otsida kasutajanime järgi:   22" + "\r\n" +
                    "Soovin ülesannet otsida tähtaja järgi:        23" + "\r\n");
            System.out.print("Valige sobiv tegevus: ");
            String line = scanner.nextLine();
            try {
                int command = Integer.parseInt(line);
                if(command == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()){
                    System.out.print("Sisestage kirjeldus, mille järgi te ülesannet/ülesandeid otsida soovite: ");
                    String description = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue());
                    out.writeUTF(description);
                    System.out.println();
                    return;
                }
                else if(command == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()){
                    System.out.print("Sisestage kasutajanimi, kelle ülesannete seast te otsida soovite: ");
                    String username = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue());
                    out.writeUTF(username);
                    System.out.println();
                    return;
                }
                else if(command == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()){
                    System.out.print("Sisestage kuupäev, mille järgi te ülesannet otsida soovite (kujul yyyy-MM-dd): ");
                    String deadline = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue());
                    out.writeUTF(deadline);
                    System.out.println();
                    return;
                }
                else{
                    System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Teie valitud ülesanne ei ole valikus. Proovige uuesti." + TextColours.ANSI_RESET + "\r\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\r\n" + TextColours.ANSI_YELLOW + "Te ei sisestanud järjekorranumbrit õigel kujul." + TextColours.ANSI_RESET + "\r\n");
            }
        }
    }

    public static void sendFollowTask(DataOutputStream out) throws IOException{
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisesta kasutajanimi, kelle ülesannet jälgida tahad: ");
        String username = scanner.nextLine();
        System.out.print("Sisesta ülesande indeks, mida soovid jälgida: ");
        String taskIndex = scanner.nextLine();
        out.writeInt(Commands.DO_FOLLOW_TASK.getValue());
        out.writeUTF(username);
        out.writeUTF(taskIndex);
    }

    public static String chooseTopic(){
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.println("Võimalikud teemad, kuhu alla ülesanne kuuluda saab: ");
        int index = 1;
        String topic;
        for (String currentTopic : topicList) {
            System.out.println(index + ") " + currentTopic);
            index++;
        }
        while(true){
            System.out.print("Sisestage, mis teema alla see ülesanne kuulub (sõnadega, mitte järjekorranumbri abil): ");
            topic = scanner.next();
            if(Arrays.asList(topicList).contains(topic)){
                break;
            }
            System.out.println(TextColours.ANSI_YELLOW + "Sellist teemat ei ole valikus. Proovi uuesti." + TextColours.ANSI_RESET);
            System.out.println();
        }
        return topic;
    }

    public static boolean chooseIsPrivate(){
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        boolean isPrivateTask;
        while (true) {
            System.out.print("Kas soovite, et ülesanne oleks privaatne (jah/ei)? ");
            String privacy = scanner.nextLine();
            if (privacy.equals("jah")) {
                isPrivateTask = true;
                break;
            }
            else if (privacy.equals("ei")) {
                isPrivateTask = false;
                break;
            }
            System.out.println(TextColours.ANSI_YELLOW + "Sisestus oli vigane, kirjutage (jah/ei)" + TextColours.ANSI_RESET);
        }
        return isPrivateTask;
    }
}
