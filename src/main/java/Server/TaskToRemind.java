package Server;

import shared.Task;

public class TaskToRemind {

    private String email;
    private String subject;
    private String body;
    private User user;
    private Task task;
    private boolean approachingDeadline;
    private boolean pastDeadline;

    public TaskToRemind(String email, String subject, String body, User user, Task task, boolean approachingDeadline, boolean pastDeadline) {
        this.email = email;
        this.subject = subject;
        this.body = body;
        this.user = user;
        this.task = task;
        this.approachingDeadline = approachingDeadline;
        this.pastDeadline = pastDeadline;
    }

    public String getEmail() {
        return email;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public User getUser() {
        return user;
    }

    public Task getTask() {
        return task;
    }

    public boolean isApproachingDeadline() {
        return approachingDeadline;
    }

    public boolean isPastDeadline() {
        return pastDeadline;
    }
}
