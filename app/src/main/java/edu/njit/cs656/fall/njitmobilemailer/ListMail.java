package edu.njit.cs656.fall.njitmobilemailer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import edu.njit.cs656.fall.njitmobilemailer.auth.Authentication;
import edu.njit.cs656.fall.njitmobilemailer.email.Mail;


public class ListMail extends AppCompatActivity {

    private static final String TAG = "ListMail";
    private TableRow[] tableRows;
    private TableLayout.LayoutParams tableParams;
    private TableRow.LayoutParams rowParams;
    private LayoutInflater inflater;
    private TableLayout tableLayout;
    private ListMail reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_mail);

        tableLayout = (TableLayout) findViewById(R.id.list_email_table);


        reference = this;
        inflater = LayoutInflater.from(this);

        new Updater().execute(new Authentication());
    }


    public class Updater extends AsyncTask<Authentication, Integer, List<Mail>> {

        @Override
        protected void onPostExecute(List<Mail> messages) {
            super.onPostExecute(messages);

            tableRows = new TableRow[messages.size()];
            tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            rowParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);

            for (int i = 0; i < messages.size(); i++) {
                tableRows[i] = new TableRow(reference);
                View customView = inflater.inflate(R.layout.data_item, tableRows[i], false);
                ((TextView) customView.findViewById(R.id.view_subject)).setText(messages.get(i).getSubject());
                tableRows[i].addView(customView, rowParams);

                tableLayout.addView(tableRows[i], tableParams);
            }
        }

        @Override
        protected List<Mail> doInBackground(Authentication... authentication) {

            try {
                Session emailSession = Session.getDefaultInstance(authentication[0].getIMAPProperties());

                Store store = emailSession.getStore("imaps");

                store.connect("imap.gmail.com", authentication[0].getUsername(), authentication[0].getPassword());

                Folder emailFolder = store.getFolder("INBOX");

                emailFolder.open(Folder.READ_WRITE);
                Message[] messages = emailFolder.getMessages();
                List<Mail> messageList = new ArrayList<Mail>();


                for (int i = 0; i < messages.length; i++) {
                    Mail mail = new Mail();
                    mail.setSubject(messages[i].getSubject());
                    messageList.add(mail);
                }

                emailFolder.close(false);
                store.close();

                return messageList;
            } catch (MessagingException e) {
                return null;
            }
        }
    }
}
