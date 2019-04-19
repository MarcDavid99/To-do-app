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
    private List<String> taskFollowers;
    private boolean isPrivateTask;


    public Task(String taskDescription, String taskID, String taskCreatorID, String taskUserID, boolean isPrivateTask) {
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
        this.isPrivateTask = isPrivateTask;
    }

    public void addFollower(String userId){

        taskFollowers.add(userId);
    }

    public void setDeadline(int deadlineAmountInDays, List<User> allUsers) {
        if(!taskFollowers.isEmpty()){
            for (String userId : taskFollowers) {
                for (User currentUser : allUsers) {
                    if(currentUser.getUserID().equals(userId)){
                        new SendMail().sendMail(currentUser.getMailAdress(),
                                "The deadline of a task you are following has been adjusted",
                                "Hello!" +
                                        "\r\n" + "\r\n" +
                                        "The following task's deadline has been adjusted: " + this.getTaskDescription() +
                                        "\r\n" +
                                        "New deadline: " + this.getTaskDeadline().dateToString() +
                                        "\r\n" + "\r\n" +
                                        "Thank you for using our to-do app!");
                    }
                }
            }
        }
        this.taskDeadline.setDeadline(deadlineAmountInDays);
    }

    public void setTaskFinished(List<User> allUsers){
        if(!taskFollowers.isEmpty()){
            for (String userId : taskFollowers) {
                for (User currentUser : allUsers) {
                    if(currentUser.getUserID().equals(userId)){
                        new SendMail().sendMail(currentUser.getMailAdress(),
                                "A task you are following has been marked as completed.",
                                "Hello!" +
                                        "\r\n" + "\r\n" +
                                        "The following task has been set as finished: " + this.getTaskDescription() +
                                        "\r\n" + "\r\n" +

                                        "Thank you for using our to-do app!");
                    }
                }
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
    public boolean isPrivateTask() {
        return isPrivateTask;
    }

    public void addComments(String comment, List<User> allUsers) {
        if(!taskFollowers.isEmpty()){
            for (String userId : taskFollowers) {
                for (User currentUser : allUsers) {
                    if(currentUser.getUserID().equals(userId)){
                        new SendMail().sendMail(currentUser.getMailAdress(),
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
