import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) throws Exception {


        while (true) {
            try (Socket socket = new Socket("localhost", 1337);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {


                System.out.print("Kui soovite registreerida kasutajat, kirjutage 1, kui teil on kasutaja olemas, kirjutage 2: ");
                Scanner scanner = new Scanner(System.in);
                String initialCommand = scanner.nextLine();

                if (initialCommand.equals("1")) {

                    //Kasutaja loomise meetod
                    userCreation(out, scanner);
                }
                else if (initialCommand.equals("2")) {
                    //Kasutaja tuvastamise meetod
                    if (userVerification(out, input, scanner)){
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
        if(isequiredPassword(password)){
            //genereeritakse suvaline täisarv
            int verificationCode = (int)Math.floor(Math.random()*100000+1);
            //saadetakse kood sisestatud meilile
            SendMail verificationmail = new SendMail();
            if(verificationmail.sendMail(mailAddress, "Verification code for your To-Do list account",
                    "Hello!" + "\r\n" + "\r\n" + "Your verification code is: " + verificationCode + "." + "\r\n" + "\r\n" +
                    "Thank you for using our to-do app!")){
                System.out.print("Sisestage sisestatud meiliaadressile saadetud verification code: ");
                try{
                    int inputCode = Integer.parseInt(scanner.nextLine());
                    if(inputCode == verificationCode){
                        //siin võiks enne useri loomist passwordi ära hashida
                        User newUser = new User(firstName, lastName, username, mailAddress, password);

                        System.out.println("Kasutaja on edukalt loodud; kasutajanimi: " + username);

                        socketOut.writeInt(91);
                        Gson gsonUser = new Gson();
                        String jsonUser = gsonUser.toJson(newUser);
                        socketOut.writeUTF(jsonUser);
                    }
                    else{
                        System.out.println("Sisestatud kood ei ole õige, palun proovige uuesti registreerida.");
                    }

                }
                catch (NumberFormatException e){
                    System.out.println("Sisestasite koodi valesti.");
                }
            }
            else{
                System.out.println("Sisestatud meiliaadressile meili saatmine ebaõnnestus, palun proovige uuesti registreerida.");
            }
        }
        else{
            System.out.println("Salasõna peab olema vähemalt 8 tähemärki pikk. Palun proovige uuesti registreerida.");
        }

    }

    private static void addToMainUserList(List<User> list){

    }


    private static boolean userVerification(DataOutputStream socketOut, DataInputStream input, Scanner scanner) throws IOException {
        System.out.print("Sisestage oma kasutajanimi: ");
        String existingUsername = scanner.nextLine();
        System.out.print("Sisestage oma salasõna: ");
        String existingPassword = scanner.nextLine();
        //siin peaks ära hashima passwordi, et serverile saadetaks hashitud password

        socketOut.writeInt(92);
        socketOut.writeUTF(existingUsername);
        socketOut.writeUTF(existingPassword);

        //tuleks saada serverilt tagasi kinnitus, et kasutaja on olemas ja parool õige
        int type = input.readInt();
        if(type == 93){
            System.out.println("Olete sisselogitud.");
            return true;
        }
        if(type == 94){
            System.out.println("Sisestatud parool on vale. Proovige uuesti.");
            return false;
        }
        if(type == 95){
            System.out.println("Sellise kasutajanimega kasuajat ei leidu. Proovige uuesti.");
            return false;
        }
        return false;
        //vb sellest typeist piisakski, et nt kui parool vale siis mingi kindel type
        //kui kõik korras siis kindel, kui kasutaja sellise
        //nimega puudub siis ka mingi kindel message type
    }

    private static void commandToServer(DataOutputStream socketOut, String command) throws IOException {

        int commandAsInt = Integer.parseInt(command);
        socketOut.writeInt(commandAsInt);
    }

    private static boolean isequiredPassword(String password){
        return password.length() >= 8;
    }
}