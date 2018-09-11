package com.uncmorfi.helpers;

import com.uncmorfi.menu.DayMenu;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public abstract class ParserHelper {

    public static Date stringToDate(String string) {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        try {
            return dateFormat.parse(string);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class MenuDayComparator implements Comparator<DayMenu>
    {
        public int compare(DayMenu left, DayMenu right) {
            return left.getDate().compareTo(right.getDate());
        }
    }
}