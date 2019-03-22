import com.google.gson.Gson;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ServerThread implements Runnable {

    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("Uue kliendi jaoks luuakse uus thread");
            boolean closeProgramme = false;
            while (true) {
                System.out.println("ServerThread teeb tööd");
                closeProgramme = detectClientRequest(input, out);
                if (closeProgramme) {
                    System.out.println("ServerThread lõpetab töö!" + "\r\n");
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        if (requestType == 93) {
            checkForUsernameInList(socketIn, socketOut);
        }
        if (requestType == 3) {
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
        String password = socketIn.readUTF();
        List<User> registeredUsers = Server.getRegisteredUsers();
        boolean responseSent = false;
        for (User user : registeredUsers) {
            if (user.getUsername().equals(username)) {
                if (user.getPassword().equals(password)) {
                    socketOut.writeInt(93); //kui sisselogimine õnnestub
                    responseSent = true;
                } else {
                    socketOut.writeInt(94); //kui parool on vale
                    responseSent = true;
                }
            }
        }
        if (!responseSent) {
            socketOut.writeInt(95); //kui kasutajanime ei leidu kasutajate hulgas
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
    }

    private static void displayTask(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        // mingi taski näitamise meetod kliendi palvel
    }

    private static void completeTask(DataInputStream socketIn, DataOutputStream socketout) throws IOException {
        // mingi taski lõpetamise meetod
    }

    private static void taskReminder(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

    }
}