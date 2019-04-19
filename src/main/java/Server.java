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


            // TODO: DeadlineThreadi deadlinede ära kontrollimisest ei saa ükski teine klass teada ehk see tuleb korda teha
            Thread deadlineThread = new Thread(new DeadlineThread(serverContext));
            deadlineThread.start();

            System.out.println("DEBUG: DeadlineThread loodud ja programm jätkab");

            while (true) {
                Socket socket = ss.accept();

                Thread thread = new Thread(new ServerThread(socket, serverContext));
                thread.start();
            }
        }
    }
}