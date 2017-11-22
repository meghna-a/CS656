package edu.njit.cs656.fall.njitmobilemailer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;
import edu.njit.cs656.fall.njitmobilemailer.email.Mail;

public class ListMail extends AppCompatActivity {

    public static final String TAG = "ListMail";
    private List<Mail> localMail = new ArrayList<Mail>();
    private List<Mail> remoteMail = new ArrayList<Mail>();
    private LinearLayout linearLayout;
    private ListView list;
    private ListView.LayoutParams listView;
    private ProgressDialog progress;

    private String abbreviateString(String s, int maxLength){
        // If string is longer than max length, truncate the string
        // and add '...'
        if (s.length() > maxLength){
            return s.substring(0, Math.min(s.length(), maxLength)) + "...";
        }
        else {
            return s;
        }
    }

    @SuppressLint("ResourceType")
    public void reDrawLocalMail() {
        //list = new ListView(this);

        localMail.addAll(remoteMail);
        for (int i = 0; i < remoteMail.size(); i++) {
            LinearLayout emailTextContainer = new LinearLayout(this);
            emailTextContainer.setOrientation(LinearLayout.VERTICAL);

            LinearLayout emailView = new LinearLayout(this);
            emailView.setOrientation(LinearLayout.HORIZONTAL);

            RelativeLayout emailInnerView = new RelativeLayout(this);
            RelativeLayout.LayoutParams relativeLayoutParams;
            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            TextView subjectView = new TextView(this);
            TextView dateView = new TextView(this);
            TextView fromView = new TextView(this);
            fromView.setId(1);
            emailInnerView.addView(fromView, relativeLayoutParams);
            subjectView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    System.out.println("Clicked on ");
                }
            });
            subjectView.setPadding(10, 5, 10, 5);
            subjectView.setTextSize(14);
            subjectView.setText(abbreviateString(remoteMail.get(i).getSubject(), 40));
            fromView.setText(abbreviateString(remoteMail.get(i).getFromPersonal(), 30));

            SimpleDateFormat formatter = new SimpleDateFormat("M/d h:mm a");
            String s = formatter.format(remoteMail.get(i).getDate());

            relativeLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            dateView.setText(s);
            dateView.setGravity(5);
            dateView.setTextAlignment(1);
            fromView.setTextSize(20);

            relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, fromView.getId());
            relativeLayoutParams.addRule(RelativeLayout.ALIGN_TOP, fromView.getId()); // added top alignment rule

            emailInnerView.addView(dateView, relativeLayoutParams);
            emailInnerView.setPadding(10, 5, 10, 5);
            emailTextContainer.addView(emailInnerView);
            emailTextContainer.addView(subjectView);
            emailTextContainer.setPadding(15, 15, 15, 15);

            emailView.setGravity(16); //Center Vertical
            CheckBox checkBoxView = new CheckBox(this);
            emailView.addView(checkBoxView);
            emailView.addView(emailTextContainer);
            emailView.setPadding(10, 10, 10, 10);
            linearLayout.addView(emailView, 0, listView);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_mail);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        myToolbar.setTitle("NJIT Mail");
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        linearLayout = (LinearLayout) findViewById(R.id.linear_layout_emails);
        LayoutInflater inflater = LayoutInflater.from(this);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END);
        Window window = this.getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        list = new ListView(this);
        listView = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT);
        progress = new ProgressDialog(this);
//        progress.setTitle("Loading");
        progress.setMessage("Loading emails...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean check = false;
                while (true) {
                    try {
                        remoteMail = getMessages(new Authentication(), check);

                        // The check is to get all initial emails loaded into the structure
                        // Then it is set to true so that only future unread emails are loaded
                        // to improve throughput.
                        if (!check) check = true;

                        if (remoteMail.size() > 0) {

                            // Add drawing step here
                            linearLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    reDrawLocalMail();
                                    progress.dismiss();
                                }
                            });
                        }

                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        Log.v(TAG, e.getMessage());
                    }
                }
            }
        }).start();

    }

    public List<Mail> getMessages(Authentication authentication, boolean checked) {
        try {
            Session emailSession = Session.getDefaultInstance(authentication.getIMAPProperties());
            Store store = emailSession.getStore("imaps");
            store.connect("imap.gmail.com", authentication.getUsername(), authentication.getPassword());
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
            Message messages[] = emailFolder.getMessages();
            List<Mail> messageList = new ArrayList<Mail>();
            for (int i = 0; i < messages.length; i++) {
                if (checked && messages[i].isSet(Flags.Flag.SEEN)) continue;

                Mail mail = new Mail();
                try {
                    messages[i].setFlag(Flags.Flag.SEEN, true);
                    mail.setSubject(messages[i].getSubject());
                    mail.setMessage(messages[i].getContent().toString());
                    mail.setDate(messages[i].getReceivedDate());
                    InternetAddress from = (InternetAddress) messages[i].getFrom()[0];
                    mail.setFromClient(from.getAddress());
                    mail.setFromPersonal(from.getPersonal());
                } catch (MessagingException | IOException e) {
                    Log.v(TAG, e.getMessage());
                }
                messageList.add(mail);
            }

            emailFolder.close(true);
            store.close();

            return messageList;
        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
            return null;
        }
    }


}
