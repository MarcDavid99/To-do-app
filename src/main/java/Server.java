import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class Server {

    //List kõigist kasutajatest
    final static List<User> registeredUsers = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("Server alustab tööd" + "\r\n");
            registeredUsers.add(new User("Marc","David","marcdavid","marcdavid99@gmail.com","marcdavid"));
            while (true) {
                Socket socket = ss.accept();
                Thread thread = new Thread(new ServerThread(socket));
                thread.start();
            }
        }
    }

    public static List<User> getRegisteredUsers() {
        return registeredUsers;
    }
}