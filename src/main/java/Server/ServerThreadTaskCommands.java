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

    public static void deleteUser(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        Argon2 argon2 = Argon2Factory.create();

        boolean usernameExists = false;
        boolean passwordMatches = false;
        boolean deletingYourself = false;
        User userToDelete = null;

        synchronized (serverThread.sctx) {
            userToDelete = serverThread.allUsers.get(0);
            for (User user : serverThread.allUsers) {
                if (user.getUsername().equals(username)) {
                    if (user.equals(serverThread.currentUser)) {
                        deletingYourself = true;
                        break;
                    } else {
                        usernameExists = true;
                        userToDelete = user;
                        break;
                    }
                }
            }
        }

        if (argon2.verify(userToDelete.getPassword(), password)) {
            passwordMatches = true;
        }

        boolean userAlreadyDeleted = false;
        if (usernameExists && passwordMatches && !deletingYourself) { // kasutajanimi ja password klapivad ja ei kustutata iseennast
            synchronized (serverThread.sctx) {
                if (serverThread.allUsers.contains(userToDelete)) { // kasutaja on veel kasutajate listis
                    serverThread.allUsers.remove(userToDelete);
                    serverThread.sctx.writeExistingUsersToFile();
                } else { // kasutaja on juba kustutatud
                    userAlreadyDeleted = true;
                }
            }
            if (userAlreadyDeleted) { // kasutaja on juba kustutatud
                socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                socketOut.writeUTF("Kasutaja on juba kustutatud!");
            } else { // kasutaja kustutati ära
                socketOut.writeInt(Commands.DO_DELETE_USER.getValue());
                socketOut.writeUTF("Kasutaja on edukalt kustutatud!");
            }
        } else { // kasutajanimi või password oli vale või üritati ennast kustutada
            if (deletingYourself) {
                socketOut.writeInt(Commands.DO_CLOSE_TODO_LIST_1.getValue()); // Main ei oska sellega tegelikult midagi peale hakata, aga tuleb saata ikka
                socketOut.writeUTF("Kustutasite oma kasutaja, teie sessioon katkeb sulgub!");
            } else { // ei üritatud ennast kustutada
                if (usernameExists) { // kasutajanimi eksisteeris
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Password on ebakorrektne!");
                } else { // kõik antud info oli vale
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Sellist kasutajat ei leidu!");
                }

            }
        }
        if (deletingYourself) {
            synchronized (serverThread.sctx) {
                serverThread.allUsers.remove(userToDelete);
                serverThread.sctx.writeExistingUsersToFile();
            }
            socketOut.writeBoolean(true); // Sulge to do list
        } else {
            socketOut.writeBoolean(false); // Ära sulge
        }
    }

    public static boolean checkForUsernameInList(String username, ServerThread serverThread) throws IOException {
        boolean usernameAlreadyExists = false;

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
                if (user.getUsername().equals(username)) {
                    usernameAlreadyExists = true;
                }
            }
        }

        return usernameAlreadyExists;
    }

    public static void followTask(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String username = socketIn.readUTF();
        int taskIndex = socketIn.readInt();
        boolean tryToFollowTask = false;
        User userWhoseTaskToFollow = null;

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
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
        } else {  // kasutajanimi eksisteerib
            if (userWhoseTaskToFollow.getToDoList().size() >= taskIndex) {
                if (userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).getTaskFollowers().contains(serverThread.currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Seda ülesannet sa juba jälgid.");
                    socketOut.writeBoolean(false);
                } else if (userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).isPrivateTask() && !userWhoseTaskToFollow.getUserID().equals(serverThread.currentUser.getUserID())) {
                    socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
                    socketOut.writeUTF("Ülesande jälgimine pole võimalik, sest see on privaatne.");
                    socketOut.writeBoolean(false);
                } else {
                    userWhoseTaskToFollow.getToDoList().get(taskIndex - 1).addFollower(serverThread.currentUser.getUserID());
                    socketOut.writeInt(Commands.DO_FOLLOW_TASK.getValue());
                    socketOut.writeUTF("Ülesande jälgimine toimis.");
                    socketOut.writeBoolean(false);
                }
            } else {
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

    public static void addComment(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws Exception {
        List<Task> todoList = serverThread.currentUser.getToDoList();
        int taskIndex = socketIn.readInt() - 1;
        if (taskIndex >= 0 && taskIndex < todoList.size()) {
            String comment = socketIn.readUTF();

            synchronized (serverThread.sctx) {
                todoList.get(taskIndex).addComments(comment, serverThread.allUsers);
            }

            socketOut.writeInt(Commands.DO_ADD_COMMENT.getValue());
            socketOut.writeUTF("Kommentaar lisatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }

        socketOut.writeBoolean(false);
    }

    public static void pushDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws Exception {
        List<Task> todoList = serverThread.currentUser.getToDoList();
        int taskIndex = socketIn.readInt() - 1;
        if (taskIndex >= 0 && taskIndex < todoList.size()) {
            int pushDeadline = socketIn.readInt();

            synchronized (serverThread.sctx) {
                todoList.get(taskIndex).setDeadline(pushDeadline, serverThread.allUsers);
            }

            socketOut.writeInt(Commands.DO_PUSH_DEADLINE.getValue());
            socketOut.writeUTF("Tähtaeg edasi lükatud.");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    public static void addTaskToOtherUser(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws Exception {
        String username = socketIn.readUTF();
        String description = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        if (checkForUsernameInList(username, serverThread)) {

            synchronized (serverThread.sctx) {
                for (User user : serverThread.allUsers) {
                    if (user.getUsername().equals(username)) {
                        String taskID = UUID.randomUUID().toString();
                        user.addTask(new Task(description, taskID, serverThread.currentUser.getUserID(), user.getUserID(), isPrivateTask, topic));
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

    public static void addTask(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String taskDescription = socketIn.readUTF();
        boolean isPrivateTask = socketIn.readBoolean();
        String topic = socketIn.readUTF();
        String taskID = UUID.randomUUID().toString();
        serverThread.currentUser.addTask(new Task(taskDescription, taskID, serverThread.currentUser.getUserID(), serverThread.currentUser.getUserID(), isPrivateTask, topic));
        socketOut.writeInt(Commands.DO_ADD_TASK.getValue());
        socketOut.writeUTF("Ülesanne loodud.");
        socketOut.writeBoolean(false);
    }

    public static void displayTasks(DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        List<Task> todoList = serverThread.currentUser.getToDoList();
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoList, socketOut, true, serverThread);
        socketOut.writeBoolean(false);
    }

    public static void displayTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();
        for (Task task : serverThread.currentUser.getToDoList()) {
            if (task.getTaskTopic().equals(topic)) {
                todoListByTopic.add(task);
            }
        }
        socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
        sendTasks(todoListByTopic, socketOut, true, serverThread);
        socketOut.writeBoolean(false);
    }

    public static void displayCertainUserTasks(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
                if (user.getUsername().equals(username)) {
                    todoList = user.getToDoList();
                }
            }
        }

        if (!checkForUsernameInList(username, serverThread)) {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sellise kasutajanimega kasutajat ei leidu.");
        } else {
            if (username.equals(serverThread.currentUser.getUsername())) {
                socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                sendTasks(todoList, socketOut, true, serverThread);
                socketOut.writeBoolean(false);
            } else {
                socketOut.writeInt(Commands.DO_DISPLAY_TASK.getValue());
                sendTasks(todoList, socketOut, false, serverThread);
                socketOut.writeBoolean(false);
            }
        }
    }


    public static void completeTask(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws Exception {
        List<Task> todoList = serverThread.currentUser.getToDoList();
        int taskIndex = socketIn.readInt() - 1;
        if (taskIndex >= 0 && taskIndex < todoList.size()) {

            synchronized (serverThread.sctx) {
                todoList.get(taskIndex).setTaskFinished(serverThread.allUsers);
            }

            todoList.remove(taskIndex);
            socketOut.writeInt(Commands.DO_COMPLETE_TASK.getValue());
            socketOut.writeUTF("Ülesanne edukalt eemaldatud");
        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud järjekorranumbriga ülesannet sinu todo listis ei leidu.");
        }
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByDeadline(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String deadline = socketIn.readUTF();
        List<Task> suitableTasksArray = new ArrayList<>();

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
                List<Task> todoList = user.getToDoList();
                if (user.equals(serverThread.currentUser)) { //kui tegu on kasutaja endaga, siis tema private taske võib kuvada
                    for (Task task : todoList) {
                        if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline)) {
                            suitableTasksArray.add(task);
                        }
                    }
                } else {
                    for (Task task : todoList) {
                        if (task.getTaskDeadline().getDeadlineDate().toString().equals(deadline) && !task.isPrivateTask()) { //võõra private taske ei tohiks kuvada
                            suitableTasksArray.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasksArray, socketOut, true, serverThread);

        socketOut.writeBoolean(false);
    }

    public static void searchTaskByUsername(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String username = socketIn.readUTF();
        List<Task> todoList = new ArrayList<>();
        if (checkForUsernameInList(username, serverThread)) {

            synchronized (serverThread.sctx) {
                for (User user : serverThread.allUsers) {
                    if (user.getUsername().equals(username)) {
                        todoList.addAll(user.getToDoList());
                        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
                        if (user.equals(serverThread.currentUser)) {
                            sendTasks(todoList, socketOut, true, serverThread);
                            break;
                        } else {
                            sendTasks(todoList, socketOut, false, serverThread);
                            break;
                        }
                    }
                }
            }

        } else {
            socketOut.writeInt(Commands.ERROR_OCCURED.getValue());
            socketOut.writeUTF("Sisestatud kasutajanime ei eksisteeri, proovi uuesti.");
        }
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByDescription(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        List<Task> suitableTasks = new ArrayList<>();
        String description = socketIn.readUTF();

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
                if (user.equals(serverThread.currentUser)) {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskDescription().contains(description)) {
                            suitableTasks.add(task);
                        }
                    }
                } else {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskDescription().contains(description) && !task.isPrivateTask()) {
                            suitableTasks.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(suitableTasks, socketOut, true, serverThread);
        socketOut.writeBoolean(false);
    }

    public static void searchTaskByTopic(DataInputStream socketIn, DataOutputStream socketOut, ServerThread serverThread) throws IOException {
        String topic = socketIn.readUTF();
        List<Task> todoListByTopic = new ArrayList<>();

        synchronized (serverThread.sctx) {
            for (User user : serverThread.allUsers) {
                if (serverThread.currentUser.equals(user)) {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskTopic().equals(topic)) {
                            todoListByTopic.add(task);
                        }
                    }
                } else {
                    for (Task task : user.getToDoList()) {
                        if (task.getTaskTopic().equals(topic) && !task.isPrivateTask()) {
                            todoListByTopic.add(task);
                        }
                    }
                }
            }
        }

        socketOut.writeInt(Commands.DO_SEARCH_TASKS.getValue());
        sendTasks(todoListByTopic, socketOut, true, serverThread);
        socketOut.writeBoolean(false);
    }

    public static void sendTasks(List<Task> list, DataOutputStream socketOut, boolean justShowToCurrentUser, ServerThread serverThread) throws IOException {
        socketOut.writeInt(list.size());
        socketOut.writeBoolean(justShowToCurrentUser);
        for (Task task : list) {
            String taskCreator = "";
            String taskUser = "";

            synchronized (serverThread.sctx) {
                for (User user : serverThread.allUsers) {
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
