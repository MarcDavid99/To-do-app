import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class DeadlineThread implements Runnable {

    // teen siia hiljem threadi, mis perioodiliselt kontrollib kõigi taskide deadline

    private final Socket socket;

    public DeadlineThread(Socket socket) {

        this.socket = socket;
    }

    public void run() {

        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("DEBUG: Alustab Deadline-de kontrollimise thread");

            // DeadlineThread-i töö

        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
