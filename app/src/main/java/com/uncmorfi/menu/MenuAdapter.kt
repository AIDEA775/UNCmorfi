package com.uncmorfi.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.helpers.compareToToday
import com.uncmorfi.helpers.toFormat
import com.uncmorfi.models.DayMenu
import kotlinx.android.synthetic.main.menu_item.view.*

internal class MenuAdapter (private val mContext: Context,
                            private val mClickListener: (DayMenu) -> Unit,
                            private val mLongClickListener: (DayMenu) -> Unit) :
                            RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {
    private var mMenuList: List<DayMenu> = emptyList()

    internal inner class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(day: DayMenu) {
            val offset = day.date.compareToToday()

            val colorDay: Int
            val colorFood: Int
            val colorBack: Int
            when {
                offset < 0 -> {
                    colorDay = mContext.colorOf(R.color.primary)
                    colorFood = mContext.colorOf(R.color.secondary_text)
                    colorBack = mContext.colorOf(R.color.white)
                }
                offset == 0 -> {
                    colorDay = mContext.colorOf(R.color.white)
                    colorFood = mContext.colorOf(R.color.white)
                    colorBack = mContext.colorOf(R.color.accent)

                }
                else -> {
                    colorDay = mContext.colorOf(R.color.primary_dark)
                    colorFood = mContext.colorOf(R.color.primary_text)
                    colorBack = mContext.colorOf(R.color.white)
                }
            }

            itemView.menuCard.setCardBackgroundColor(colorBack)
            itemView.menuDayNumber.setTextColor(colorDay)
            itemView.menuDayName.setTextColor(colorDay)

            itemView.menuFood1.setTextColor(colorFood)
            itemView.menuFood2.setTextColor(colorFood)
            itemView.menuFood3.setTextColor(colorFood)

            itemView.menuDayNumber.text = day.date.toFormat("dd")
            itemView.menuDayName.text = day.date.toFormat("EEEE").capitalize()

            itemView.menuFood1.text = day.food.getOrNull(0)
            itemView.menuFood2.text = day.food.getOrNull(1)
            itemView.menuFood3.text = day.food.getOrNull(2)

            itemView.menuCard.setOnClickListener { mClickListener(day) }
            itemView.menuCard.setOnLongClickListener { mLongClickListener(day); true }
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