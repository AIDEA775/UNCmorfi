package com.uncmorfi.counter

import android.graphics.Matrix
import android.view.MotionEvent

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import android.view.View

class SyncChartsGestureListener(private val source: LineChart,
                                private vararg val dest: LineChart) : OnChartGestureListener {

    override fun onChartGestureStart(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {}

    override fun onChartGestureEnd(me: MotionEvent, lastPerformedGesture: ChartTouchListener.ChartGesture) {}

    override fun onChartLongPressed(me: MotionEvent) {}

    override fun onChartDoubleTapped(me: MotionEvent) {}

    override fun onChartSingleTapped(me: MotionEvent) {}

    override fun onChartFling(me1: MotionEvent, me2: MotionEvent, velocityX: Float, velocityY: Float) {}

    override fun onChartScale(me: MotionEvent, scaleX: Float, scaleY: Float) {
        sync()
    }

    override fun onChartTranslate(me: MotionEvent, dX: Float, dY: Float) {
        sync()

    }

    private fun sync() {
        val srcVals = FloatArray(9)
        var dstMatrix: Matrix
        val dstVals = FloatArray(9)

        val srcMatrix = source.viewPortHandler.matrixTouch
        srcMatrix.getValues(srcVals)

        for (chart in dest) {
            if (chart.visibility == View.VISIBLE) {
                dstMatrix = chart.viewPortHandler.matrixTouch
                dstMatrix.getValues(dstVals)
                dstVals[Matrix.MSCALE_X] = srcVals[Matrix.MSCALE_X]
                dstVals[Matrix.MTRANS_X] = srcVals[Matrix.MTRANS_X]
                dstMatrix.setValues(dstVals)
                chart.viewPortHandler.refresh(dstMatrix, chart, true)
            }
        }
    }
}
