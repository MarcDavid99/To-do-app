package Server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    // Config failist username ja passwordi lugemine
    public Properties getPropValues() throws IOException {
        Properties properties = new Properties();
        String propertiesFileName = "config.properties";

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFileName)){

            if (inputStream != null) {
                properties.load(inputStream);
            }
            else {
                throw new FileNotFoundException("Property file " + propertiesFileName + " was not found!");
            }
        }
        return properties;
    }

    public boolean sendMail(String recieverEmail, String subject, String mailBody) throws Exception {

        Properties properties = getPropValues();
        final String username = properties.getProperty("user");
        final String password = properties.getProperty("pw");

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

            return true;

        } catch (MessagingException e) {
            return false;
        }
    }
}