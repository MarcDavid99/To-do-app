import java.util.ArrayList;
import java.util.List;

public class User {
    //mõtlesin et siia saaks teha siis kasutaja 'isendid', et kõigil siis mingid teatud fieldid
    //ja kui kasutaja luuakse siis saab uue isendi luua
    final private String firstName;
    final private String lastName;
    final private String username;
    final private String mailAdress;
    private String password;
    private List<Task> toDoList;

    public User(String firstName, String lastName, String username, String mailAdress, String password) {
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

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setNewPassword(String password){
        this.password = password;
    }

    public List<Task> getToDoList() {
        return toDoList;
    }

    public void addTask(Task task){
        toDoList.add(task);
    }

}