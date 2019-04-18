import java.util.ArrayList;
import java.util.List;

public class Task {

    /*

    Gson-i tõttu tekitas fieldi: final private User taskOwner kasutamine lõputu tsükli
    Pidin selle ära kustutama ja siia tuleks mingi int tüüpi ID asemele teha

     */
    final private String taskCreatorID;
    final private String taskUserID;
    final private String taskDescription;
    final String taskID;
    private boolean isFinished;
    final private List<Task> linkedTasks;
    final private List<String> comments;
    private Deadline taskDeadline = new Deadline();
    private boolean remindedOfApproachingDeadline;
    private boolean remindedOfPassedDeadline;
    private List<User> taskFollowers;



    public Task(String taskDescription, String taskID, String taskCreatorID, String taskUserID) {
        this.taskCreatorID = taskCreatorID;
        this.taskUserID = taskUserID;
        this.taskDescription = taskDescription;
        this.taskID = taskID;
        this.comments = new ArrayList<>();
        this.linkedTasks = new ArrayList<>();
        this.isFinished = false;
        this.taskDeadline.setDeadline(7); //Taski creatimisel on automaatselt deadlinei dateks järgmine nädal
        this.remindedOfApproachingDeadline = false;
        this.remindedOfPassedDeadline = false;
        this.taskFollowers = new ArrayList<>();
    }

    public void addFollower(User user){
        taskFollowers.add(user);
    }

    public void setDeadline(int deadlineAmountInDays) {
        this.taskDeadline.setDeadline(deadlineAmountInDays);
    }

    public void setTaskFinished(){
        if(!taskFollowers.isEmpty()){
            for (User user : taskFollowers) {
                new SendMail().sendMail(user.getMailAdress(),
                        "A task you are following is completed.",
                        "Hello!" +
                                "\r\n" + "\r\n" +
                                "The following task has been set as finished: " + this.getTaskDescription() +
                                "\r\n" + "\r\n" +

                                "Thank you for using our to-do app!");
            }
        }
        isFinished = true;
    }

    public String getTaskCreatorID() {
        return taskCreatorID;
    }

    public String getTaskUserID() {
        return taskUserID;
    }

    public String getTaskID() {
        return taskID;
    }

    public boolean isRemindedOfApproachingDeadline() {
        return remindedOfApproachingDeadline;
    }

    public void setRemindedOfApproachingDeadline(boolean remindedOfApproachingDeadline) {
        this.remindedOfApproachingDeadline = remindedOfApproachingDeadline;
    }

    public boolean isRemindedOfPassedDeadline() {
        return remindedOfPassedDeadline;
    }

    public void setRemindedOfPassedDeadline(boolean remindedOfPassedDeadline) {
        this.remindedOfPassedDeadline = remindedOfPassedDeadline;
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
        if(!taskFollowers.isEmpty()){
            for (User user : taskFollowers) {
                new SendMail().sendMail(user.getMailAdress(),
                        "A comment was added to a task you are following.",
                        "Hello!" +
                                "\r\n" + "\r\n" +
                                "A comment was added to the following task: " + this.getTaskDescription() +
                                "\r\n" +
                                "The comment that was added: " + comment +
                                "\r\n" + "\r\n" +

                                "Thank you for using our to-do app!");
            }
        }
        comments.add(comment);
    }

    public void addLinkedTasks(Task task){
        linkedTasks.add(task);
    }

    boolean isFinished(){
        return isFinished;
    }

}
