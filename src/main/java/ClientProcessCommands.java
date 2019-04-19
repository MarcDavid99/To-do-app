import java.io.DataInputStream;
import java.io.IOException;

public class ClientProcessCommands {

    public static void processDisplayTasks(DataInputStream input) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        if (tasksAmount == 0) {
            System.out.println("Sul pole ühtegi aktiivset ülesannet. \r\n");
        } else {
            System.out.println("Sinu aktiivsed ülesanded:");
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
            System.out.println("Leitud ülesanded:");
            for (int i = 0; i < tasksAmount; i++) {
                System.out.println(input.readUTF());
            }
            System.out.println();
        }
    }

    public static void processFollowTask(DataInputStream input) throws IOException {
        int didItWork = input.readInt();
        if (didItWork == 0){
            System.out.println(input.readUTF());
        }else if (didItWork == 1){
            System.out.println("Ülesande jälgimine toimis.");
        }
    }
}
