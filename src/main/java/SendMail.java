import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


// got the code from: https://www.mkyong.com/java/javamail-api-sending-email-via-gmail-smtp-example/
public class SendMail {

    public static void sendMail(String recieverEmail, String subject, String mailBody){
        final String username = "todolistreminderOOP@gmail.com";
        final String password = "todolistOOP";

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("todolistreminderOOP@gmail.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recieverEmail));
            message.setSubject(subject);
            message.setText(mailBody);

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}