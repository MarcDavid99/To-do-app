import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class DeadlineThread implements Runnable {

    private final Socket socket;
    private final ServerContext sctx;

    public DeadlineThread(Socket socket, ServerContext sctx) {

        this.socket = socket;
        this.sctx = sctx;
    }


    public void run() {

        try (socket;
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            System.out.println("DEBUG: Alustab Deadline-de kontrollimise thread");

            long timeOfSleep = 3600000; // 3 tundi millisekundites
            // long timeOfSleep = 10000;        10 sekundit (Testväärtus)
            long start = 0; // vajab algväärtust
            long end;
            long timeSlept;


            while (true) {
                List<User> allUsersToCheck = sctx.getAllUsers();
                for (User user : allUsersToCheck) {
                    List<Task> currentUserTaskList = user.getToDoList();
                    for (Task task : currentUserTaskList) {

                        // TODO: Taskidele tuleks lisada mingi boolean või int arv, mis näitab, kas deadline
                        // TODO: tulekut on juba hoiatatud. Int-ist oleks kasu, kui anname kasutajale võimaluse
                        // TODO: paluda nt mitut meeldetuletust.
                        // Hetkel saadetakse iga 3 tunni tagant kasutajale e-mail.

                        Deadline currentDeadline = task.getTaskDeadline();
                        if (currentDeadline.isPastDeadline()) {

                            String mailSubject = "Reminder of your task in our To-Do List!";
                            String mailBody = "Hello!" +
                                    "\r\n" + "\r\n" +
                                    "Your task's deadline is approaching soon." + "\r\n" +
                                    "Task description: " + task.getTaskDescription() +
                                    "\r\n" + "\r\n" +
                                    "Thank you for using our to-do app!";

                            SendMail remindCurrentTaskOwner = new SendMail();
                            remindCurrentTaskOwner.sendMail(user.getMailAdress(), mailSubject, mailBody);
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
                        timeOfSleep = 3600000; // thread katkestab sleepi, aga teab et järgmine kord kestab sleep jälle 3600000 ms
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
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
