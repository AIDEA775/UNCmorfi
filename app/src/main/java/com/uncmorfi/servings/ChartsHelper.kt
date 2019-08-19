package com.uncmorfi.servings

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.helpers.toFormat
import com.uncmorfi.models.Serving
import java.util.*

enum class ChartStyle {
    POINTS,
    ESTIMATE,
    RATIONS,
    CUMULATIVE
}

fun LineChart.init(context: Context) {
    this.setNoDataText("")
    this.isScaleYEnabled = false
    this.description = null

    this.setDrawGridBackground(false)
    this.setDrawBorders(false)

    this.isHighlightPerTapEnabled = false
    this.isHighlightPerDragEnabled = false

    this.axisLeft.axisMinimum = 0f
    this.axisLeft.setDrawAxisLine(false)
    this.axisRight.isEnabled = false
    this.axisRight.setDrawGridLines(false)

    this.xAxis.setDrawAxisLine(false)

    this.legend.textSize = 14f
    this.legend.textColor = context.colorOf(R.color.primary_text)

    val xAxis = this.xAxis
    xAxis.valueFormatter = HourAxisValueFormatter()
    xAxis.position = XAxis.XAxisPosition.BOTTOM
}

fun PieChart.init(context: Context) {
    this.setNoDataText(context.getString(R.string.counter_chart_empty))
    this.description.isEnabled = false
    this.legend.isEnabled = false

    this.isDrawHoleEnabled = true
    this.holeRadius = 90f
    this.setHoleColor(Color.TRANSPARENT)

    this.setDrawCenterText(true)
    this.setDrawEntryLabels(false)

    this.isHighlightPerTapEnabled = false
}

fun LineChart.update(dataSet: LineDataSet, xMinRange: Float = 200f) {
    this.data = LineData(dataSet)
    this.setVisibleXRangeMinimum(xMinRange)
    this.invalidate()
}

fun LineChart.update(dataSet: ArrayList<ILineDataSet>) {
    this.data = LineData(dataSet)
    this.fitScreen()
    this.invalidate()
}

fun PieChart.update(context: Context, result: List<Serving>) {
    val total = result.fold(0) { subtotal, item -> subtotal + item.serving }

    val percent = (total.toFloat()/ FOOD_RATIONS)*100f
    val percentPie = minOf(percent, 100f)

    val entries = ArrayList<PieEntry>()
    entries.add(PieEntry(percentPie, "Total"))
    entries.add(PieEntry(100f - percentPie, "Empty"))

    val dataSet = PieDataSet(entries, "").style(context)

    this.centerText = generatePieChartText(context, total, percent)
    this.update(dataSet)
}

/*
 * Perdón, esta función es horrible. Pero funciona.
 */
private fun generatePieChartText(context: Context, total: Int, percent: Float) : SpannableString {
    val pText = "%.2f%%".format(percent)
    val tText = context.getString(R.string.counter_rations_title).format(total, FOOD_RATIONS)
    val s = SpannableString("$pText\n$tText")

    s.setSpan(RelativeSizeSpan(3f), 0, pText.length, 0)
    s.setSpan(ForegroundColorSpan(context.colorOf(
            if (total > FOOD_RATIONS - FOOD_LIMIT) R.color.accent else R.color.primary_dark)),
            0, pText.length, 0)

    s.setSpan(StyleSpan(Typeface.NORMAL), pText.length, s.length, 0)
    s.setSpan(ForegroundColorSpan(Color.BLACK), pText.length, s.length, 0)

    return s
}

private fun PieChart.update(dataSet: PieDataSet) {
    this.animateY(800, Easing.EasingOption.EaseInOutCubic)
    this.data = PieData(dataSet)
    this.invalidate()
}

fun LineDataSet.style(context: Context, style: ChartStyle): LineDataSet {
    when (style) {
        ChartStyle.POINTS -> {
            this.setDrawValues(false)
            this.setDrawCircleHole(true)
            this.setCircleColor(context.colorOf(R.color.primary_dark))
            this.setCircleColorHole(context.colorOf(R.color.primary_dark))
            this.circleRadius = 4f
            this.color = Color.TRANSPARENT
        }
        ChartStyle.ESTIMATE -> {
            this.setDrawValues(false)
            this.setDrawCircles(false)
            this.color = context.colorOf(R.color.accent)
            this.lineWidth = 2f
            this.enableDashedLine(20f, 10f, 0f)
        }
        ChartStyle.RATIONS -> {
            this.setDrawValues(false)
            this.color = context.colorOf(R.color.accent)
            this.setCircleColor(context.colorOf(R.color.accent))
            this.lineWidth = 2f
            this.circleRadius = 3f
        }
        ChartStyle.CUMULATIVE -> {
            this.setDrawValues(false)
            this.setDrawCircles(false)
            this.color = context.colorOf(R.color.primary_dark)
            this.lineWidth = 1f

            this.setDrawFilled(true)
            this.fillAlpha = 255
            this.fillColor = context.colorOf(R.color.primary)
        }
    }
    return this
}

fun PieDataSet.style(context: Context): PieDataSet {
    val colors = ArrayList<Int>()
    colors.add(context.colorOf(R.color.accent))
    colors.add(Color.TRANSPARENT)
    this.colors = colors
    this.setDrawValues(false)
    return this
}

private class HourAxisValueFormatter : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        val valueDate = Date(value.toLong() * 1000)
        return valueDate.toFormat("HH:mm")
    }
}

private const val FOOD_RATIONS = 1500
private const val FOOD_LIMIT = 200