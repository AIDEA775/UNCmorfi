package com.uncmorfi.helpers;

import com.github.mikephil.charting.data.Entry;
import com.uncmorfi.menu.DayMenu;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class ParserHelper {

    public static Date stringToDate(String string) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long clearDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis()/1000;
    }

    public static class MenuDayComparator implements Comparator<DayMenu>
    {
        public int compare(DayMenu left, DayMenu right) {
            return left.getDate().compareTo(right.getDate());
        }
    }

    public static class CounterEntryComparator implements Comparator<Entry>
    {
        public int compare(Entry left, Entry right) {
            return new Date((long) left.getX()).compareTo(new Date((long)right.getX()));
        }
    }
}