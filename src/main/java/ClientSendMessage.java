import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class ClientSendMessage {
    public static void sendAddComment(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage taski järjekorranumber, millele te kommentaari lisada soovite: ");
            String line = scanner.nextLine();
            try {
                int taskIndex = Integer.parseInt(line);
                out.writeInt(Commands.doAddComment);
                out.writeInt(taskIndex);
                System.out.print("Sisestage kommentaar: ");
                String comment = scanner.next();
                out.writeUTF(comment);
                return;

            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }

    }

    public static void sendCompleteTask(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage taski järjekorranumber, mida te eemaldada soovite: ");
            String line = scanner.nextLine();
            try {
                int taskIndex = Integer.parseInt(line);
                out.writeInt(Commands.doCompleteTask);
                out.writeInt(taskIndex);
                return;
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }
    }

    public static void sendPushDeadline(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while (true) {
            System.out.print("Sisestage taski järjekorranumber, mille deadline'i te muuta soovite: ");
            String line = scanner.nextLine();
            try {
                int taskIndex = Integer.parseInt(line);
                out.writeInt(Commands.doPushDeadline);
                out.writeInt(taskIndex);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }

        while (true) {
            System.out.print("Sisestage päevade arv, mille võrra soovite deadline'i edasi lükata: ");
            String line = scanner.nextLine();
            try {
                int days = Integer.parseInt(line);
                out.writeInt(days);
                return;
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud päevade arvu korrektselt.");
            }
        }
    }

    public static void sendAddTask(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage taski kirjeldus: ");
        String taskDescription = scanner.nextLine();
        out.writeInt(Commands.doAddTask);
        out.writeUTF(taskDescription);
    }

    public static void sendAddTaskToOtherUsers(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage kasutaja nimi, kellele tahate ülesande lisada: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Lisa ülesande kirjeldus: ");
        String description = scanner.nextLine();
        out.writeInt(Commands.doAddTaskToOtherUser);
        out.writeUTF(enteredUsername);
        out.writeUTF(description);
    }
}
