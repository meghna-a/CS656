package edu.njit.cs656.fall.njitmobilemailer.email;

import java.util.Comparator;

/**
 * Created by Eugen on 11/9/2017.
 */

public class MailComparator implements Comparator<Mail> {

    @Override
    public int compare(Mail first, Mail second) {
        return (int) (first.getEpoch() - second.getEpoch());
    }
}
