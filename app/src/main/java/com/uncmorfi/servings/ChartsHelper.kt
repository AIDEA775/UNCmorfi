package com.uncmorfi.servings

import android.content.Context
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.helpers.toFormat
import java.util.*

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

private class HourAxisValueFormatter : IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        val valueDate = Calendar.getInstance()
        valueDate.timeInMillis = value.toLong() * 1000
        return valueDate.toFormat("HH:mm")
    }
}