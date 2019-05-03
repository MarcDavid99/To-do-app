import java.util.concurrent.TimeUnit;

public class SynchronizationTest {

    // Käivitab Server.java ja mitu Main.java-t ning üritab testida, kas sünkroniseeritus töötab
    public static void main(String[] args) throws Exception {

        // Käivitab Server.java
        callServer();

        // Annab Serverile 10 sekundit aega käivituda
        TimeUnit.SECONDS.sleep(10);

        // Käivitab 30 Main.java-t
        // TODO: Kasutajate loomine ja allUsers listi muutmine
        for (int i = 0; i < 30; i++) {
            callUser();
        }
    }

    // Käivitab Server.java main meetodi
    public static void callServer() throws Exception {
        Server.main(null);
    }

    // Käivitab Main.java main meetodi
    public static void callUser() throws Exception {
        Main.main(null);
    }
}
