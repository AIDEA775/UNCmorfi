package com.uncmorfi.servings

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
import com.uncmorfi.helpers.*
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.models.Serving
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_counter.*
import org.apache.commons.math3.stat.regression.SimpleRegression
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 */

class ServingsFragment : Fragment(), SeekBar.OnSeekBarChangeListener {
    private lateinit var mRootView: View
    private lateinit var mViewModel: MainViewModel
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
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        counterSwipeRefresh.init { refreshCounter() }
        counterPieChart.init(requireContext())
        counterEstimateChart.init(requireContext())
        counterTimeChart.init(requireContext())
        counterAccumulatedChart.init(requireContext())

        counterTimeChart.onChartGestureListener = SyncChartsGestureListener(
                counterTimeChart, counterAccumulatedChart)

        counterAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
                counterAccumulatedChart, counterTimeChart)

        // Parametros especiales de ambos LineChart
        counterEstimateChart.legend.isEnabled = false
        counterAccumulatedChart.xAxis.setDrawGridLines(false)

        counterSeek.setOnSeekBarChangeListener(this)
        counterSeek.progress = 99

        counterEstimateButton.setOnClickListener { updateEstimate() }

        mViewModel.getServings().observe(this, Observer {
            counterPieChart.update(requireContext(), it)
            updateCharts(it)
        })

        mViewModel.servingStatus.observe(this, Observer {
            if (it == BUSY) return@Observer
            counterSwipeRefresh.isRefreshing = false
            when (it) {
                UPDATED -> mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                UPDATING -> mRootView.snack(context, R.string.updating, SnackType.FINISH)
                DELETED -> TODO()
                EMPTY_ERROR -> mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                else -> mRootView.snack(context, it)
            }
        })

        refreshCounter()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_counter)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.servingStatus.value = BUSY
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
            counterSwipeRefresh.isRefreshing = true
            mViewModel.updateServings()
        } else {
            counterSwipeRefresh.isRefreshing = false
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
                .style(requireContext(), ChartStyle.ESTIMATE)
        val pointSet = LineDataSet(mEstimateList, "")
                .style(requireContext(), ChartStyle.POINTS)

        dataSets.add(pointSet)
        dataSets.add(lineSet)

        counterEstimateChart.visibility = View.VISIBLE
        counterEstimateChart.update(dataSets)

        counterEstimateText.visibility = View.VISIBLE

        val timeStamp = Date(root.toLong() * 1000)
        val text = timeStamp.toFormat("HH:mm")

        val minutes = (root - time) / 60
        counterEstimateText.text = getString(R.string.counter_estimate)
                .format(minutes.roundToInt(), text)
    }

    private fun updateCharts(items: List<Serving>) {
        if (items.isNotEmpty()) {
            val data = items.map { s -> Entry(s.date.clearDate().toFloat(), s.serving.toFloat()) }
            if (data.size > 1) {
                // Algunas veces el medidor se cae, y las raciones aparecen cargadas a las 00:00hs
                // así que al primer elemento lo ponemos 1 min antes del segundo elemento
                // para que no haya un espacio vacío en el medio.
                data[0].x = data[1].x - 60f
            }

            val timeData = LineDataSet(data, getString(R.string.counter_chart_label_time))
                    .style(requireContext(), ChartStyle.RATIONS)

            val cumulativeData = LineDataSet(accumulate(data), getString(R.string.counter_chart_label_accumulated))
                    .style(requireContext(), ChartStyle.CUMULATIVE)

            counterChartsCard.visibility = View.VISIBLE
            counterTimeChart.update(timeData)
            counterAccumulatedChart.update(cumulativeData)
        }
    }

    private fun accumulate(data: List<Entry>): List<Entry> {
        var sum = 0f
        val accumulated = ArrayList<Entry>()
        for (entry in data) {
            sum += entry.y
            accumulated.add(Entry(entry.x, sum))
        }
        return accumulated
    }

    companion object {
        private const val FOOD_RATIONS = 1500
        private const val FOOD_LIMIT = 200
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
    }
}