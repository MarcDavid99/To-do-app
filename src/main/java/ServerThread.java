import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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

    }

    private void saveNewUser(DataInputStream socketIn) throws IOException {

        String json = socketIn.readUTF();
        Gson gson = new Gson();
        User newUser = gson.fromJson(json, User.class);
        Server.getRegisteredUsers();
        /*
        tuleb välja mõelda mingi viis kuidas salvestada usereid, sest nii ei saa
         */
    }

    private static void sendInfoToClient(DataOutputStream socketOut) throws IOException {

    }
}