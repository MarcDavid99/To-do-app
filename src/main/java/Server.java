import java.net.ServerSocket;
import java.net.Socket;


public class Server {

    public static void main(String[] args) throws Exception {
        //boolean deadlineThreadActive = false;
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("Server alustab tööd" + "\r\n");

            // Siin pole veel midagi mõistlikku, sp välja kommenteeritud
                /*
                if (!deadlineThreadActive) {
                    Thread deadlineThread = new Thread(new DeadlineThread(socket));
                    deadlineThread.start();
                    deadlineThreadActive = true;
                }
                */

            while (true) {
                Socket socket = ss.accept();

                Thread thread = new Thread(new ServerThread(socket));
                thread.start();
            }
        }
    }
}