package com.uncmorfi.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.uncmorfi.R
import com.uncmorfi.models.DayMenu
import com.uncmorfi.shared.colorOf
import com.uncmorfi.shared.compareToToday
import com.uncmorfi.shared.toFormat
import com.uncmorfi.shared.updateVisibility
import kotlinx.android.synthetic.main.view_day_menu.view.*

class DayMenuView : RelativeLayout {
    private lateinit var mDayMenu: DayMenu

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_day_menu, this, true)
    }

    fun setDayMenu(menu: DayMenu) {
        mDayMenu = menu
        val offset = menu.date.compareToToday()

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

        menuDayNumber.text = menu.date.toFormat("dd")
        menuDayName.text = menu.date.toFormat("EEEE").capitalize()

        menuFood1.text = menu.food.getOrNull(0)
        menuFood1.updateVisibility()
        menuFood2.text = menu.food.getOrNull(1)
        menuFood2.updateVisibility()
        menuFood3.text = menu.food.getOrNull(2)
        menuFood3.updateVisibility()
    }
}