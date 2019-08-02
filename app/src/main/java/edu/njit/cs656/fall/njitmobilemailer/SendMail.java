package edu.njit.cs656.fall.njitmobilemailer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;
import edu.njit.cs656.fall.njitmobilemailer.email.Mail;
import edu.njit.cs656.fall.njitmobilemailer.email.Send;

public class SendMail extends AppCompatActivity {

    public static final String TAG = "SendMail";
    private Toolbar toolbar;

    private String getTo() {
        EditText item = (EditText) findViewById(R.id.to_editText);
        return item.getText().toString();
    }

    private String getBody() {
        EditText item = (EditText) findViewById(R.id.body_editText);
        return item.getText().toString();
    }

    private String getSubject() {
        EditText item = (EditText) findViewById(R.id.subject_editText);
        return item.getText().toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.send_mail_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_mail);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_send:
                Thread sender = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Mail letter = new Mail();
                        letter.setSubject(getSubject());
                        letter.setMessage(getBody());
                        try {
                            Authentication auth = new Authentication();
                            letter.setFromClient(auth.getUsername(getBaseContext()));
                            letter.setToClient(getTo());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Send.Email(letter, getBaseContext());
                    }
                });

                sender.start();
                try {
                    sender.join();
                } catch (Exception e) {
                    Log.v(TAG, "Error on interruption.");
                }

                onBackPressed();
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
