package com.uncmorfi.servings

import android.os.Bundle
import android.view.*
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.uncmorfi.R
import com.uncmorfi.helpers.*
import com.uncmorfi.helpers.StatusCode.BUSY
import com.uncmorfi.models.Serving
import com.uncmorfi.servings.StyledLineDataSet.Companion.ChartStyle.*
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_servings.*
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
        return inflater.inflate(R.layout.fragment_servings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        servingSwipeRefresh.init { refreshServings() }
        servingEstimateChart.init(requireContext())
        servingTimeChart.init(requireContext())
        servingAccumulatedChart.init(requireContext())

        servingTimeChart.onChartGestureListener = SyncChartsGestureListener(
                servingTimeChart, servingAccumulatedChart)

        servingAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
                servingAccumulatedChart, servingTimeChart)

        // Parametros especiales de ambos LineChart
        servingEstimateChart.legend.isEnabled = false
        servingAccumulatedChart.xAxis.setDrawGridLines(false)

        servingSeek.setOnSeekBarChangeListener(this)
        servingSeek.progress = 99

        servingEstimateButton.setOnClickListener { updateEstimate() }

        mViewModel.getServings().observe(this, Observer {
            servingsPieChart.set(it)
            updateCharts(it)
        })

        mViewModel.servingStatus.observe(this, Observer {
            when (it) {
                BUSY -> {}
                else -> {
                    servingSwipeRefresh.isRefreshing = false
                    mRootView.snack(it)
                }
            }
        })

        refreshServings()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_servings)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.servingStatus.value = BUSY
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.servings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.serving_update -> { refreshServings(); true }
            R.id.serving_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun refreshServings() {
        servingSwipeRefresh.isRefreshing = true
        mViewModel.updateServings()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val windows = progress + 3 // Mínimo 3 ventanas
            servingDistance.text = getString(R.string.servings_distance).format(windows)
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
        val windows = servingSeek.progress + 3 // Mínimo 3 ventanas
        val time = Calendar.getInstance().clearDate()
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

        val lineSet = StyledLineDataSet(requireContext(), estimateLine, "", ESTIMATE)
        val pointSet = StyledLineDataSet(requireContext(), mEstimateList, "", POINTS)

        dataSets.add(pointSet)
        dataSets.add(lineSet)

        servingEstimateChart.visibility = View.VISIBLE
        servingEstimateChart.update(dataSets)

        servingEstimateText.visibility = View.VISIBLE

        val timeStamp = Calendar.getInstance()
        timeStamp.timeInMillis = root.toLong() * 1000
        val text = timeStamp.toFormat("HH:mm")

        val minutes = (root - time) / 60
        servingEstimateText.text = getString(R.string.servings_estimate)
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

            val timeData = StyledLineDataSet(requireContext(), data, getString(R.string.servings_chart_label_time), RATIONS)
            val cumulativeData = StyledLineDataSet(requireContext(), accumulate(data), getString(R.string.servings_chart_label_accumulated), CUMULATIVE)

            servingChartsCard.visibility = View.VISIBLE
            servingTimeChart.update(timeData)
            servingAccumulatedChart.update(cumulativeData)
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
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
    }
}