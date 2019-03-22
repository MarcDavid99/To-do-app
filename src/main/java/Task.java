import java.util.ArrayList;
import java.util.List;

public class Task {
    final private User taskOwner;
    final private String taskDescription;
    final int taskID;
    private boolean isFinished;
    final private List<Task> linkedTasks;
    final private List<String> comments;
    private Deadline taskDeadline = new Deadline();

    public Task(User taskOwner, String taskDescription, int taskID) {
        this.taskOwner = taskOwner;
        this.taskDescription = taskDescription;
        this.taskID = taskID;
        this.comments = new ArrayList<>();
        this.linkedTasks = new ArrayList<>();
        //kui alles taski loome, siis ilmselt see pole kohe algul finished
        this.isFinished = false;
    }

    public void setDeadline(Deadline deadline) {
        this.taskDeadline = deadline;
    }

    public void setTaskFinished(){
        isFinished = true;
    }

    public User getTaskOwner() {
        return taskOwner;
    }

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
