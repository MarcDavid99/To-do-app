package Server;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import shared.*;


public class DeadlineThread implements Runnable {

    private final ServerContext sctx;

    public DeadlineThread(ServerContext sctx) {
        this.sctx = sctx;
    }

    final private long sleepAmount = TimeUnit.HOURS.toMillis(3);

    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public void run() {

        System.out.println("DEBUG: Alustab Deadline-de kontrollimise thread; aeg: " + dateFormat.format(new Date()));

        //long timeOfSleep = sleepAmount; // 3 tundi millisekundites
        long timeOfSleep = 30000;        //30 sekundit (Testväärtus)
        long start = 0; // vajab algväärtust
        long end;
        long timeSlept;

        List<TaskToRemind> listOfTasksToRemind = new ArrayList<>();

        while (true) {

            synchronized (sctx) {
                List<User> allUsersToCheck = sctx.getAllUsers();
                for (User user : allUsersToCheck) {
                    List<Task> currentUserTaskList = user.getToDoList();
                    for (Task task : currentUserTaskList) {
                        Deadline currentDeadline = task.getTaskDeadline();
                        if (currentDeadline.isPastDeadline()) {
                            if (!task.isRemindedOfPassedDeadline()) {
                                String mailSubject = "Reminder that your task's deadline has passed!";
                                String mailBody = "Hello, " + user.getUsername() + "!" +
                                        "\r\n" + "\r\n" +
                                        "Your task's deadline has passed" + "\r\n" +
                                        "Task description: " + task.getTaskDescription() + "\r\n" +
                                        "Deadline: " + currentDeadline.dateToString() +
                                        "\r\n" + "\r\n" +
                                        "Thank you for using our to-do app!";

                                listOfTasksToRemind.add(new TaskToRemind(user.getMailAdress(), mailSubject, mailBody, user, task, false, true));
                            }
                        }
                        if (currentDeadline.isDeadlineApproaching()) {
                            if (!task.isRemindedOfApproachingDeadline()) {
                                String mailSubject = "Reminder of your task in our To-Do List!";
                                String mailBody = "Hello, " + user.getUsername() + "!" +
                                        "\r\n" + "\r\n" +
                                        "Your task's deadline is approaching soon." + "\r\n" +
                                        "Task description: " + task.getTaskDescription() + "\r\n" +
                                        "Deadline: " + currentDeadline.dateToString() +
                                        "\r\n" + "\r\n" +
                                        "Thank you for using our to-do app!";

                                listOfTasksToRemind.add(new TaskToRemind(user.getMailAdress(), mailSubject, mailBody, user, task, true, false));
                            }
                        }
                    }
                }
            }

            SendMail sendMail = new SendMail();
            Iterator<TaskToRemind> iter = listOfTasksToRemind.iterator();
            while (iter.hasNext()) {
                TaskToRemind taskToRemind = iter.next();
                if (taskToRemind.isPastDeadline()) {
                    User currentUser = taskToRemind.getUser();
                    String mailSubject = taskToRemind.getSubject();
                    String mailBody = taskToRemind.getBody();
                    try {
                        sendMail.sendMail(currentUser.getMailAdress(), mailSubject, mailBody);
                        synchronized (sctx) {
                            Task currentTask = taskToRemind.getTask();
                            currentTask.setRemindedOfPassedDeadline(true); // märgitakse true kui meil on päriselt välja saadetud
                        }
                        iter.remove();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                if (taskToRemind.isApproachingDeadline()) {
                    User currentUser = taskToRemind.getUser();
                    String mailSubject = taskToRemind.getSubject();
                    String mailBody = taskToRemind.getBody();
                    try {
                        sendMail.sendMail(currentUser.getMailAdress(), mailSubject, mailBody);
                        synchronized (sctx) {
                            Task currentTask = taskToRemind.getTask();
                            currentTask.setRemindedOfApproachingDeadline(true); // märgitakse true kui meil on päriselt välja saadetud
                        }
                        iter.remove();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            try {
                sctx.writeExistingUsersToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("DEBUG: Server.DeadlineThread jääb magama; aeg: " + dateFormat.format(new Date()));
            // Inspiratsioon: https://stackoverflow.com/questions/3797941/how-to-make-a-thread-sleep-for-specific-amount-of-time-in-java
            try {
                while (timeOfSleep > 0) {
                    start = System.currentTimeMillis();
                    Thread.sleep(timeOfSleep);
                    //timeOfSleep = sleepAmount; // thread katkestab sleepi, aga teab et järgmine kord kestab sleep jälle 3600000 ms
                    timeOfSleep = 30000; //testväärtus
                    System.out.println("DEBUG: Server.DeadlineThread ärkab; aeg: " + dateFormat.format(new Date()));
                    break;

                }
            } catch (InterruptedException e) {
                end = System.currentTimeMillis();
                timeSlept = end - start;
                timeOfSleep -= timeSlept;
                Thread.currentThread().interrupt();
            }
        }
    }
}
