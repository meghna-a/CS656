package edu.njit.cs656.fall.njitmobilemailer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;

public class ReadMail extends AppCompatActivity {

    public static final String TAG = "ReadMail";
    private Toolbar toolbar;
    private int mailIndex;
    private int mailId;

    public String getMultipartText(MimeMultipart mimeContent) {
        try {
            StringBuilder textBuilder = new StringBuilder();
            StringBuilder htmlBuilder = new StringBuilder();
            Boolean isHTML = false;
            for (int j = 0; j < mimeContent.getCount(); j++) {
                BodyPart bodyContent = mimeContent.getBodyPart(j);
                if (bodyContent.isMimeType("text/html")) {
                    htmlBuilder.append(bodyContent.getContent().toString());
                    isHTML = true;
                } else if (bodyContent.isMimeType("text/plain")) {
                    textBuilder.append(bodyContent.getContent().toString().replaceAll("\r\n", "<br>"));
                } else if (bodyContent.getContent() instanceof MimeMultipart) {
                    textBuilder.append(getMultipartText((MimeMultipart) bodyContent.getContent()));
                }
            }
            String returnString = "";
            if(isHTML){
                returnString = htmlBuilder.toString();
            }
            else{
                returnString = textBuilder.toString();
            }
            return returnString;
        } catch (IOException | MessagingException ex) {
            Log.v(TAG, "Message error.");
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.read_mail_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_mail);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Bundle extra = getIntent().getExtras();

        TextView subject = (TextView) findViewById(R.id.subject_textView);
        String subjectString = extra.getString("subject");
        subject.setText(subjectString);

        TextView from = (TextView) findViewById(R.id.from_textView);
        String fromString = extra.getString("from");
        from.setText(fromString);

        mailIndex = extra.getInt("index");
        mailId = extra.getInt("id");

        TextView dateTextView = (TextView) findViewById(R.id.datetime_textView);
        // Convert date from Long > Date > String
        Date date = new Date();
        date.setTime(extra.getLong("date", -1));
        SimpleDateFormat formatter = new SimpleDateFormat("M/d h:mm a");
        String dateString = formatter.format(date);
        dateTextView.setText(dateString);

        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Loading email...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String contentString = "";
                try {

                    Authentication authentication = new Authentication();
                    Session emailSession = Session.getDefaultInstance(authentication.getIMAPProperties());
                    Store store = emailSession.getStore("imaps");
                    store.connect("imap.gmail.com", authentication.getUsername(getBaseContext()), authentication.getPassword(getBaseContext()));
                    Folder emailFolder = store.getFolder("INBOX");
                    emailFolder.open(Folder.READ_WRITE);
                    Message message = emailFolder.getMessage(mailIndex + 1);

                    if (message == null) {
                        emailFolder.close(true);
                        store.close();
                        setResult(-1);
                        finish();
                    }

                    Log.v(TAG, message.getContentType());
                    message.setFlag(Flags.Flag.SEEN, true);
                    contentString = getMultipartText((MimeMultipart) message.getContent());
                    emailFolder.close(true);
                    store.close();

                } catch(Exception e) {
                    e.printStackTrace();
                }

                final String finalContent = contentString;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        WebView content = (WebView) findViewById(R.id.content_webView);
                        content.loadData(finalContent,"text/html; charset=utf-8","UTF-8");
                        progress.dismiss();
                    }
                });
            }}).start();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO need to update ethe emails in the inbox in ListMail
        // so that user can see that the email was deleted.

        switch (item.getItemId()) {
            case R.id.action_delete:
                Thread runner = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Thread deleter = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    Authentication authentication = new Authentication();
                                    Session emailSession = Session.getDefaultInstance(authentication.getIMAPProperties());
                                    Store store = emailSession.getStore("imaps");
                                    store.connect("imap.gmail.com", authentication.getUsername(getBaseContext()), authentication.getPassword(getBaseContext()));
                                    Folder emailFolder = store.getFolder("INBOX");
                                    emailFolder.open(Folder.READ_WRITE);
                                    emailFolder.getMessage(mailIndex + 1).setFlag(Flags.Flag.DELETED, true);
                                    emailFolder.close(true);
                                    store.close();

                                } catch (MessagingException e) {
                                    Log.v(TAG, "Message error.");
                                }

                            }
                        });

                        deleter.run();
                        try {
                            deleter.join();
                        } catch (InterruptedException e) {
                            Log.v(TAG, "Interrupted error.");
                        }
                    }
                });

                // Need to wait for thread to finish.
                try {
                    runner.start();
                    runner.join();
                } catch (InterruptedException e) {
                    Log.v(TAG, "Interrupted error.");

                }

                Intent intent = new Intent();
                intent.putExtra("index", mailId);
                setResult(1, intent);
                finish();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return true;
    }
}
