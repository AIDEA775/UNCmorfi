package com.uncmorfi.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import kotlinx.android.synthetic.main.item_menu.view.*

internal class MenuAdapter(
    private val onClick: (DayMenu) -> Unit,
    private val onLongClick: (DayMenu) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {

    private val menu = mutableListOf<DayMenu>()

    internal inner class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        fun bind(day: DayMenu) {
            itemView.menuView.setDayMenu(day)
            itemView.menuView.setOnClickListener { onClick(day) }
            itemView.menuView.setOnLongClickListener { onLongClick(day); true }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return MenuItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
        val day = menu[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int {
        return menu.size
    }

    fun updateMenu(menuList: List<DayMenu>) {
        notifyItemRangeRemoved(0, menu.size)
        menu.clear()
        menu.addAll(menuList)
        notifyItemRangeInserted(0, menu.size)
    }
}