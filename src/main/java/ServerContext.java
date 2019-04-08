import java.util.List;

public class ServerContext {

    // Klass, mille abil DeadlineThread.java pääseb ligi ServerThread.java-s olevale List<User> allUsers-ile

    public List<User> allUsers;

    public ServerContext(List<User> allUsers) {
        this.allUsers = allUsers;
    }

    public List<User> getAllUsers() {
        return allUsers;
    }

    public void setAllUsers(List<User> allUsers) {
        this.allUsers = allUsers;
    }
}
