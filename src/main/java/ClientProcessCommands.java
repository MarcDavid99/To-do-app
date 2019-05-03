import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.IOException;

public class ClientProcessCommands {

    // TODO: Interface Commandide jaoks

    public static void processDisplayTasks(DataInputStream input) throws IOException {
        //task listi kuvamine kasutajale
        int tasksAmount = input.readInt();
        boolean isForCurrentUser = input.readBoolean();

        if (tasksAmount == 0) {
            System.out.println("Sul pole ühtegi aktiivset ülesannet. \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + "Sinu aktiivsed ülesanded:" + TextColours.ANSI_RESET);
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
        boolean isForCurrentUser = input.readBoolean();

        if (tasksAmount == 0) {
            System.out.println("Sul pole ühtegi aktiivset ülesannet. \r\n");
        } else {
            System.out.println(TextColours.ANSI_RED + "Leitud ülesanded:" + TextColours.ANSI_RESET);
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

    public static void processFollowTask(DataInputStream input) throws IOException {
        System.out.println(input.readUTF());
        System.out.println();
    }
}
