import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("DEBUG: Server alustab tööd" + "\r\n");

            // TODO: teen hiljem ilmselt CopyOnWriteArrayList-iks
            List<User> starterUserList = new ArrayList<>();
            ServerContext serverContext = new ServerContext(starterUserList);


            // Välja kommenteeritud, sest terve Server ootab hektel DeadlineThreadi taga
            /*

            Thread deadlineThread = new Thread(new DeadlineThread(ss.accept(), serverContext));
            deadlineThread.start();

            */

            while (true) {
                Socket socket = ss.accept();

                Thread thread = new Thread(new ServerThread(socket, serverContext));
                thread.start();
            }
        }
    }
}