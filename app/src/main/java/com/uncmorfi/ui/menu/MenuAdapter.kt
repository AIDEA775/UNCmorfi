package com.uncmorfi.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.databinding.ItemMenuBinding

internal class MenuAdapter(
    private val onClick: (DayMenu) -> Unit,
    private val onLongClick: (DayMenu) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder>() {

    private val menu = mutableListOf<DayMenu>()

    internal inner class MenuItemViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        private val binding = ItemMenuBinding.bind(v)

        fun bind(day: DayMenu) {
            binding.menuView.apply {
                setDayMenu(day)
                setOnClickListener { onClick(day) }
                setOnLongClickListener { onLongClick(day); true }
            }
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