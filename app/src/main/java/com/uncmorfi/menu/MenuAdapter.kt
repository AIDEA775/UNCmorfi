package com.uncmorfi.menu

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.uncmorfi.R
import java.text.SimpleDateFormat
import java.util.*

class MenuAdapter internal constructor(private val mContext: Context, menu: List<DayMenu>) :
        RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {
    private val mDateNumber = SimpleDateFormat("dd", Locale.getDefault())
    private val mDateName = SimpleDateFormat("EEE", Locale.getDefault())
    var menuList: List<DayMenu> = menu

    class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var dayNumberText: TextView = v.findViewById(R.id.menu_day_number)
        var dayNameText: TextView = v.findViewById(R.id.menu_day_name)
        var food1Text: TextView = v.findViewById(R.id.menu_food1)
        var food2Text: TextView = v.findViewById(R.id.menu_food2)
        var food3Text: TextView = v.findViewById(R.id.menu_food3)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_item, parent, false)
        return MenuItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val day = menuList[position]

        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val offset = fmt.format(day.date).compareTo(fmt.format(Date()))

        val colorDay: Int
        val colorFood: Int
        val colorBack: Int
        when {
            offset < 0 -> {
                colorDay = ContextCompat.getColor(mContext, R.color.secondary_text)
                colorFood = ContextCompat.getColor(mContext, R.color.secondary_text)
                colorBack = ContextCompat.getColor(mContext, R.color.white)
            }
            offset == 0 -> {
                colorDay = ContextCompat.getColor(mContext, R.color.white)
                colorFood = ContextCompat.getColor(mContext, R.color.white)
                colorBack = ContextCompat.getColor(mContext, R.color.accent)

            }
            else -> {
                colorDay = ContextCompat.getColor(mContext, R.color.primary_text)
                colorFood = ContextCompat.getColor(mContext, R.color.primary_text)
                colorBack = ContextCompat.getColor(mContext, R.color.white)
            }
        }

        holder.itemView.setBackgroundColor(colorBack)
        holder.dayNumberText.setTextColor(colorDay)
        holder.dayNameText.setTextColor(colorDay)

        holder.food1Text.setTextColor(colorFood)
        holder.food2Text.setTextColor(colorFood)
        holder.food3Text.setTextColor(colorFood)

        holder.dayNumberText.text = mDateNumber.format(day.date)
        holder.dayNameText.text = mDateName.format(day.date)

        holder.food1Text.text = day.food[0]
        holder.food2Text.text = day.food[1]
        holder.food3Text.text = day.food[2]
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    fun updateMenu(menuList: List<DayMenu>) {
        this.menuList = menuList
        notifyDataSetChanged()
    }
}