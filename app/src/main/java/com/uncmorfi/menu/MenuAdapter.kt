package com.uncmorfi.menu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.models.DayMenu
import kotlinx.android.synthetic.main.item_menu.view.*

internal class MenuAdapter (private val mContext: Context,
                            private val mClickListener: (DayMenu) -> Unit,
                            private val mLongClickListener: (DayMenu) -> Unit) :
                            RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {
    private var mMenuList: List<DayMenu> = emptyList()

    internal inner class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(day: DayMenu) {
            itemView.menuView.setDayMenu(day)
            itemView.menuCard.setOnClickListener { mClickListener(day) }
            itemView.menuCard.setOnLongClickListener { mLongClickListener(day); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_menu, parent, false)
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