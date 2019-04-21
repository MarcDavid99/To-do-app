import java.io.DataInputStream;
import java.io.IOException;

public class ClientProcessCommands {

    // TODO: Interface Commandide jaoks

    public static void processDisplayTasks(DataInputStream input) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        if (tasksAmount == 0) {
            System.out.println("Pole ühtegi aktiivset ülesannet. \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + "Aktiivsed ülesanded" + TextColours.ANSI_RESET);
            for (int i = 0; i < tasksAmount; i++) {
                System.out.println(input.readUTF());
            }
            System.out.println();
        }
    }

    public static void processAddTask(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processPushDeadline(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processCompleteTask(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processAddTaskToOtherUsers(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processAddComment(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processErrorOccured(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }

    public static void processShowSearchedTasks(DataInputStream input) throws IOException {
        int tasksAmount = input.readInt();
        if (tasksAmount == 0) {
            System.out.println("Ei leitud sinu otsingule vastavaid ülesandeid. \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + "Leitud ülesanded" + TextColours.ANSI_RESET);
            for (int i = 0; i < tasksAmount; i++) {
                System.out.println(input.readUTF());
            }
            System.out.println();
        }
    }

    public static void processFollowTask(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }
}
