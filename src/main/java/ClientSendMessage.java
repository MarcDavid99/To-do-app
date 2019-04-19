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
                System.out.print("Sisestage kommentaar: ");
                String comment = scanner.next();
                out.writeInt(Commands.DO_ADD_COMMENT.getValue());
                out.writeInt(taskIndex);
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
                out.writeInt(Commands.DO_COMPLETE_TASK.getValue());
                out.writeInt(taskIndex);
                return;
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }
    }

    public static void sendPushDeadline(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        int taskIndex;
        while (true) {
            System.out.print("Sisestage taski järjekorranumber, mille deadline'i te muuta soovite: ");
            String line = scanner.nextLine();
            try {
                taskIndex = Integer.parseInt(line);
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
                out.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
                out.writeInt(taskIndex);
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
        boolean isPrivateTask;
        while (true) {
            System.out.print("Kas soovite, et task oleks privaatne (jah/ei)? ");
            String privacy = scanner.nextLine();
            if (privacy.equals("jah")) {
                isPrivateTask = true;
                break;
            }
            else if (privacy.equals("ei")) {
                isPrivateTask = false;
                break;
            }
            System.out.println("Sisestus oli vigane, kirjutage (jah/ei)");
        }
        out.writeInt(Commands.DO_ADD_TASK.getValue());
        out.writeUTF(taskDescription);
        out.writeBoolean(isPrivateTask);
    }

    public static void sendAddTaskToOtherUsers(DataOutputStream out) throws IOException {
        Scanner scanner = new Scanner(System.in).useDelimiter("\\n");
        System.out.print("Sisestage kasutaja nimi, kellele tahate ülesande lisada: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Lisa ülesande kirjeldus: ");
        String description = scanner.nextLine();
        out.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
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
                if(command == Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue()){
                    System.out.print("Sisestage kirjeldus, mille järgi te taski/taske otsida soovite: ");
                    String description = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DESCRIPTION.getValue());
                    out.writeUTF(description);
                    System.out.println();
                    return;
                }
                else if(command == Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue()){
                    System.out.print("Sisestage kasutajanimi, kelle taskide seast te soovite taski otsida: ");
                    String username = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_USERNAME.getValue());
                    out.writeUTF(username);
                    System.out.println();
                    return;
                }
                else if(command == Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue()){
                    System.out.print("Sisestage kuupäev, mille järgi te taski otsida soovite (kujul yyyy-MM-dd): ");
                    String deadline = scanner.nextLine();
                    out.writeInt(Commands.DO_SEARCH_TASKS_BY_DEADLINE.getValue());
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
}
