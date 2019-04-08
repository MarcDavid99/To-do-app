import java.util.ArrayList;
import java.util.List;

public class Task {

    /*

    Gson-i tõttu tekitas fieldi: final private User taskOwner kasutamine lõputu tsükli
    Pidin selle ära kustutama ja siia tuleks mingi int tüüpi ID asemele teha

     */
    //final private User taskOwner;
    final private String taskDescription;
    final int taskID;
    private boolean isFinished;
    final private List<Task> linkedTasks;
    final private List<String> comments;
    private Deadline taskDeadline = new Deadline();


    public Task(String taskDescription, int taskID) {
        //this.taskOwner = taskOwner;
        this.taskDescription = taskDescription;
        this.taskID = taskID;
        this.comments = new ArrayList<>();
        this.linkedTasks = new ArrayList<>();
        //kui alles taski loome, siis ilmselt see pole kohe algul finished
        this.isFinished = false;
        this.taskDeadline.setDeadline(7); //Taski creatimisel on automaatselt deadlinei dateks järgmine nädal
    }



    public void setDeadline(int deadlineAmountInDays) {
        this.taskDeadline.setDeadline(deadlineAmountInDays);
    }

    public void setTaskFinished(){
        isFinished = true;
    }

    // vt üles, miks välja kommenteeritud
    /*
    public User getTaskOwner() {
        return taskOwner;
    }
    */

    public String getTaskDescription() {
        return taskDescription;
    }

    public List<Task> getLinkedTasks() {
        return linkedTasks;
    }

    public List<String> getComments() {
        return comments;
    }

    public Deadline getTaskDeadline() {
        return taskDeadline;
    }

    public void addComments(String comment) {
        comments.add(comment);
    }

    public void addLinkedTasks(Task task){
        linkedTasks.add(task);
    }

    boolean isFinished(){
        return isFinished;
    }

}
