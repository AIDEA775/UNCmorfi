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
        val name = getDateName()
        val num = getDateNumber()
        val menu = TextUtils.join(", ", food)
        return "$name $num:\n$menu\n\n#UNCmorfi"
    }

    fun getDateNumber(): String {
        val mDateNumber = SimpleDateFormat("dd", Locale.getDefault())
        return mDateNumber.format(date)
    }

    fun getDateName(): String {
        val mDateName = SimpleDateFormat("EEE", Locale.getDefault())
        return mDateName.format(date)
    }
}