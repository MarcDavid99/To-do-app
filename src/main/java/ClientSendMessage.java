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

    public static void sendSearchTasks(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        while(true){
            System.out.println("21 - soovin taski otsida kirjelduse järgi\n" +
                    "22 - soovin taski otsida kasutajanime järgi\n" +
                    "23 - soovin taski otsida deadline'i järgi");
            System.out.print("Sisestage sobiva käsu number: ");
            String line = scanner.nextLine();
            try {
                int command = Integer.parseInt(line);
                if(command == Commands.doSearchTasksByDescription){
                    out.writeInt(Commands.doSearchTasksByDescription);
                    System.out.print("Sisestage kirjeldus, mille järgi te taski/taske otsida soovite: ");
                    String description = scanner.nextLine();
                    out.writeUTF(description);
                    System.out.println();
                    return;
                }
                else if(command == Commands.doSearchTasksByUsername){
                    out.writeInt(Commands.doSearchTasksByUsername);
                    System.out.print("Sisestage kasutajanimi, kelle taskide seast te soovite taski otsida: ");
                    String username = scanner.nextLine();
                    out.writeUTF(username);
                    System.out.println();
                    return;
                }
                else if(command == Commands.doSearchTasksByDeadline){
                    out.writeInt(Commands.doSearchTasksByDeadline);
                    System.out.print("Sisestage kuupäev, mille järgi te taski otsida soovite (kujul yyyy-MM-dd): ");
                    String deadline = scanner.nextLine();
                    out.writeUTF(deadline);
                    System.out.println();
                    return;
                }
                else{
                    System.out.println("\nTeie valitud käsk ei ole valikus. Proovige uuesti.\n");
                }
            } catch (NumberFormatException e) {
                System.out.println("\nTe ei sisestanud järjekorranumbrit õigel kujul.\n");
            }
        }
    }
}
