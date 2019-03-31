import java.net.ServerSocket;
import java.net.Socket;


public class Server {


    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("Server alustab tööd" + "\r\n");
            while (true) {
                Socket socket = ss.accept();
                Thread thread = new Thread(new ServerThread(socket));
                thread.start();
            }
        }
    }
}