package com.uncmorfi.ui.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.shared.DateUtils.FORMAT_ARG2
import com.uncmorfi.shared.DateUtils.FORMAT_ARG3
import com.uncmorfi.shared.colorOf
import com.uncmorfi.shared.updateVisibility
import kotlinx.android.synthetic.main.view_day_menu.view.*
import java.time.LocalDate

class DayMenuView @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attr, defStyleAttr) {
    private lateinit var mDayMenu: DayMenu

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day_menu, this, true)
    }

    fun setDayMenu(menu: DayMenu) {
        mDayMenu = menu
        val offset = menu.date.compareTo(LocalDate.now())

        val colorDay: Int
        val colorFood: Int
        val colorBack: Int
        when {
            offset < 0 -> {
                colorDay = context.colorOf(R.color.primary)
                colorFood = context.colorOf(R.color.secondary_text)
                colorBack = context.colorOf(R.color.white)
            }
            offset == 0 -> {
                colorDay = context.colorOf(R.color.white)
                colorFood = context.colorOf(R.color.white)
                colorBack = context.colorOf(R.color.accent)

            }
            else -> {
                colorDay = context.colorOf(R.color.primary_dark)
                colorFood = context.colorOf(R.color.primary_text)
                colorBack = context.colorOf(R.color.white)
            }
        }

        setBackgroundColor(colorBack)
        menuDayNumber.setTextColor(colorDay)
        menuDayName.setTextColor(colorDay)

        menuFood1.setTextColor(colorFood)
        menuFood2.setTextColor(colorFood)
        menuFood3.setTextColor(colorFood)
        menuFood4.setTextColor(colorFood)
        menuFood5.setTextColor(colorFood)
        menuFood6.setTextColor(colorFood)

        menuDayNumber.text = menu.date.format(FORMAT_ARG2)
        menuDayName.text = menu.date.format(FORMAT_ARG3)

        menuFood1.text = menu.food.getOrNull(0)
        menuFood1.updateVisibility()

        // TODO: mejorar esto
        menuFood2.text = menu.food.getOrNull(1)
        menuFood2.updateVisibility()
        menuFood3.text = menu.food.getOrNull(2)
        menuFood3.updateVisibility()
        menuFood4.text = menu.food.getOrNull(3)
        menuFood4.updateVisibility()
        menuFood5.text = menu.food.getOrNull(4)
        menuFood5.updateVisibility()
        menuFood6.text = menu.food.getOrNull(5)
        menuFood6.updateVisibility()
    }
}