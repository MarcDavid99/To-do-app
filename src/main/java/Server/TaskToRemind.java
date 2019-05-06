package Server;

import shared.Task;

public class TaskToRemind {

    public final String email;
    public final String subject;
    public final String body;
    public final User user;
    public final Task task;
    public final boolean approachingDeadline;
    public final boolean pastDeadline;

    public TaskToRemind(String email, String subject, String body, User user, Task task, boolean approachingDeadline, boolean pastDeadline) {
        this.email = email;
        this.subject = subject;
        this.body = body;
        this.user = user;
        this.task = task;
        this.approachingDeadline = approachingDeadline;
        this.pastDeadline = pastDeadline;
    }
}
