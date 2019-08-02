package edu.njit.cs656.fall.njitmobilemailer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;
import edu.njit.cs656.fall.njitmobilemailer.email.Mail;
import edu.njit.cs656.fall.njitmobilemailer.interfaces.Listener;

public class ListMail extends AppCompatActivity {

    public static final String TAG = "ListMail";
    private List<Mail> remoteMail = new ArrayList<Mail>();
    private List<Mail> localMail = new ArrayList<Mail>();
    private List<TextView> elementList = new ArrayList<TextView>();
    private LinearLayout linearLayout;
    private ListView list;
    private ListView.LayoutParams listView;
    private ProgressDialog progress;
    private int numOfEmails = 0;
    private int numOfUnreadEmails = 0;


    private String abbreviateString(String s, int maxLength){
        // If string is longer than max length, truncate the string
        // and add '...'
        if (s == null) return null;

        if (s.length() > maxLength){
            return s.substring(0, Math.min(s.length(), maxLength)) + "...";
        }
        else {
            return s;
        }
    }

    // callback for setting up indices
    // instead of using final int
    public void setUpListener(int index, Listener listener) {
        listener.setUp(index);
    }

    public void DrawLocalMail() {
        linearLayout.removeViewsInLayout(0, localMail.size());

        for (int i = 0; i < localMail.size(); i++) {
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

            setUpListener(i, new Listener() {
                @Override
                public void setUp(int index) {
                    emailTextContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ReadMail.class);
                            intent.putExtra("id", localMail.get(index).getIndex());
                            intent.putExtra("index", index);
                            //intent.putExtra("hash", localMail.get(index).getContentHash());
                            intent.putExtra("subject", localMail.get(index).getSubject());
                            intent.putExtra("from", localMail.get(index).getFromPersonal());
                            intent.putExtra("date", localMail.get(index).getDate().getTime());
                            startActivityForResult(intent, 1);
                        }
                    });
                }
            });


            subjectView.setPadding(10, 5, 10, 5);
            subjectView.setTextSize(14);
            subjectView.setText(abbreviateString(localMail.get(i).getSubject(), 40));
            fromView.setText(abbreviateString(localMail.get(i).getFromPersonal(), 20));

            SimpleDateFormat formatter = new SimpleDateFormat("M/d h:mm a");
            String s = formatter.format(localMail.get(i).getDate());

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

    @SuppressLint("ResourceType")
    public void reDrawLocalMail() {
        //list = new ListView(this);
    localMail.clear();
	localMail.addAll(remoteMail);
	linearLayout.removeAllViews();
        for (int i = 0; i < localMail.size(); i++) {
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

            setUpListener(((localMail.size() - remoteMail.size()) + i), new Listener() {
                @Override
                public void setUp(int index) {
                    emailTextContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getApplicationContext(), ReadMail.class);
                            intent.putExtra("id", localMail.get(index).getIndex());
                            intent.putExtra("index", index);
                            //intent.putExtra("hash", localMail.get(index).getContentHash());
                            intent.putExtra("subject", localMail.get(index).getSubject());
                            intent.putExtra("from", localMail.get(index).getFromPersonal());
                            intent.putExtra("date", localMail.get(index).getDate().getTime());
                            startActivityForResult(intent, 1);
                        }
                    });
                }
            });


            subjectView.setPadding(10, 5, 10, 5);
            subjectView.setTextSize(14);
            if (!localMail.get(i).getIsRead()) {
                subjectView.setTypeface(null, Typeface.BOLD);
                fromView.setTypeface(null, Typeface.BOLD);
                dateView.setTypeface(null, Typeface.BOLD);
            }
            else {
                subjectView.setTypeface(null, Typeface.NORMAL);
                fromView.setTypeface(null, Typeface.NORMAL);
                dateView.setTypeface(null, Typeface.NORMAL);
            }
            subjectView.setText(abbreviateString(localMail.get(i).getSubject(), 40));
            fromView.setText(abbreviateString(localMail.get(i).getFromPersonal(), 20));

            SimpleDateFormat formatter = new SimpleDateFormat("M/d h:mm a");
            String s = formatter.format(localMail.get(i).getDate());

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) return;

        int index = data.getExtras().getInt("index");
        Log.v(TAG, "Result Check: " + index);
        deleteMessage(index);
        DrawLocalMail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_mail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_compose:
                Intent intent = new Intent(getApplicationContext(), SendMail.class);
                startActivity(intent);
                break;
            default:
                break;
        }

        return true;
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
        progress.setMessage("Loading emails...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean check = false;
                while (true) {
                    try {
                        remoteMail = getMessages(new Authentication());

                        // The check is to get all initial emails loaded into the structure
                        // Then it is set to true so that only future unread emails are loaded
                        // to improve throughput.

                        // This prevents app from crashing if the inbox is empty
                        int size = 0;
                        int unread = 0;
                        try{
                            size = remoteMail.size();
                            for (int i = 0; i < size; i++){
                                if (!remoteMail.get(i).getIsRead()){
                                    unread = unread + 1;
                                }
                            }
                        }
                        catch (Exception e) {

                            Log.v(TAG, "remoteMail is not null.");
                        }

                        // Only reDraw if number of emails changed
                        if (numOfEmails != size || numOfUnreadEmails != unread) {

                            // Add drawing step here
                            linearLayout.post(new Runnable() {
                                @Override
                                public void run() {
                                    reDrawLocalMail();
                                }
                            });
                        }
                        progress.dismiss();
                        numOfEmails = size;
                        numOfUnreadEmails = unread;
                        // Thread sleep time of 5 seconds
                        // TODO we should change this to a more appropriate number for production
                        Thread.sleep(5 * 1000);
                    } catch (Exception e) {
                        Log.v(TAG, e.getMessage());
                    }
                }
            }
        }).start();

    }

    public void deleteMessage(int messageIndex) {
        Log.v(TAG, "Removing: " + localMail.get(messageIndex - 1).getSubject());
        localMail.remove(messageIndex - 1);
    }

    public List<Mail> getMessages(Authentication authentication) {
        try {
            Session emailSession = Session.getDefaultInstance(authentication.getIMAPProperties());
            Store store = emailSession.getStore("imaps");
            store.connect("imap.gmail.com", authentication.getUsername(getBaseContext()), authentication.getPassword(getBaseContext()));
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_WRITE);
            emailFolder.expunge();

            Message messages[] = emailFolder.getMessages();

            List<Mail> messageList = new ArrayList<>();

            for (int i = 0; i < messages.length; i++) {
                Mail mail = new Mail();
                try {
//                    messages[i].setFlag(Flags.Flag.SEEN, true);
                    mail.setSubject(messages[i].getSubject());
                    mail.setIndex(messages[i].getMessageNumber());
                    mail.setDate(messages[i].getReceivedDate());
                    InternetAddress from = (InternetAddress) messages[i].getFrom()[0];
                    mail.setFromClient(from.getAddress());
                    mail.setFromPersonal(from.getPersonal());
                    mail.setIsRead(messages[i].isSet(Flags.Flag.SEEN));

                } catch (Exception e) {
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
