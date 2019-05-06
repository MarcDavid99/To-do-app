package Server;

import com.google.gson.Gson;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import shared.*;

public class ServerThreadTaskCommands {

    public static void deleteUser(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        Argon2 argon2 = Argon2Factory.create();

        boolean usernameExists = false;
        boolean passwordMatches = false;
        boolean deletingYourself = false;

        synchronized (sctx) {
            User userToDelete = allUsers.get(0);
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    if (user.equals(currentUser)) {
                        deletingYourself = true;
                        break;
                    }
                    usernameExists = true;
                    if (argon2.verify(user.getPassword(), password)) {
                        userToDelete = user;
                        passwordMatches = true;
                        break;
                    }
                }
            }
            if (deletingYourself) {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Iseennast ei või kustutada!");
            }
            else {
                if (usernameExists && passwordMatches) {
                    allUsers.remove(userToDelete);
                    sctx.setAllUsers(allUsers);
                    sctx.writeExistingUsersToFile();
                    socketOut.writeInt(Commands.DO_DELETE_USER.getValue());
                    socketOut.writeUTF("Kasutaja on edukalt kustutatud!");
                } else {
                    if (usernameExists) {
                        socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                        socketOut.writeUTF("Password on ebakorrektne!");
                    } else {
                        socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                        socketOut.writeUTF("Sellist kasutajat ei leidu!");
                    }
                }
            }
        }
        socketOut.writeBoolean(false);
    }

    public static boolean checkForUsernameInList(String username, ServerContext sctx, List<User> allUsers) throws IOException {
        boolean usernameAlreadyExists = false;
        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    usernameAlreadyExists = true;
                }
            }
        }
        return usernameAlreadyExists;
    }

    public static void followTask(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        int taskIndex = Integer.parseInt(socketIn.readUTF());
        boolean tryToFollowTask = false;
        User userWhoseTaskToFollow = null;

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    userWhoseTaskToFollow = user;
                    tryToFollowTask = true;
                }
            }
        }
        if (!tryToFollowTask) { // kasutajanime ei eksisteeri
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellist kasutajanime pole olemas.");
            socketOut.writeBoolean(false);
        }
        else {  // kasutajanimi eksisteerib
            try {
                System.out.println(currentUser);
                if (userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).getTaskFollowers().contains(currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Seda ülesannet sa juba jälgid.");
                    socketOut.writeBoolean(false);
                }

                else if(userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).isPrivateTask() && !userWhoseTaskToFollow.getUserID().equals(currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Ülesande jälgimine pole võimalik, sest see on privaatne.");
                    socketOut.writeBoolean(false);
                }
                else{
                    userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).addFollower(currentUser.getUserID());
                    socketOut.writeInt(Commands.DO_FOLLOW_TASK.getValue());
                    socketOut.writeUTF("Ülesande jälgimine toimis.");
                    socketOut.writeBoolean(false);
                }
            } catch (IndexOutOfBoundsException e) {
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Sellise indeksiga ülesannet ei eksisteeri.");
                socketOut.writeBoolean(false);
            }
        }

    }

    public static boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    public static void addComment(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            String comment = socketIn.readUTF();
            synchronized (sctx) {
                todoList.get(indeks).addComments(comment, allUsers);
            }
            socketOut.writeInt(Commands.DO_ADD_COMMENT.getValue());
            socketOut.writeUTF("Kommentaar lisatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }

        socketOut.writeBoolean(false);
    }

    public static void pushDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
        List<Task> todoList = currentUser.getToDoList();

        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            int pushDeadline = socketIn.readInt();
            synchronized (sctx) {
                todoList.get(indeks).setDeadline(pushDeadline, allUsers);
            }
            socketOut.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
            socketOut.writeUTF("Tähtaeg edasi lükatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    public static void addTaskToOtherUser(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
        String username = socketIn.readUTF();
        String description = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        if (checkForUsernameInList(username, sctx, allUsers)) {
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        String taskID = UUID.randomUUID().toString();
                        user.addTask(new Task(description, taskID, currentUser.getUserID(), user.getUserID(), isPrivateTask, topic));
                    }
                }
            }
            socketOut.writeInt(Commands.DO_ADD_TASK_TO_OTHER_USER.getValue());
            socketOut.writeUTF("Kasutajale " + username + " on lisatud ülesanne kirjeldusega " + description);
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);

    }

    public static void addTask(DataInputStream socketIn, DataOutputStream socketOut, User currentUser) throws IOException {
        String taskDescription = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        String taskID = UUID.randomUUID().toString();
        currentUser.addTask(new Task(taskDescription, taskID, currentUser.getUserID(), currentUser.getUserID(), isPrivateTask, topic));
        socketOut.writeInt(Commands.DO_ADD_TASK.getValue());
        socketOut.writeUTF("Ülesanne loodud.");
        socketOut.writeBoolean(false);
    }

    public static void displayTasks(DataOutputStream socketOut, User currentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoList, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void displayTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut, User currentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();
        for (Task task : currentUser.getToDoList()) {
            if (task.getTaskTopic().equals(topic)) {
                todoListByTopic.add(task);
            }
        }
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoListByTopic, socketOut, true, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void displayCertainUserTasks(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();

        synchronized (sctx) {
            for (User user : allUsers) {
                if (user.getUsername().equals(username)) {
                    todoList = user.getToDoList();
                }
            }
        }
        if (!checkForUsernameInList(username, sctx, allUsers)) {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellise kasutajanimega kasutajat ei leidu.");
        } else {
            socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
            sendTasks(todoList, socketOut, true, sctx, allUsers);
            socketOut.writeBoolean(false);
        }
    }


    public static void completeTask(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers, User currentUser) throws Exception {
        List<Task> todoList = currentUser.getToDoList();
        int indeks = socketIn.readInt() - 1;
        if (indeks >= 0 && indeks < todoList.size()) {
            synchronized (sctx) {
                todoList.get(indeks).setTaskFinished(allUsers);
            }
            todoList.remove(indeks);
            socketOut.writeInt(Commands.DO_COMPLETE_TASK.getValue());
            socketOut.writeUTF("Ülesanne edukalt eemaldatud");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers) throws IOException {
        String deadline = socketIn.readUTF();
        List<Task> suitableTasksArray = new ArrayList<>();

        synchronized (sctx) {
            for (User user : allUsers) {
                List<Task> todoList = user.getToDoList();
                for (Task task : todoList) {
                    if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline) && !task.isPrivateTask()) {
                        suitableTasksArray.add(task);
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasksArray, socketOut, false, sctx, allUsers);

        socketOut.writeBoolean(false);
    }

    public static void searchTaskByUsername(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();
        if (checkForUsernameInList(username, sctx, allUsers)) {
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUsername().equals(username)) {
                        todoList.addAll(user.getToDoList());
                        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
                        sendTasks(todoList, socketOut, false, sctx, allUsers);
                        break;
                    }
                }
            }
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByDescription(DataInputStream socketIn, DataOutputStream socketOut, ServerContext sctx, List<User> allUsers) throws IOException {
        List<Task> suitableTasks = new ArrayList<>();
        String description = socketIn.readUTF();

        synchronized (sctx) {
            for (User user : allUsers) {
                for (Task task : user.getToDoList()) {
                    if (task.getTaskDescription().contains(description) && !task.isPrivateTask()) {
                        suitableTasks.add(task);
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasks, socketOut, false, sctx, allUsers);
        socketOut.writeBoolean(false);
    }

    public static void sendTasks(List<Task> list, DataOutputStream socketOut, boolean justShowToCurrentUser, ServerContext sctx, List<User> allUsers) throws IOException {
        socketOut.writeInt(list.size());
        socketOut.writeBoolean(justShowToCurrentUser);
        for (Task task : list) {
            String taskCreator = "";
            String taskUser = "";
            synchronized (sctx) {
                for (User user : allUsers) {
                    if (user.getUserID().equals(task.getTaskCreatorID())) {
                        taskCreator = user.getUsername();
                    }
                    if (user.getUserID().equals(task.getTaskUserID())) {
                        taskUser = user.getUsername();
                    }
                }
            }
            Gson gson = new Gson();
            String jsonTask = gson.toJson(task);
            socketOut.writeUTF(jsonTask);
            socketOut.writeUTF(taskCreator);
            socketOut.writeUTF(taskUser);
        }
    }
}
