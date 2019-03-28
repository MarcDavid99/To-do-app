import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerThread implements Runnable {

    private final Socket socket;
    //siia tuleks salvestada kuidagi hetkene user, kuid probleem on selles, et salvestamine
    //toimuks static meetodis ja siis peaks field ka olema static, kuid ei saa teha seda staticuks
    private User currentUser = new User();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Uue kliendi jaoks luuakse uus thread");

            //Peaks kõik eksisteerivad User-id failist lugema enne alustamist
            /*
            Välja kommenteeritud sest siin bug
             */
            //readExistingUsersFromFile();

            boolean closeProgramme;
            while (true) {
                System.out.println("ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out);
                if (closeProgramme) {

                    //Peaks kõik eksisteerivad User-id faili kirjutama enne sulgumist
                    /*
                    Välja kommenteeritud sest siin bug
                    */
                    //writeExistingUsersToFile();

                    System.out.println("ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Failis olgu üksikud muutujad eraldatud ;; sümbolitega
    Viimane muutuja on task-list
    Task-listi taskid on eraldatud :: sümbolitega
     */
    private static void readExistingUsersFromFile() throws IOException {
        //Path pathToFile = Paths.get("users.txt");
        //List<String> lines = Files.readAllLines(pathToFile);
        List<String> lines = new ArrayList<>();
        File file = new File("users.txt");
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lines.add(line);
        }
        System.out.println(lines);
        for (String line : lines) {
            if (!line.equals("")) {
                String[] lineSplit = line.split(";;");
                String firstName = lineSplit[0];
                String lastName = lineSplit[1];
                String username = lineSplit[2];
                String mailAddress = lineSplit[3];
                int hashedPassword = Integer.parseInt(lineSplit[4]);

                String allTasks = lineSplit[5];
                List<Task> toDoList = new ArrayList<>();
                String[] jsonTasks = allTasks.split("::");
                Gson gson = new Gson();
                for (String json : jsonTasks) {
                    //Kuna faili viimase taski lõppu kirjutatakse ::, võib potentsiaalselt
                    //tekkida olukord, kus ta arvab, et :: on ka task
                    json.replaceAll("::", "");
                    if (!json.equals("")) {
                        Task newTask = gson.fromJson(json, Task.class);
                        toDoList.add(newTask);
                    }
                }

                User newUser = new User(firstName, lastName, username, mailAddress, hashedPassword, toDoList);
                Server.getRegisteredUsers().add(newUser);

                //Niisama abiks üleval ja all oleva koodi jaoks
                //Gson gsonUser = new Gson();
                //String jsonUser = gsonUser.toJson(newUser);
                //String json = socketIn.readUTF();
                //Gson gson = new Gson();
                //User newUser = gson.fromJson(json, User.class);
            }
        }
    }

    private static void writeExistingUsersToFile() throws IOException {
        FileWriter fileWriter = new FileWriter("users.txt");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        for (User user : Server.getRegisteredUsers()) {
            printWriter.print(
                    user.getFirstName() + ";;" +
                            user.getLastName() + ";;" +
                            user.getUsername() + ";;" +
                            user.getMailAdress() + ";;" +
                            user.getHashedPassword() + ";;"
            );

            for (Task task : user.getToDoList()) {
                Gson gson = new Gson();
                String jsonTask = gson.toJson(task);
                printWriter.print(jsonTask + "::");
            }
        }
    }

    private static boolean detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        int requestType = socketIn.readInt();
        //Taskidega seotud käsud võiks alata 11-st
        //Kasutajaga seotud käsud võiks alata 91-st
        //91 on kasutaja loomine
        if (requestType == 91) {
            saveNewUser(socketIn);
        }
        if (requestType == 92) {
            verifyClient(socketIn, socketOut);
        }
        if (requestType == 95) {
            checkForUsernameInList(socketIn, socketOut);
        }
        if(requestType == 12){

            //vaata ülesandeid
        }
        if(requestType==13){
            //muuda ülesandeid
        }
        if(requestType==14){
            //märgi ülesanne lõpetatuks
        }

        if (requestType == 3 || requestType == 15) {
            closeTodoList(socketIn, socketOut);
            return true;
        }
        return false;
    }

    private static boolean closeTodoList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        socketOut.writeBoolean(true);
        return true;
    }

    private static void saveNewUser(DataInputStream socketIn) throws IOException {
        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        Server.getRegisteredUsers().add(newUser);
    }

    private static void verifyClient(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        int hashedPassword = socketIn.readInt();
        List<User> registeredUsers = Server.getRegisteredUsers();
        boolean responseSent = false;
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                if (user.getHashedPassword() == hashedPassword) {
                    socketOut.writeInt(93); //kui sisselogimine õnnestub
                    socketOut.writeUTF("Olete sisselogitud.");
                    responseSent = true;
                }
                else {
                    socketOut.writeInt(94); //kui sisselogimine ei õnnestu
                    socketOut.writeUTF("Sisestatud parool on vale. Proovige uuesti.");
                    responseSent = true;
                }
            }
        }
        if (!responseSent) {
            socketOut.writeInt(94); //94 tähendab, et sisselogimine ei õnnestunud
            socketOut.writeUTF("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
        }
    }

    private static void checkForUsernameInList(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        boolean usernameAlreadyExists = false;
        List<User> registeredUsers = Server.getRegisteredUsers();
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                usernameAlreadyExists = true;
            }
        }
        socketOut.writeBoolean(usernameAlreadyExists);
    }

    private static void sendInfoToClient(DataOutputStream socketOut) throws IOException {
        // üldmeetod clientile (ehk Main-ile) tagasi info saatmiseks
        // võiks delegeerida töö väiksematele spetsiifilistele meetoditele
        // saadab spetsiifilistele meetoditele mingi käsu
    }

    private static void sendConfirmationMessage(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

    }

    private static void editTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        // mingi taski muutmise meetod
        //user võiks saada valida, mitmenda taski ta muuta soovib e. teame taski indeksit
        //saab valida kuidas ta kirjeldust muuta soovib ka
        List<Task> todoList = currentUser.getToDoList();
        int indeks = socketIn.readInt();
        socketOut.writeInt(13);
        socketOut.writeUTF("Mida soovite antud taskiga teha: ");
        socketOut.writeUTF("15 - Soovin lisada kommentaari.");
        socketOut.writeUTF("16 - Soovin deadline'i muuta. ");
        int requestType = socketIn.readInt();
        if(requestType == 15){
            String comment = socketIn.readUTF();
            todoList.get(indeks-1).addComments(comment);
            writeMessage(socketOut, 15, "Kommentaar lisatud.");
        }
        if(requestType == 16){
            socketOut.writeInt(16);
            socketOut.writeUTF("Sisestage päevade arv, mille võrra soovite deadline'i edasi lükata: ");
            int days = socketIn.readInt();
            todoList.get(indeks-1).setDeadline(days);
        }
    }


    private static void displayTasks(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        List<Task> todoList = currentUser.getToDoList();
        socketOut.writeInt(12);
        socketOut.writeInt(todoList.size());
        for (Task task : todoList) {
            socketOut.writeUTF(task.getTaskDescription());
        }
    }

    private static void completeTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        // mingi taski lõpetamise meetod
        //user võiks saada valida, mitmenda taski ta listist lõpetada soovib e. teame taski indeksit
        int indeks = socketIn.readInt();
        List<Task> todoList = currentUser.getToDoList();
        todoList.get(indeks-1).setTaskFinished();
        todoList.remove(indeks-1);
        writeMessage(socketOut, 14, "Task edukalt eemaldatud");
    }

    private static void taskReminder(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
    }

    private static void writeMessage(DataOutputStream socketOut, int messageType, String message) throws IOException {
        socketOut.writeInt(messageType);
        socketOut.writeUTF(message);
    }

}