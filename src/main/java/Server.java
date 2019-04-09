import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    public static void main(String[] args) throws Exception {
        //boolean deadlineThreadActive = false;
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("DEBUG: Server alustab tööd" + "\r\n");

            // Siin pole veel midagi mõistlikku, sp välja kommenteeritud
                /*
                if (!deadlineThreadActive) {
                    Thread deadlineThread = new Thread(new DeadlineThread(socket));
                    deadlineThread.start();
                    deadlineThreadActive = true;
                }
                */

            // TODO: teen hiljem ilmselt CopyOnWriteArrayList-iks
            List<User> starterUserList = new ArrayList<>();
            ServerContext serverContext = new ServerContext(starterUserList);
            while (true) {
                Socket socket = ss.accept();

                Thread thread = new Thread(new ServerThread(socket, serverContext));
                thread.start();
            }
        }
    }
}