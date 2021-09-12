package com.swordrunner.swordrunner.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtilss {
    long nweek = 1000 * 24 * 60 * 60*7;
    long nday = 1000 * 24 * 60 * 60;
    long nhour = 1000 * 60 * 60;
    long nminute = 1000 * 60;
    long nusecond = 1000;
    private Locale TIMEZONE = Locale.GERMANY;
    private String PATTERN="yyyy-MM-dd HH:mm:ss";

    //
    public Date stringToDate(String dateStr){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(PATTERN);
        try {
            Date date=simpleDateFormat.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Date timeStampToDate(String timeStamp){
        Long timestamp = Long.parseLong(timeStamp);
        String date = new SimpleDateFormat(PATTERN, TIMEZONE).format(new Date(timestamp));
        return stringToDate(date);
    }

    public String dateToString(Date date){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat(PATTERN);
        return simpleDateFormat.format(date);
    }



}
