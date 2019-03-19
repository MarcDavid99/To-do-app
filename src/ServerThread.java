import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerThread {

    private final Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            System.out.println("Thread töötab siin");

        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void detectClientRequest(DataInputStream socketIn, DataOutputStream socketOut) throws IOException {

        int requestType = socketIn.readInt();
    }
}