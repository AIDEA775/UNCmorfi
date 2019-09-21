package com.uncmorfi.servings

import android.content.Context
import android.graphics.Color
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.uncmorfi.R
import com.uncmorfi.shared.colorOf

class StyledLineDataSet(context: Context, yVals: List<Entry>, label: String, style: ChartStyle):
        LineDataSet(yVals, label) {

    init {
        when (style) {
            ChartStyle.POINTS -> {
                setDrawValues(false)
                setDrawCircleHole(true)
                setCircleColor(context.colorOf(R.color.primary_dark))
                setCircleColorHole(context.colorOf(R.color.primary_dark))
                circleRadius = 4f
                color = Color.TRANSPARENT
            }
            ChartStyle.ESTIMATE -> {
                setDrawValues(false)
                setDrawCircles(false)
                color = context.colorOf(R.color.accent)
                lineWidth = 2f
                enableDashedLine(20f, 10f, 0f)
            }
            ChartStyle.RATIONS -> {
                setDrawValues(false)
                color = context.colorOf(R.color.accent)
                setCircleColor(context.colorOf(R.color.accent))
                lineWidth = 2f
                circleRadius = 3f
            }
            ChartStyle.CUMULATIVE -> {
                setDrawValues(false)
                setDrawCircles(false)
                color = context.colorOf(R.color.primary_dark)
                lineWidth = 1f

                setDrawFilled(true)
                fillAlpha = 255
                fillColor = context.colorOf(R.color.primary)
            }
        }
    }

    companion object {
        enum class ChartStyle {
            POINTS,
            ESTIMATE,
            RATIONS,
            CUMULATIVE
        }
    }
}