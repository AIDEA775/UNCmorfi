package com.uncmorfi.helpers

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
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

fun PieChart.update(dataSet: PieDataSet) {
    this.animateY(800, Easing.EasingOption.EaseInOutCubic)
    this.data = PieData(dataSet)
    this.invalidate()
}

fun LineDataSet.style(style: ChartStyle, context: Context): LineDataSet {
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
        return valueDate.toString("HH:mm")
    }
}