import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    // Server mis hoiustab kasutajate andmeid ja millega, saab request-response
    // põhimõttega suhelda

    //List kõigist kasutajatest
    ArrayList<User> users = new ArrayList<>();


    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            while (true) {
                Socket socket = ss.accept();
                Thread serverThread = new Thread(new ServerThread(socket));
                serverThread.start();
            }
        }
    }
}
