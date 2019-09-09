package com.uncmorfi.reservations

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.get
import com.google.android.material.chip.Chip
import com.uncmorfi.R
import kotlinx.android.synthetic.main.view_week_days.view.*
import java.text.DateFormatSymbols
import java.util.*

class WeekDaysView : LinearLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int): super(context, attrs, defStyleAttr)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_week_days, this, true)
        orientation = VERTICAL
    }

    // Fixme? hay una mejor forma de hacer esto
    fun initDays() {
        val days = DateFormatSymbols.getInstance().shortWeekdays

        // MONDAY=2, por eso el [i-2], así indexamos desde 0
        for (i in Calendar.MONDAY..Calendar.FRIDAY) {
            val chip = week_days_group[i-2] as Chip
            chip.text = days[i].capitalize()
        }
    }

    fun setChecked(index: Int, value: Boolean) {
        val chip = week_days_group[index-2] as Chip
        chip.isChecked = value
    }

    fun getChecked(index: Int): Boolean {
        val chip = week_days_group[index-2] as Chip
        return chip.isChecked
    }

    fun setHeader(id: Int) {
        week_days_header.text = context.getString(id)
    }
}
