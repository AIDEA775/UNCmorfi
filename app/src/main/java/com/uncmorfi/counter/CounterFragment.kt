package com.uncmorfi.counter

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.SeekBar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.uncmorfi.R
import com.uncmorfi.helpers.*
import kotlinx.android.synthetic.main.fragment_counter.*
import java.util.*

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 * Usa a [RefreshCounterTask] para actualizar el medidor.
 */

class CounterFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    private lateinit var mRootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_counter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        counterSwipeRefresh.init { refreshCounter() }

        setChartsOptionsBase(counterTimeChart)
        setChartsOptionsBase(counterAccumulatedChart)

        counterTimeChart.onChartGestureListener = SyncChartsGestureListener(
                counterTimeChart, counterAccumulatedChart)

        counterAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
                counterAccumulatedChart, counterTimeChart)

        setTimeChart()
        setCumulativeChart()

        counterBar.max = FOOD_RATIONS
        counterSeek.setOnSeekBarChangeListener(this)
        counterSeek.progress = 0

        refreshCounter()
    }

    private fun setChartsOptionsBase(chart: LineChart) {
        chart.setNoDataText(getString(R.string.counter_chart_empty))
        chart.isScaleYEnabled = false
        chart.description = null

        chart.setDrawGridBackground(false)
        chart.setDrawBorders(false)

        chart.isHighlightPerTapEnabled = false
        chart.isHighlightPerDragEnabled = false

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisRight.isEnabled = false
        chart.axisRight.setDrawGridLines(false)

        chart.xAxis.setDrawAxisLine(false)

        chart.legend.textSize = 14f
        chart.legend.textColor = requireContext().colorOf(R.color.primary_text)

        val xAxis = chart.xAxis
        xAxis.valueFormatter = HourAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }

    private fun setTimeChart() {
        // Por ahora nada, pero en caso de que se agreguen más graficos, dejar las opciones comunes
        // en setChartsOptionsBase, y las específicas en estas funciones.
    }

    private fun setCumulativeChart() {
        counterAccumulatedChart.xAxis.setDrawGridLines(false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_counter)
    }

    override fun onStop() {
        super.onStop()
        counterSwipeRefresh.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.counter, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.counter_update -> { refreshCounter(); true }
            R.id.counter_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshCounter() {
        if (requireContext().isOnline()) {
            refreshStatus(true)
            RefreshCounterTask { code, result -> onCounterDownloaded(code, result) }
                    .execute()
        } else {
            refreshStatus(false)
            mRootView.snack(context, R.string.no_connection, SnackType.ERROR)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val windows = progress + 5 // Mínimo 5 ventanas

        val minutes = getEstimateFromPosition(windows)
        val currentTime = Date().time

        val timeStamp = Date(currentTime + minutes * 60 * 1000)
        val text = timeStamp.toString("HH:mm")

        counterDistance.text = String.format(getString(R.string.counter_distance), windows)
        counterEstimate.text = String.format(getString(R.string.counter_estimate), minutes, text)
    }

    /**
     * Estimar el tiempo de espera.
     * @param x Distancia (cantidad de ventanas)
     * @return Minutos estimados
     */
    private fun getEstimateFromPosition(x: Int): Long {
        return (x * (x * 0.062307 + 1.296347) - 2.190814).toLong()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    private fun onCounterDownloaded(code: ReturnCode, result: List<Entry>) {
        if (activity != null && isAdded) {
            refreshStatus(false)
            when (code) {
                ReturnCode.OK -> {
                    updateTextViews(result)
                    updateCharts(result)
                    mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                }
                else -> mRootView.snack(context, code)
            }
        }
    }

    private fun updateTextViews(result: List<Entry>) {
        var total = 0
        for (entry in result)
            total += entry.y.toInt()

        counterPercent.setTextColor(requireContext().colorOf(
                if (total > FOOD_RATIONS - FOOD_LIMIT) R.color.accent else R.color.primary_dark))

        counterBar.progress = total
        counterResume.text = String.format(
                getString(R.string.counter_rations_title), total, FOOD_RATIONS)
        counterPercent.text = String.format(
                Locale.US, "%d%%", total * 100 / FOOD_RATIONS)
    }

    private fun updateCharts(data: List<Entry>?) {
        if (data != null && !data.isEmpty()) {
            val accumulatedData = getAccumulate(data)

            updateChart(counterTimeChart, getTimeDataSet(data))
            updateChart(counterAccumulatedChart, getAccumulatedDataSet(accumulatedData))
        }
    }

    private fun getAccumulate(data: List<Entry>): List<Entry> {
        var accumulated = 0
        val accumulatedData = ArrayList<Entry>()
        for (entry in data) {
            accumulated += entry.y.toInt()
            accumulatedData.add(Entry(entry.x, accumulated.toFloat()))
        }
        return accumulatedData
    }

    private fun getTimeDataSet(data: List<Entry>): LineDataSet {
        val dataSet = LineDataSet(data, getString(R.string.counter_chart_label_time))
        dataSet.setColors(intArrayOf(R.color.accent), context)
        dataSet.setCircleColors(intArrayOf(R.color.accent), context)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 3f
        dataSet.setDrawValues(false)
        return dataSet
    }

    private fun getAccumulatedDataSet(data: List<Entry>): LineDataSet {
        val dataSet = LineDataSet(data, getString(R.string.counter_chart_label_accumulated))
        dataSet.setColors(intArrayOf(R.color.primary_dark), context)
        dataSet.lineWidth = 1f
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)

        dataSet.setDrawFilled(true)
        dataSet.fillAlpha = 255
        dataSet.fillColor = requireContext().colorOf(R.color.primary)

        return dataSet
    }

    private fun updateChart(chart: LineChart, dataSet: LineDataSet) {
        chart.data = LineData(dataSet)
        chart.setVisibleXRangeMinimum(200f)
        chart.invalidate()
    }

    private fun refreshStatus(show : Boolean) {
        counterBar.isIndeterminate = show
        counterSwipeRefresh.isRefreshing = show
    }

    private inner class HourAxisValueFormatter : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val valueDate = Date(value.toLong() * 1000)
            return valueDate.toString("HH:mm")
        }
    }

    companion object {
        private const val FOOD_RATIONS = 1500
        private const val FOOD_LIMIT = 200
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
    }
}