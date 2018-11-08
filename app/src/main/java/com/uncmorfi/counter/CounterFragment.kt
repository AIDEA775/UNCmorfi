package com.uncmorfi.counter

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.*
import android.widget.SeekBar
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
import com.uncmorfi.helpers.*
import kotlinx.android.synthetic.main.fragment_counter.*
import org.apache.commons.math3.stat.regression.SimpleRegression
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 * Usa a [RefreshCounterTask] para actualizar el medidor.
 */

class CounterFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    private lateinit var mRootView: View
    private val mEstimateList = ArrayList<Entry>()
    private var mEstimateFirst: Double = 0.0
    private val mSimpleRegression = SimpleRegression(true)

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
        counterPieChart.init(requireContext())
        counterEstimateChart.init(requireContext())
        counterTimeChart.init(requireContext())
        counterAccumulatedChart.init(requireContext())

        counterTimeChart.onChartGestureListener = SyncChartsGestureListener(
                counterTimeChart, counterAccumulatedChart)

        counterAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
                counterAccumulatedChart, counterTimeChart)

        counterEstimateChart.legend.isEnabled = false
        counterAccumulatedChart.xAxis.setDrawGridLines(false)

        counterSeek.setOnSeekBarChangeListener(this)
        counterSeek.progress = 99

        counterEstimateButton.setOnClickListener { updateEstimate() }

        refreshCounter()
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
        if (fromUser) {
            val windows = progress + 3 // Mínimo 3 ventanas
            counterDistance.text = getString(R.string.counter_distance).format(windows)
        }
    }

    /**
     * Estimar el tiempo de espera.
     * @param x Distancia (cantidad de ventanas)
     * @return Minutos estimados
     */
    private fun getEstimateFromPosition(x: Int): Double {
        return (x * (x * 0.062307 + 1.296347) - 2.190814)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    private fun onCounterDownloaded(code: ReturnCode, result: List<Entry>) {
        if (activity != null && isAdded) {
            refreshStatus(false)
            when (code) {
                ReturnCode.OK -> {
                    updatePieChart(result)
                    updateCharts(result)
                    mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                }
                else -> mRootView.snack(context, code)
            }
        }
    }

    private fun updatePieChart(result: List<Entry>) {
        var total = 0
        for (entry in result)
            total += entry.y.toInt()

        val percent = (total.toFloat()/FOOD_RATIONS)*100f
        val percentPie = minOf(percent, 100f)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentPie, "Total"))
        entries.add(PieEntry(100f - percentPie, "Empty"))

        val dataSet = PieDataSet(entries, "").style(requireContext())

        counterPieChart.centerText = generatePieChartText(total, percent)
        counterPieChart.update(dataSet)
    }

    /*
     * Perdón, esta función es horrible. Pero funciona.
     */
    private fun generatePieChartText(total: Int, percent: Float) : SpannableString {
        val pText = "%.2f%%".format(percent)
        val tText = getString(R.string.counter_rations_title).format(total, FOOD_RATIONS)
        val s = SpannableString("$pText\n$tText")

        s.setSpan(RelativeSizeSpan(3f), 0, pText.length, 0)
        s.setSpan(ForegroundColorSpan(requireContext().colorOf(
                if (total > FOOD_RATIONS - FOOD_LIMIT) R.color.accent else R.color.primary_dark)),
                0, pText.length, 0)

        s.setSpan(StyleSpan(Typeface.NORMAL), pText.length, s.length, 0)
        s.setSpan(ForegroundColorSpan(Color.BLACK), pText.length, s.length, 0)

        return s
    }

    /*
     * Perdón, esta función tambien horrible de entender.
     */
    private fun updateEstimate() {
        val windows = counterSeek.progress + 3 // Mínimo 3 ventanas
        val time = Date().clearDate()
        val dataSets = ArrayList<ILineDataSet>()

        mEstimateList.add(Entry(time.toFloat(), windows.toFloat()))

        mSimpleRegression.addData(time.toDouble(), windows.toDouble())
        var root = (-mSimpleRegression.intercept / mSimpleRegression.slope)
        val estimateLine = ArrayList<Entry>()

        if (root.isNaN()) {
            // Primera estimación
            mEstimateFirst = time.toDouble()
            root = mEstimateFirst + getEstimateFromPosition(windows) * 60
            estimateLine.add(Entry(mEstimateFirst.toFloat(), windows.toFloat()))
            estimateLine.add(Entry(root.toFloat(), 0f))
        } else {
            // Estimación con 2 o más datos
            estimateLine.add(Entry(mEstimateFirst.toFloat(),
                    mSimpleRegression.predict(mEstimateFirst).toFloat()))
            estimateLine.add(Entry(root.toFloat(), 0f))
        }

        val lineSet = LineDataSet(estimateLine, "")
                .style(ChartStyle.ESTIMATE, requireContext())
        val pointSet = LineDataSet(mEstimateList, "")
                .style(ChartStyle.POINTS, requireContext())

        dataSets.add(pointSet)
        dataSets.add(lineSet)

        counterEstimateChart.visibility = View.VISIBLE
        counterEstimateChart.update(dataSets)

        counterEstimateText.visibility = View.VISIBLE

        val timeStamp = Date(root.toLong() * 1000)
        val text = timeStamp.toString("HH:mm")

        val minutes = (root - time) / 60
        counterEstimateText.text = getString(R.string.counter_estimate)
                .format(minutes.roundToInt(), text)
    }

    private fun updateCharts(data: List<Entry>?) {
        if (data != null && !data.isEmpty()) {
            // Algunas veces el medidor se cae, y las raciones aparecen cargadas a las 00:00hs
            // así que al primer elemento lo ponemos 1 min antes del segundo elemento
            // para que no haya un espacio vacío en el medio.
            data[0].x = data[1].x - 60f

            val timeData = LineDataSet(data, getString(R.string.counter_chart_label_time))
                    .style(ChartStyle.RATIONS, requireContext())

            val cumulativeData = LineDataSet(accumulate(data), getString(R.string.counter_chart_label_accumulated))
                    .style(ChartStyle.CUMULATIVE, requireContext())

            counterChartsCard.visibility = View.VISIBLE
            counterTimeChart.update(timeData)
            counterAccumulatedChart.update(cumulativeData)
        }
    }

    private fun accumulate(data: List<Entry>): List<Entry> {
        var sum = 0
        val accumulated = ArrayList<Entry>()
        for (entry in data) {
            sum += entry.y.toInt()
            accumulated.add(Entry(entry.x, sum.toFloat()))
        }
        return accumulated
    }

    private fun refreshStatus(show : Boolean) {
        counterSwipeRefresh.isRefreshing = show
    }

    companion object {
        private const val FOOD_RATIONS = 1500
        private const val FOOD_LIMIT = 200
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
    }
}