import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        while (true) {
            try (Socket socket = new Socket("localhost", 1337);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {


                System.out.print("Kui soovite registreerida kasutajat, kirjutage 1, kui teil on kasutaja olemas, kirjutage 2");
                Scanner scanner = new Scanner(System.in);
                String initialCommand = scanner.nextLine();

                if (initialCommand.equals("1")) {
                    //Kasutaja loomise meetod
                    userCreation(out, scanner);
                }
                else if (initialCommand.equals("2")) {
                    //Kasutaja tuvastamise meetod
                    userVerification(out, scanner);


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
                            commandToServer(out, command);
                        }
                        catch (IOException e) {
                            System.out.println("Tekkis viga serverile käsu saatmisel");
                        }

                    }
                    //Vigane käsk kasutaja poolt, eeldusel et ta kasutaja on olemas
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

    private static void userCreation(DataOutputStream socketOut, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma eesnimi: ");
        String firstName = scanner.nextLine();
        System.out.print("Sisestage oma perenimi: ");
        String lastName = scanner.nextLine();
        System.out.print("Sisestage soovitud kasutajanimi: ");
        String username = scanner.nextLine();
        System.out.print("Sisestage soovitud meiliaadress: ");
        String mailAddress = scanner.nextLine();
        System.out.print("Sisestage soovitud salasõna: ");
        String password = scanner.nextLine();
        //genereeritakse suvaline täisarv
        int verificationCode = (int)Math.floor(Math.random()*100000+1);
        //saadetakse kood sisestatud meilile
        SendMail verificationmail = new SendMail();
        verificationmail.sendMail(mailAddress, "Verification code for your To-Do list account", "Your verification code is: " + verificationCode + ".");
        System.out.print("Sisestage sisestatud meiliaadressile saadetud verification code: ");
        try{
            int inputCode = Integer.parseInt(scanner.nextLine());
            if(inputCode == verificationCode){
                User newUser = new User(firstName, lastName, username, mailAddress, password);
                System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);
            }
            else{
                System.out.println("Sisestatud kood ei ole õige, palun proovige uuesti registreerida.");
            }

        }
        catch (NumberFormatException e){
            System.out.println("Sisestasite koodi valesti.");
        }

        socketOut.writeInt(1);
        //tuleb loodud User gsoniga serverile saata
    }


    private static void userVerification(DataOutputStream socketOut, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();

        socketOut.writeInt(2);
    }

    private static void commandToServer(DataOutputStream socketOut, String command) throws IOException {

        int commandAsInt = Integer.parseInt(command);
        socketOut.writeInt(commandAsInt);
    }
}