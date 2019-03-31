import com.google.gson.Gson;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerThread implements Runnable {

    //hetkel on serverthreadis jama, kui nt pärast taski loomist user programmi sulgeb
    //ilmselt on faili kirjutamisega probleem, sest errorid gsoniga seotud ja pärast seda
    //on user.txt file täiesti tühi

    private final Socket socket;
    private User currentUser;
    private Argon2 argon2 = Argon2Factory.create();

    private List<User> allUsers = new ArrayList<>();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Uue kliendi jaoks luuakse uus thread");

            readExistingUsersFromFile();

            boolean closeProgramme;
            while (true) {
                System.out.println("ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out);
                if (closeProgramme) {

                    writeExistingUsersToFile();

                    System.out.println(allUsers);
                    System.out.println("ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void readExistingUsersFromFile() throws IOException {
        try {
            Path pathToFile = Path.of("users.txt");
            List<String> users = Files.readAllLines(pathToFile);

            for (String user : users) {
                String json = user;
                System.out.println(json);
                Gson gson = new Gson();
                User newUser = gson.fromJson(json, User.class);
                allUsers.add(newUser);
            }
        }
        //peaks siin exceptionid läbi mõtlema, hetkel lihtsalt tühi plokk
        catch (Exception e) {
        }

        //Niisama abiks üleval ja all oleva koodi jaoks
        //Gson gsonUser = new Gson();
        //String jsonUser = gsonUser.toJson(newUser);

        //String json = socketIn.readUTF();
        //Gson gson = new Gson();
        //User newUser = gson.fromJson(json, User.class);
    }

    private void writeExistingUsersToFile() throws IOException {
        FileWriter fileWriter = new FileWriter("users.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);

        for (User registeredUser : allUsers) {
            Gson gson = new Gson();
            String jsonUser = gson.toJson(registeredUser);
            printWriter.println(jsonUser);
        }
        printWriter.close();
        fileWriter.close();
    }

    private boolean detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        int requestType = socketIn.readInt();
        //Taskidega seotud käsud võiks alata 11-st
        //Kasutajaga seotud käsud võiks alata 91-st
        //91 on kasutaja loomine
        if (requestType == Commands.doSaveNewUser) {
            saveNewUser(socketIn);
        }
        if (requestType == Commands.doVerifyClient) {
            verifyClient(socketIn, socketOut);
        }
        if (requestType == Commands.doCheckForUsername) {
            checkForUsernameInList(socketIn, socketOut);
        }
        if(requestType == Commands.doAddTask){
            addTask(socketIn, socketOut);
        }
        if(requestType == Commands.doDisplayTasks){
            //vaata ülesandeid
            displayTasks(socketIn, socketOut);
        }
        if(requestType == Commands.doEditTask){
            //muuda ülesandeid
            editTask(socketIn, socketOut);
        }
        if(requestType == Commands.doCompleteTask){
            //märgi ülesanne lõpetatuks
            completeTask(socketIn, socketOut);
        }

        if (requestType == Commands.doCloseTodoList1 || requestType == Commands.doCloseTodoList2) {
            closeTodoList(socketIn, socketOut);
            return true;
        }
        return false;
    }


    private boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    private void saveNewUser(DataInputStream socketIn) throws IOException {
        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        //Server.getRegisteredUsers().add(newUser);
        allUsers.add(newUser);
        writeExistingUsersToFile();
    }

    private void verifyClient(DataInputStream socketIn, DataOutputStream socketOut) throws Exception {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        boolean responseSent = false;



        for (User user : allUsers) {
            if (user.getUsername().equals(username)) {
                if (argon2.verify(user.getPassword(), password)) { //Kontrollib, kas sisse logides sisestatud pass on sama mis failis olev password.
                    currentUser = user;
                    socketOut.writeInt(Commands.doConfirmLogin); //kui sisselogimine õnnestub
                    socketOut.writeUTF("Olete sisselogitud.");
                    responseSent = true;
                }
                else {
                    socketOut.writeInt(Commands.doNotConfirmLogin); //kui sisselogimine ei õnnestu
                    socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti.");
                    responseSent = true;
                }
            }
        }
        if (!responseSent) {
            socketOut.writeInt(Commands.doNotConfirmLogin); //94 tähendab, et sisselogimine ei õnnestunud
            socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
        }
    }

    //Vajab parandusi
    private String readHashedPasswordFromFile(String username) throws Exception{
        List<String> fileContent = Files.readAllLines(Path.of("users.txt"));
        if (fileContent.size() == 1){ //kui fail on tühi
            return argon2.hash(10,65536,1,allUsers.get(0).getPassword().toCharArray());
        }
        for (String user :
                fileContent) {
            if (user.contains(username)){
                return user.split(";;")[5];
            }
        }
        return null;
    }

    private void checkForUsernameInList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        boolean usernameAlreadyExists = false;
        for (User user : allUsers) {
            if (user.getUsername().equals(username)) {
                usernameAlreadyExists = true;
            }
        }
        socketOut.writeBoolean(usernameAlreadyExists);
    }

    private void sendInfoToClient(DataOutputStream socketOut) throws IOException {
        // üldmeetod clientile (ehk Main-ile) tagasi info saatmiseks
        // võiks delegeerida töö väiksematele spetsiifilistele meetoditele
        // saadab spetsiifilistele meetoditele mingi käsu
    }

    private void sendConfirmationMessage(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

    }

    private void editTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        // mingi taski muutmise meetod
        //user võiks saada valida, mitmenda taski ta muuta soovib e. teame taski indeksit
        //saab valida kuidas ta kirjeldust muuta soovib ka
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.doEditTask);
        socketOut.writeUTF("Sisestage taski järjekorranumber, mida te muuta soovite: ");
        int indeks = socketIn.readInt();
        socketOut.writeUTF("Mida soovite antud taskiga teha: ");
        socketOut.writeUTF("16 - Soovin lisada kommentaari.");
        socketOut.writeUTF("17 - Soovin deadline'i muuta. ");
        int requestType = socketIn.readInt();
        if(requestType == Commands.doAddComment){
            String comment = socketIn.readUTF();
            todoList.get(indeks-1).addComments(comment);
            System.out.println("saatmisel");
           socketOut.writeUTF("Kommentaar lisatud.");
            System.out.println("saadetud");
            socketOut.writeBoolean(false);
        }
        if(requestType == Commands.doPushDeadline){
            socketOut.writeUTF("Sisestage päevade arv, mille võrra soovite deadline'i edasi lükata: ");
            int days = socketIn.readInt();
            todoList.get(indeks - 1).setDeadline(days);
            socketOut.writeUTF("Deadline muudetud.");
            socketOut.writeBoolean(false);
        }
    }

    private void addTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException{
        socketOut.writeInt(Commands.doAddTask);
        socketOut.writeUTF("Sisestage taski kirjeldus: ");
        String taskDescription = socketIn.readUTF();
        // siia peaks mõtlema, kuidas unique task id teha, hetkel kõigil 0.
        int taskID = 0;
        currentUser.addTask(new Task(currentUser, taskDescription, taskID));
        socketOut.writeUTF("Task loodud.");
        socketOut.writeBoolean(false);

    }

    private void displayTasks(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(Commands.doDisplayTasks);
        int commentsAmount = 0;
        for (Task task : todoList) {
            commentsAmount += task.getComments().size();
        }
        socketOut.writeInt(todoList.size()*2 + commentsAmount);
        for (Task task : todoList) {
            socketOut.writeUTF(task.getTaskDescription());
            for (String comment : task.getComments()) {
                socketOut.writeUTF("     Comment: " + comment);
            }
            socketOut.writeUTF("     Deadline: " + task.getTaskDeadline().getDeadlineDate());
        }
        socketOut.writeBoolean(false);
    }

    private void completeTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        // mingi taski lõpetamise meetod
        //user võiks saada valida, mitmenda taski ta listist lõpetada soovib e. teame taski indeksit
        socketOut.writeInt(Commands.doCompleteTask);
        socketOut.writeUTF("Kirjutage selle taski järjekorranumber, mida soovite eemaldada: ");
        int indeks = socketIn.readInt();
        List<Task> todoList = currentUser.getToDoList();
        todoList.get(indeks - 1).setTaskFinished();
        todoList.remove(indeks - 1);
        socketOut.writeUTF("Task edukalt eemaldatud");
        socketOut.writeBoolean(false);
    }

    private void taskReminder(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
    }

    private void writeMessage(DataOutputStream socketOut, int messageType, String message) throws IOException {
        socketOut.writeInt(messageType);
        socketOut.writeUTF(message);
    }

}