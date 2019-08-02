package edu.njit.cs656.fall.njitmobilemailer.email;

/**
 * Created by Eugen on 9/20/2017.
 */

import org.apache.commons.validator.routines.EmailValidator;

import java.util.Date;
import java.util.List;

public class Mail { // TODO: I might delete this later on due to javax already having Message class

    private String fromClient = "";
    private String fromPersonal = "";
    private String toClient = "";
    private String subject = "";
    private List<String> ccClient;
    private String message = "";
    private Date date = new Date();
    private Boolean isRead = false;
    private int index;
    private String contentHash = "";

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setIsRead(Boolean read){
        this.isRead = read;
    }

    public Boolean getIsRead(){
        return isRead;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public long getEpoch() {
        return date.getTime();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFromPersonal() {
        return fromPersonal;
    }

    public void setFromPersonal(String fromPersonal) {
        this.fromPersonal = fromPersonal;
    }

    public String getFromClient() {

        return fromClient;
    }

    public void setFromClient(final String fromClient)  throws Exception {
        EmailValidator validator = EmailValidator.getInstance();
        if(validator.isValid(fromClient)) {
            this.fromClient = fromClient;
        } else {
            throw new Exception("ERR: Invalid email.");
        }
    }


    public String getToClient() {

        return toClient;
    }

    public void setToClient(final String toClient)  throws Exception  {
        EmailValidator validator = EmailValidator.getInstance();
        if(validator.isValid(toClient)) {
            this.toClient = toClient;
        } else {
            throw new Exception("ERR: Invalid email.");
        }
    }

    public List<String> getCcClient() {

        return ccClient;
    }

    public void addCcClient(final String ccClient) throws Exception {
        EmailValidator validator = EmailValidator.getInstance();
        if(validator.isValid(toClient)) {
            this.ccClient.add(ccClient);
        } else {
            throw new Exception("ERR: Invalid email.");
        }
    }


    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

    public String getMessage() {

        return message;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("Subject: %s\nMessage: %s\n", this.getSubject(), this.getMessage().substring(0, this.getMessage().length() > 100 ? 100 : this.getMessage().length() - 1));
    }

}
