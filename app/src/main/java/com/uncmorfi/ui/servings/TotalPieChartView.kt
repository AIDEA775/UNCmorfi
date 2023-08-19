package com.uncmorfi.ui.servings

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.shared.colorOf
import java.util.*

class TotalPieChartView: PieChart {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setNoDataText(context.getString(R.string.servings_chart_empty))
        description.isEnabled = false
        legend.isEnabled = false

        isDrawHoleEnabled = true
        holeRadius = 90f
        setHoleColor(Color.TRANSPARENT)

        setDrawCenterText(true)
        setDrawEntryLabels(false)

        isHighlightPerTapEnabled = false
    }

    fun set(data: List<Serving>) {
        val total = data.fold(0) { subtotal, item -> subtotal + item.serving }

        val percent = (total.toFloat()/ FOOD_RATIONS)*100f
        val percentPie = minOf(percent, 100f)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentPie, "Total"))
        entries.add(PieEntry(100f - percentPie, "Empty"))

        val dataSet = PieDataSet(entries, "").style()

        centerText = generatePieChartText(context, total, percent)
        update(dataSet)
    }

    private fun PieDataSet.style(): PieDataSet {
        val colors = ArrayList<Int>()
//        colors.add(context.colorOf(R.color.accent))
        colors.add(Color.TRANSPARENT)
        this.colors = colors
        this.setDrawValues(false)
        return this
    }

    private fun update(dataSet: PieDataSet) {
        data = PieData(dataSet)
        invalidate()
    }

    /*
     * Perdón, esta función es horrible. Pero funciona.
     */
    private fun generatePieChartText(context: Context, total: Int, percent: Float) : SpannableString {
        val pText = "%.2f%%".format(percent)
        val tText = context.getString(R.string.servings_rations_title).format(total, FOOD_RATIONS)
        val s = SpannableString("$pText\n$tText")

        s.setSpan(RelativeSizeSpan(3f), 0, pText.length, 0)
//        s.setSpan(ForegroundColorSpan(context.colorOf(
//                if (total > FOOD_RATIONS - FOOD_LIMIT) R.color.accent else R.color.primary_dark)),
//                0, pText.length, 0)

        s.setSpan(StyleSpan(Typeface.NORMAL), pText.length, s.length, 0)
        s.setSpan(ForegroundColorSpan(Color.BLACK), pText.length, s.length, 0)

        return s
    }

    fun setTouchListener(tapListener: () -> Unit) {
        onChartGestureListener = object : OnChartGestureListener {
            override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
            override fun onChartSingleTapped(me: MotionEvent?) {
                tapListener()
            }
            override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
        }
    }

    companion object {
        private const val FOOD_RATIONS = 1500
        private const val FOOD_LIMIT = 200
    }
}