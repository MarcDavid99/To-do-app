package Server;



import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

        System.out.println("DEBUG: Alustab shared.Deadline-de kontrollimise thread; aeg: " + dateFormat.format(new Date()));

        //long timeOfSleep = sleepAmount; // 3 tundi millisekundites
        long timeOfSleep = 30000;        //30 sekundit (Testväärtus)
        long start = 0; // vajab algväärtust
        long end;
        long timeSlept;

        // TODO: Synchronized plokid selleks, et andmete listi erinevates kohtades
        // TODO: muteerimisel midagi kaotsi ei läheks

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
                                String mailBody = "Hello!" +
                                        "\r\n" + "\r\n" +
                                        "Your task's deadline has passed" + "\r\n" +
                                        "shared.Task description: " + task.getTaskDescription() + "\r\n" +
                                        "shared.Deadline: " + currentDeadline.dateToString() +
                                        "\r\n" + "\r\n" +
                                        "Thank you for using our to-do app!";

                                SendMail remindCurrentTaskOwner = new SendMail();
                                try {
                                    remindCurrentTaskOwner.sendMail(user.getMailAdress(), mailSubject, mailBody);
                                }
                                catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                                task.setRemindedOfPassedDeadline(true);
                            }
                        }
                        if (currentDeadline.isDeadlineApproaching()) {
                            if (!task.isRemindedOfApproachingDeadline()) {
                                String mailSubject = "Reminder of your task in our To-Do List!";
                                String mailBody = "Hello!" +
                                        "\r\n" + "\r\n" +
                                        "Your task's deadline is approaching soon." + "\r\n" +
                                        "shared.Task description: " + task.getTaskDescription() + "\r\n" +
                                        "shared.Deadline: " + currentDeadline.dateToString() +
                                        "\r\n" + "\r\n" +
                                        "Thank you for using our to-do app!";

                                SendMail remindCurrentTaskOwner = new SendMail();
                                try {
                                    remindCurrentTaskOwner.sendMail(user.getMailAdress(), mailSubject, mailBody);
                                }
                                catch (Exception e) {
                                    throw new RuntimeException(e);
                                }

                                task.setRemindedOfApproachingDeadline(true);
                            }
                        }
                    }
                }
                // Värskendab sctx-s hoiustatavat userite listi
                sctx.setAllUsers(allUsersToCheck);
                try {
                    sctx.writeExistingUsersToFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
