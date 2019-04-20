import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class Server {

    public static void main(String[] args) throws Exception {
        try (ServerSocket ss = new ServerSocket(1337)) {
            System.out.println("DEBUG: Server alustab tööd" + "\r\n");

            // TODO: teen hiljem ilmselt CopyOnWriteArrayList-iks
            List<User> starterUserList = new ArrayList<>();
            readExistingUsersFromFileSERVER(starterUserList);
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

    // Algselt ServerThreadi meetod, mis loeb serveri kävitamisel info
    // (kirjutamine toimub nüüd alles kliendi ehk serverthreadi sulgemisel)
    private static void readExistingUsersFromFileSERVER(List<User> starterUserList) throws IOException {

        if (new File("users.txt").exists() && new File("users.txt").length() > 0) {
            String jsonAllUsers;
            Path pathToFile = Path.of("users.txt");
            jsonAllUsers = Files.readString(pathToFile);
            Gson gson = new Gson();
            List<User> usersFromFile = gson.fromJson(jsonAllUsers, UserList.class);
            starterUserList.addAll(usersFromFile);
        } else {
            try {
                Files.createFile(Path.of("users.txt"));
            } catch (Exception e) {
                System.out.println("File already exists");
            }
        }
    }
}