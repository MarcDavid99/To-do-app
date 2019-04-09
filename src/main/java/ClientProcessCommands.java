import java.io.DataInputStream;
import java.io.IOException;

public class ClientProcessCommands {

    public static void processDisplayTasks(DataInputStream input) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        if (tasksAmount == 0) {
            System.out.println("Sul pole ühtegi aktiivset taski. \r\n");
        }
        else {
            System.out.println("Sinu aktiivsed taskid:");
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

}
