import java.util.List;
import java.util.concurrent.TimeUnit;

public class DeadlineThread implements Runnable {

    private final ServerContext sctx;

    public DeadlineThread(ServerContext sctx) {
        this.sctx = sctx;
    }

    final private long sleepAmount = TimeUnit.HOURS.toMillis(3);

    public void run() {

        System.out.println("DEBUG: Alustab Deadline-de kontrollimise thread");

        long timeOfSleep = sleepAmount; // 3 tundi millisekundites
        // long timeOfSleep = 30000;        //30 sekundit (Testväärtus)
        long start = 0; // vajab algväärtust
        long end;
        long timeSlept;

        // TODO: Synchronized plokid selleks, et andmete listi erinevates kohtades
        // TODO: muteerimisel midagi kaotsi ei läheks

        while (true) {
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
                                    "Task description: " + task.getTaskDescription() + "\r\n" +
                                    "Deadline: " + currentDeadline.dateToString() +
                                    "\r\n" + "\r\n" +
                                    "Thank you for using our to-do app!";

                            SendMail remindCurrentTaskOwner = new SendMail();
                            remindCurrentTaskOwner.sendMail(user.getMailAdress(), mailSubject, mailBody);

                            task.setRemindedOfPassedDeadline(true);
                        }
                    }
                    if (currentDeadline.isDeadlineApproaching()) {
                        if (!task.isRemindedOfApproachingDeadline()) {
                            String mailSubject = "Reminder of your task in our To-Do List!";
                            String mailBody = "Hello!" +
                                    "\r\n" + "\r\n" +
                                    "Your task's deadline is approaching soon." + "\r\n" +
                                    "Task description: " + task.getTaskDescription() + "\r\n" +
                                    "Deadline: " + currentDeadline.dateToString() +
                                    "\r\n" + "\r\n" +
                                    "Thank you for using our to-do app!";

                            SendMail remindCurrentTaskOwner = new SendMail();
                            remindCurrentTaskOwner.sendMail(user.getMailAdress(), mailSubject, mailBody);

                            task.setRemindedOfApproachingDeadline(true);
                        }
                    }
                }
            }
            // Värskendab sctx-s hoiustatavat userite listi
            sctx.setAllUsers(allUsersToCheck);

            System.out.println("DEBUG: DeadlineThread jääb magama");
            // Inspiratsioon: https://stackoverflow.com/questions/3797941/how-to-make-a-thread-sleep-for-specific-amount-of-time-in-java
            try {
                while (timeOfSleep > 0) {
                    start = System.currentTimeMillis();
                    Thread.sleep(timeOfSleep);
                    timeOfSleep = sleepAmount; // thread katkestab sleepi, aga teab et järgmine kord kestab sleep jälle 3600000 ms
                    // timeOfSleep = 30000; //testväärtus
                    System.out.println("DEBUG: DeadlineThread ärkab");
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
