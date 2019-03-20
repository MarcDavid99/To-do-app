import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ServerThread implements Runnable{

    private final Socket socket;

    public ServerThread(Socket socket) {

        this.socket = socket;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Thread töötab siin");

            detectClientRequest(input, out);

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

        int requestType = socketIn.readInt();

        //Taskidega seotud käsud võiks alata 11-st

        //Kasutajaga seotud käsud võiks alata 91-st
        //91 on kasutaja loomine
        if (requestType == 91) {
            saveNewUser(socketIn);
        }
        if (requestType == 92){
            verifyClient(socketIn, socketOut);
        }

    }

    private static void saveNewUser(DataInputStream socketIn) throws IOException {

        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        Server.getRegisteredUsers().add(newUser);
        /*
        tuleb välja mõelda mingi viis kuidas salvestada usereid, sest nii ei saa
         */
    }

    private static void verifyClient(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {
        String username = socketIn.readUTF();
        String password = socketIn.readUTF();
        List<User> registredUsers = Server.getRegisteredUsers();
        boolean responseSent = false;
        for(User user : registredUsers){
            if (user.getUsername().equals(username)){
                if(user.getPassword().equals(password)){
                    socketOut.writeInt(93); //kui sisselogimine õnnestub
                    responseSent = true;
                }
                else{
                    socketOut.writeInt(94); //kui parool on vale
                    responseSent = true;
                }
            }
        }
        if(!responseSent){
            socketOut.writeInt(95); //kui kasutajanime ei leidu kasutajate hulgas
        }
    }

    private static void sendInfoToClient(DataOutputStream socketOut) throws IOException {

    }
}