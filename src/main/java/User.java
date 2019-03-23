import java.util.ArrayList;
import java.util.List;

public class User {
    //mõtlesin et siia saaks teha siis kasutaja 'isendid', et kõigil siis mingid teatud fieldid
    //ja kui kasutaja luuakse siis saab uue isendi luua
    final private String firstName;
    final private String lastName;
    final private String username;
    final private String mailAdress;
    private int hashedPassword;
    private List<Task> toDoList;

    public User(String firstName, String lastName, String username, String mailAdress, int hashedPassword) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.mailAdress = mailAdress;
        this.hashedPassword = hashedPassword;
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

    public String getUsername() {
        return username;
    }

    public int getHashedPassword() {
        return hashedPassword;
    }

    public void setNewPassword(String newPassword){
        this.hashedPassword = newPassword.hashCode();
    }

    public List<Task> getToDoList() {
        return toDoList;
    }

    public void addTask(Task task){
        toDoList.add(task);
    }

}