import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClientProcessCommands {
    public static void addComment(Scanner scanner, DataOutputStream out, DataInputStream input) throws IOException {
        //väljastab userile kõigepealt tasklisti
        int tasksAmount = input.readInt();
        int toDoListIndexes = input.readInt();
        System.out.println("Sinu to-do listis olevad taskid:");
        for (int i = 0; i < tasksAmount; i++) {
            System.out.println(input.readUTF());
        }
        System.out.println();

        int taskIndex = toDoListIndexes + 1;

        while (taskIndex > toDoListIndexes || taskIndex < 1) {
            System.out.print("Sisestage taski järjekorranumber, millele te kommentaari lisada soovite: ");
            String line = scanner.nextLine();
            try {
                taskIndex = Integer.parseInt(line);
                if (taskIndex > toDoListIndexes || taskIndex < 1) {
                    System.out.println("Vale järjekorranumber! Nii suure/väikse järjekorranumbriga taski teil listis ei ole.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }
        out.writeInt(taskIndex);

        System.out.print("Sisestage kommentaar: ");
        String comment = scanner.next();
        out.writeUTF(comment);

        System.out.println(input.readUTF() + "\r\n");
    }

    public static void doDisplayTasks(DataOutputStream out, DataInputStream input) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        System.out.println("Sinu to-do listis olevad taskid:");
        for (int i = 0; i < tasksAmount; i++) {
            System.out.println(input.readUTF());
        }
        System.out.println();
    }

    public static void completeTask(Scanner scanner, DataOutputStream out, DataInputStream input) throws IOException {
        //väljastab userile kõigepealt tasklisti
        int tasksAmount = input.readInt();
        int toDoListIndexes = input.readInt();
        System.out.println("Sinu to-do listis olevad taskid:");
        for (int i = 0; i < tasksAmount; i++) {
            System.out.println(input.readUTF());
        }
        System.out.println();

        int taskIndex = toDoListIndexes + 1;
        while (taskIndex > toDoListIndexes || taskIndex < 1) {
            System.out.print("Sisestage taski järjekorranumber, mida te eemaldada soovite: ");
            String line = scanner.nextLine();
            try {
                taskIndex = Integer.parseInt(line);
                if (taskIndex > toDoListIndexes || taskIndex < 1) {
                    System.out.println("Vale järjekorranumber! Nii suure/väikse järjekorranumbriga taski teil listis ei ole.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }
        out.writeInt(taskIndex);
        String message = input.readUTF();
        System.out.println(message + "\r\n");
    }

    public static void pushDeadline(Scanner scanner, DataOutputStream out, DataInputStream input) throws IOException {
        //väljastab userile kõigepealt tasklisti
        int tasksAmount = input.readInt();
        int toDoListIndexes = input.readInt();
        System.out.println("Sinu to-do listis olevad taskid:");
        for (int i = 0; i < tasksAmount; i++) {
            System.out.println(input.readUTF());
        }
        System.out.println();

        int taskIndex = toDoListIndexes + 1;
        while (taskIndex > toDoListIndexes || taskIndex < 1) {
            System.out.print("Sisestage taski järjekorranumber, mille deadline'i te muuta soovite: ");
            String line = scanner.nextLine();
            try {
                taskIndex = Integer.parseInt(line);
                if (taskIndex > toDoListIndexes || taskIndex < 1) {
                    System.out.println("Vale järjekorranumber! Nii suure/väikse järjekorranumbriga taski teil listis ei ole.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud järjekorranumbrit õigel kujul.");
            }
        }
        out.writeInt(taskIndex);

        while (true) {
            System.out.print("Sisestage päevade arv, mille võrra soovite deadline'i edasi lükata: ");
            String line = scanner.nextLine();
            try {
                int days = Integer.parseInt(line);
                out.writeInt(days);
                System.out.println(input.readUTF() + "\r\n");
                return;
            } catch (NumberFormatException e) {
                System.out.println("Te ei sisestanud päevade arvu korrektselt.");
            }
        }
    }

    public static void addTask(Scanner scanner, DataInputStream input, DataOutputStream out) throws IOException {
        System.out.print(input.readUTF());
        String taskDescription = scanner.nextLine();
        out.writeUTF(taskDescription);
        System.out.println(input.readUTF() + "\r\n");
    }

    public static void addTaskToOtherUsers(Scanner scanner, DataOutputStream out, DataInputStream input) throws IOException {
        while (true) { //Kontrollib kas kasutaja sisestatud kasutajanimi eksisteerib üldse
            System.out.print(input.readUTF());
            String enteredUsername = scanner.nextLine();
            out.writeUTF(enteredUsername);
            if (input.readBoolean()) {
                out.writeBoolean(true); //Saadab serverile conformationi, et tõepoolest eksisteerib kasutaja nimi ja võib edasi minna
                out.writeUTF(enteredUsername);
                break;
            }
            System.out.println("Sisestatud kasutajanime ei eksisteeri, proovi uuesti");
            out.writeBoolean(false);
        }

        System.out.print(input.readUTF());
        out.writeUTF(scanner.nextLine()); //Küsib kasutajalt mis ülesande kirjeldus on ja saadab serverile

    }
}
