package com.wifiprotector.android.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CertificateValidityExtractor {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d HH:mm:ss yyyy zzz");
    private Date validNotBefore;
    private Date validNotAfter;

    public CertificateValidityExtractor(String certificateContent) {
        validNotBefore = parse(certificateContent, "Not Before");
        validNotAfter = parse(certificateContent, "Not After");
    }

    public Date getValidNotBefore() {
        return validNotBefore;
    }

    public Date getValidNotAfter() {
        return validNotAfter;
    }

    private Date parse(String certificateContent, String lineBeginning) {
        final int lineIndex = certificateContent.indexOf(lineBeginning);
        final int dateIndex = certificateContent.indexOf(":", lineIndex) + 1;
        final int eolIndex = certificateContent.indexOf("\n", dateIndex);
        String dateStr = certificateContent.substring(dateIndex, eolIndex).trim();

        try {
            return DATE_FORMAT.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }
}
