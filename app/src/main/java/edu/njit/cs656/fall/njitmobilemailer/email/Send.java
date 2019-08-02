package edu.njit.cs656.fall.njitmobilemailer.email;

/**
 * Created by Eugen on 10/12/2017.
 */

import android.content.Context;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;


public class Send {
    private static final String host = "smtp.gmail.com";

    public static void Email(Mail letter, Context context) {
        final Authentication authentication = new Authentication();

        Session session = Session.getInstance(authentication.getSMTPProperties(),
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authentication.getUsername(context), authentication.getPassword(context));
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(letter.getFromClient()));

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(letter.getToClient()));

            message.setSubject(letter.getSubject());
            Multipart mainPart = new MimeMultipart();
            BodyPart messageBody = new MimeBodyPart();
            messageBody.setText(letter.getMessage());
            mainPart.addBodyPart(messageBody);
            message.setContent(mainPart);
            Transport.send(message);
        } catch(MessagingException mex) {
            mex.printStackTrace();
        }
    }

}
