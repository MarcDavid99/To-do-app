package Server;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ServerContext {

    public final List<User> allUsers;

    public ServerContext(List<User> allUsers) {
        this.allUsers = allUsers;
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public static List<User> readExistingUsersFromFile() throws IOException {
        List<User> usersFromFile = new ArrayList<>();
        Path pathToFile = Path.of("users.txt");

        if (new File("users.txt").exists() && new File("users.txt").length() > 0) {
            String jsonAllUsers;
            jsonAllUsers = Files.readString(pathToFile);
            Gson gson = new Gson();
            usersFromFile = gson.fromJson(jsonAllUsers, UserList.class);
        } else if (!Files.exists(pathToFile)) {
            Files.createFile(pathToFile);
        }
        return usersFromFile;
    }

    public void writeExistingUsersToFile() throws IOException {
        synchronized (this) {
            Path pathToFile = Path.of("users.txt");
            Gson gson = new Gson();
            String jsonAllUsers = gson.toJson(allUsers);
            Files.writeString(pathToFile, jsonAllUsers);
        }
    }
}
