import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;

public class DeadlineThread implements Runnable {

    // teen siia hiljem threadi, mis perioodiliselt kontrollib kõigi taskide deadline

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

            // DeadlineThread-i töö
            while (true) {

                // TODO: Kontroll lõpuni teha (hetkel algelised tsüklid)
                List<User> allUsersToCheck = sctx.getAllUsers();
                for (User user : allUsersToCheck) {
                    List<Task> currentUserTaskList = user.getToDoList();
                    for (Task task : currentUserTaskList) {
                    }
                }
                /*
                kontrollib deadline siin
                 */

                // Inspiratsioon: https://stackoverflow.com/questions/3797941/how-to-make-a-thread-sleep-for-specific-amount-of-time-in-java
                long timeOfSleep = 3600000; // 3 tundi millisekundites
                long start, end, timeSlept;

                try {
                    while (timeOfSleep > 0) {
                        start = System.currentTimeMillis();
                        try {
                            Thread.sleep(timeOfSleep);
                            break;
                        }
                        catch (InterruptedException e) {
                            end = System.currentTimeMillis();
                            timeSlept = end - start;
                            timeOfSleep -= timeSlept;
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("DEBUG: DeadlineThread-i .sleep() meetod katkestati");
                    throw new RuntimeException(e);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
