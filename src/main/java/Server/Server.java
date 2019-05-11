package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Server {

    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1335)) {
            System.out.println("DEBUG: Server alustab tööd" + "\r\n");

            List<User> starterUserList = ServerContext.readExistingUsersFromFile();
            ServerContext serverContext = new ServerContext(starterUserList);

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