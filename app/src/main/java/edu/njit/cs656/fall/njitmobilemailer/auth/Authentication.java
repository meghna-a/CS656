package edu.njit.cs656.fall.njitmobilemailer.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Properties;

/**
 * Created by Eugen on 10/28/2017.
 */

public class Authentication {

    private Properties smtpProperties;
    private Properties pop3Properties;
    private Properties imapProperties;

    public Authentication() {
        smtpProperties = new Properties();
        smtpProperties.setProperty("mail.smtp.host", "smtp.gmail.com"); // TODO: 10/12/2017 Add SSL/TLS port
        smtpProperties.setProperty("mail.smtp.auth", "true");
        smtpProperties.setProperty("mail.smtp.starttls.enable", "true");
        smtpProperties.setProperty("mail.smtp.port", "587");

        imapProperties = new Properties();
        imapProperties.setProperty("mail.imaps.connectiontimeout", "10000");
        imapProperties.setProperty("mail.imaps.timeout", "10000");
        imapProperties.setProperty("mail.imaps.host", "imap.gmail.com");
        imapProperties.setProperty("mail.imaps.port", "993");
        imapProperties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        imapProperties.setProperty("mail.imap.socketFactory.fallback", "false");
    }

    public String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("username", "");
    }

    public String getPassword(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("password", "");
    }

    public Properties getSMTPProperties() {
        return smtpProperties;
    }
    public Properties getIMAPProperties() {
        return imapProperties;
    }
}
