import java.util.ArrayList;
import java.util.List;

public class User {
    //mõtlesin et siia saaks teha siis kasutaja 'isendid', et kõigil siis mingid teatud fieldid
    //ja kui kasutaja luuakse siis saab uue isendi luua
    final private String username;
    final private String mailAdress;
    final private String password;
    private List<String> toDoList;

    public User(String username, String mailAdress, String password) {
        this.username = username;
        this.mailAdress = mailAdress;
        this.password = password;
        this.toDoList = new ArrayList<>();
    }
}