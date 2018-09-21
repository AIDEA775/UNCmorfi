package com.uncmorfi.menu

import com.uncmorfi.helpers.ParserHelper

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.Collections
import java.util.Date

class DayMenu internal constructor(date: String, foods: JSONArray) {
    var date: Date? = null
    private lateinit var mFood: MutableList<String>

    val food: List<String>
        get() = mFood

    init {
        try {
            this.date = ParserHelper.stringToDate(date)
            mFood = ArrayList()
            for (i in 0 until foods.length()) {
                mFood.add(foods.getString(i))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {
        fun fromJson(source: String): List<DayMenu> {
            val menuList = ArrayList<DayMenu>()

            try {
                val result = JSONObject(source)
                val week = result.getJSONObject("menu")

                val keys = week.keys()
                while (keys.hasNext()) {
                    val key = keys.next() as String
                    menuList.add(DayMenu(key, week.getJSONArray(key)))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            Collections.sort(menuList, ParserHelper.MenuDayComparator())
            return menuList
        }
    }
}