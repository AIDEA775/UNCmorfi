package com.uncmorfi.counter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.uncmorfi.R
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.SnackbarHelper.SnackType
import com.uncmorfi.helpers.SnackbarHelper.showSnack
import java.text.SimpleDateFormat
import java.util.*

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 * Usa a [RefreshCounterTask] para actualizar el medidor.
 */
class CounterFragment :
        Fragment(), RefreshCounterTask.RefreshCounterListener, SeekBar.OnSeekBarChangeListener {

    private lateinit var mRootView: View
    private lateinit var mResumeView: TextView
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mPercentView: TextView
    private lateinit var mDistanceView: TextView
    private lateinit var mSeekBar: SeekBar
    private lateinit var mEstimateView: TextView
    private lateinit var mTimeChart: LineChart
    private lateinit var mCumulativeChart: LineChart
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_counter, container, false)

        setAllViews(view)
        initSwipeRefreshLayout()

        setChartsOptionsBase(mTimeChart)
        setChartsOptionsBase(mCumulativeChart)
        setTimeChart()
        setCumulativeChart()

        mProgressBar.max = FOOD_RATIONS
        mSeekBar.setOnSeekBarChangeListener(this)
        mSeekBar.progress = 0

        refreshCounter()
        return view
    }

    private fun setAllViews(view: View) {
        mRootView = view.findViewById(R.id.counter_coordinator)
        mResumeView = view.findViewById(R.id.counter_resume)
        mProgressBar = view.findViewById(R.id.counter_bar)
        mPercentView = view.findViewById(R.id.counter_percent)
        mDistanceView = view.findViewById(R.id.counter_distance)
        mSeekBar = view.findViewById(R.id.counter_seek)
        mEstimateView = view.findViewById(R.id.counter_estimate)
        mTimeChart = view.findViewById(R.id.counter_time_chart)
        mCumulativeChart = view.findViewById(R.id.counter_accumulated_chart)
        mSwipeRefreshLayout = view.findViewById(R.id.counter_swipe_refresh)
    }

    private fun initSwipeRefreshLayout() {
        mSwipeRefreshLayout.setOnRefreshListener { refreshCounter() }

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.accent)
        mSwipeRefreshLayout.setColorSchemeResources(R.color.white, R.color.primary_light)
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
        chart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.primary_text)

        val xAxis = chart.xAxis
        xAxis.valueFormatter = HourAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }

    private fun setTimeChart() {
        // Por ahora nada, pero en caso de que se agreguen más graficos, dejar las opciones comunes
        // en setChartsOptionsBase, y las específicas en estas funciones.
    }

    private fun setCumulativeChart() {
        mCumulativeChart.xAxis.setDrawGridLines(false)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_counter)
    }

    override fun onStop() {
        super.onStop()
        mSwipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.counter, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.counter_update) {
            refreshCounter()
            return true
        } else if (item.itemId == R.id.counter_browser) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshCounter() {
        hideRefreshStatus()

        if (ConnectionHelper.isOnline(requireContext())) {
            showRefreshStatus()
            RefreshCounterTask(this).execute()
        } else {
            hideRefreshStatus()
            showSnack(requireContext(), mRootView, R.string.no_connection, SnackType.ERROR)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val windows = progress + 5 // Mínimo 5 ventanas

        val minutes = getEstimateFromPosition(windows)
        val currentTime = Date().time

        val timeStamp = Date(currentTime + minutes * 60 * 1000)
        val text = DATE_FMT.format(timeStamp)

        mDistanceView.text = String.format(getString(R.string.counter_distance), windows)
        mEstimateView.text = String.format(getString(R.string.counter_estimate), minutes, text)
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

    override fun onRefreshCounterSuccess(result: List<Entry>) {
        if (isAdded) {
            hideRefreshStatus()
            updateTextViews(result)
            updateCharts(result)

            showSnack(requireContext(), mRootView, R.string.update_success, SnackType.FINISH)
        }
    }

    override fun onRefreshCounterFail(errorCode: Int) {
        if (isAdded) {
            hideRefreshStatus()
            showError(errorCode)
        }
    }

    private fun updateTextViews(result: List<Entry>) {
        var total = 0
        for (entry in result)
            total += entry.y.toInt()

        mPercentView.setTextColor(ContextCompat.getColor(requireContext(),
                if (total > FOOD_RATIONS - FOOD_LIMIT) R.color.accent else R.color.primary_dark))

        mProgressBar.progress = total
        mResumeView.text = String.format(
                getString(R.string.counter_rations_title), total, FOOD_RATIONS)
        mPercentView.text = String.format(
                Locale.US, "%d%%", total * 100 / FOOD_RATIONS)
    }

    private fun updateCharts(data: List<Entry>?) {
        if (data != null && !data.isEmpty()) {
            val accumulatedData = getAccumulate(data)

            updateChart(mTimeChart, getTimeDataSet(data))
            updateChart(mCumulativeChart, getAccumulatedDataSet(accumulatedData))
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
        dataSet.fillColor = ContextCompat.getColor(requireContext(), R.color.primary)

        return dataSet
    }

    private fun updateChart(chart: LineChart, dataSet: LineDataSet) {
        chart.data = LineData(dataSet)
        chart.setVisibleXRangeMinimum(200f)
        chart.invalidate()
    }

    private fun showRefreshStatus() {
        mProgressBar.isIndeterminate = true
        mSwipeRefreshLayout.isRefreshing = true
    }

    private fun hideRefreshStatus() {
        mProgressBar.isIndeterminate = false
        mSwipeRefreshLayout.isRefreshing = false
    }

    private fun showError(code: Int) {
        when (code) {
            ConnectionHelper.CONNECTION_ERROR -> {
                showSnack(requireContext(), mRootView, R.string.connection_error, SnackType.ERROR)
            }
            ConnectionHelper.INTERNAL_ERROR -> {
                showSnack(requireContext(), mRootView, R.string.internal_error, SnackType.ERROR)
            }
        }
    }

    private inner class HourAxisValueFormatter : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            val valueDate = Date(value.toLong() * 1000)
            return DATE_FMT.format(valueDate)
        }
    }

    companion object {
        private const val FOOD_RATIONS = 1500
        private const val FOOD_LIMIT = 200
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
        private val DATE_FMT = SimpleDateFormat("HH:mm", Locale.getDefault())
    }
}