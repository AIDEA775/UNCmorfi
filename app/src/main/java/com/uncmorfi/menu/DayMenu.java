package com.uncmorfi.menu;

import com.uncmorfi.helpers.ParserHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class DayMenu {
    private Date mDate;
    private List<String> mFood;

    public Date getDate() {
        return mDate;
    }

    public List<String> getFood() {
        return mFood;
    }

    DayMenu(String date, JSONArray foods) {
        try {
            mDate = ParserHelper.stringToDate(date);
            mFood = new ArrayList<>();
            for (int i = 0; i < foods.length(); i++) {
                mFood.add(foods.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static List<DayMenu> fromJson(String source) {
        ArrayList<DayMenu> menuList = new ArrayList<DayMenu>();

        try {
            JSONObject result = new JSONObject(source);
            JSONObject week = result.getJSONObject("menu");

            Iterator<?> keys = week.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                menuList.add(new DayMenu(key, week.getJSONArray(key)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return menuList;
    }
}