package com.uncmorfi.menu

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uncmorfi.R
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.menu_item.view.*

internal class MenuAdapter (private val mContext: Context,
                            private var mMenuList: List<DayMenu>,
                            private val mClickListener: (DayMenu) -> Unit,
                            private val mLongClickListener: (DayMenu) -> Unit) :
        RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {

    internal inner class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(day: DayMenu) {
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

            itemView.setBackgroundColor(colorBack)
            itemView.menuDayNumber.setTextColor(colorDay)
            itemView.menuDayName.setTextColor(colorDay)

            itemView.menuFood1.setTextColor(colorFood)
            itemView.menuFood2.setTextColor(colorFood)
            itemView.menuFood3.setTextColor(colorFood)

            itemView.menuDayNumber.text = day.getDateNumber()
            itemView.menuDayName.text = day.getDateName()

            itemView.menuFood1.text = day.food[0]
            itemView.menuFood2.text = day.food[1]
            itemView.menuFood3.text = day.food[2]

            itemView.setOnClickListener { mClickListener(day) }
            itemView.setOnLongClickListener { mLongClickListener(day); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_item, parent, false)
        return MenuItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val day = mMenuList[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int {
        return mMenuList.size
    }

    fun updateMenu(menuList: List<DayMenu>) {
        mMenuList = menuList
        notifyDataSetChanged()
    }
}