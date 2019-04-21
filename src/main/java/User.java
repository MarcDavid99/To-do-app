import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.util.ArrayList;
import java.util.List;

public class User {

    final private String firstName;
    final private String lastName;
    final private String username;
    final private String userID;
    final private String mailAdress;
    private String password;
    private List<Task> toDoList;

    //Creating a new user
    public User(String userID, String firstName, String lastName, String username, String mailAdress, String password) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.mailAdress = mailAdress;
        this.password = password;
        this.toDoList = new ArrayList<>();
    }


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMailAdress() {
        return mailAdress;
    }

    public String getUserID() {
        return userID;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setNewPassword(String newPassword){
        Argon2 argon2 = Argon2Factory.create();
        this.password = argon2.hash(10,65536,1,newPassword);
    }

    public List<Task> getToDoList() {
        return toDoList;
    }

    public void addTask(Task task){
        toDoList.add(task);
    }

}