package com.uncmorfi.menu

import android.text.TextUtils
import com.uncmorfi.helpers.ParserHelper
import com.uncmorfi.helpers.toArray
import com.uncmorfi.helpers.toDate
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun String?.toDayMenuList(): List<DayMenu> {
    val menuList = ArrayList<DayMenu>()

    if (this == null) return menuList

    try {
        val result = JSONObject(this)
        val week = result.getJSONObject("menu")

        val keys = week.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            val foods = week.getJSONArray(key)
            menuList.add(DayMenu(key, *foods.toArray()))
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }

    Collections.sort(menuList, ParserHelper.MenuDayComparator())
    return menuList
}

class DayMenu (date: String, vararg foods: String) {
    val date: Date? = date.toDate()
    val food: List<String> = foods.asList()

    override fun toString() : String {
        val name = getDateName("EEEE").capitalize()
        val num = getDateNumber("d")
        val menu = TextUtils.join(", ", food)
        return "$name $num:\n$menu"
    }

    fun getDateNumber(pattern: String = "dd"): String {
        val mDateNumber = SimpleDateFormat(pattern, Locale.getDefault())
        return mDateNumber.format(date)
    }

    fun getDateName(pattern: String = "E"): String {
        val mDateName = SimpleDateFormat(pattern, Locale.getDefault())
        return mDateName.format(date)
    }
}