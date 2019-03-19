import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        //Main-i ehk clienti töö võiks toimuda while tsükli, sees, et ta saaks tegutseda, kuni enam ei soovi

        while (true) {
            System.out.print("Kui soovite registreerida kasutajat, kirjutage 1, kui teil on kasutaja olemas, kirjutage 2");
            Scanner scanner = new Scanner(System.in);
            String initialCommand = scanner.nextLine();

            try (Socket socket = new Socket("localhost", 1337);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                if (initialCommand.equals("1")) {
                    //Kasutaja loomise meetod
                    userCreation(scanner);
                }
                else if (initialCommand.equals("2")) {
                    //Kasutaja tuvastamise meetod
                    userVerification(scanner);


                    //Seda oleks võimalik mingi String[] järjendi või ArrayList<String>-ga ja for-loopiga teha
                    //kui käske peaks tekkima väga palju
                    String[] possibleCommands = {"11", "12", "13", "14"};
                    System.out.println("Erinevad võimalused: " + "\r\n" +
                            "11 - lisa ülesanne" + "\r\n" +
                            "12 - vaata ülesannet" + "\r\n" +
                            "13 - muuda ülesannet" + "\r\n" +
                            "14 - märgi ülesanne lõpetatuks" + "\r\n");

                    System.out.print("Valige sobiv tegevus: ");
                    String command = scanner.nextLine();

                    //Tegemist on korrektse käsuga
                    if (Arrays.asList(possibleCommands).contains(command)) {
                        try {
                            commandToServer(command);
                        }
                        catch (IOException e) {
                            System.out.println("Tekkis viga serverile käsu saatmisel");
                        }

                    }
                    //Vigane sisestus kasutaja olemasolul käsu valimisel
                    else {
                        System.out.println("Sisestage korrektne käsk (11, 12, 13, 14)");
                    }

                }
                //Vigane sisestus kasutaja loomisel või kasutajaga millegi tegemisel
                else {
                    System.out.print("Sisestage korrektne käsk (1 või 2): ");
                }
            }
        }
    }

    public static void userCreation(Scanner scanner) {
        System.out.print("Sisestage soovitud kasutajanimi: ");
        String username = scanner.nextLine();
        System.out.print("Sisestage soovitud meiliaadress: ");
        String mailAddress = scanner.nextLine();
        System.out.print("Sisestage soovitud salasõna: ");
        String password = scanner.nextLine();
        User newUser = new User(username, mailAddress, password);
        System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);
    }


    public static void userVerification(Scanner scanner) {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();
    }
}