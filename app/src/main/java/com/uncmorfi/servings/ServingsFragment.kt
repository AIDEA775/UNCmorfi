package com.uncmorfi.servings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.data.Entry
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.servings.StyledLineDataSet.Companion.ChartStyle.CUMULATIVE
import com.uncmorfi.servings.StyledLineDataSet.Companion.ChartStyle.RATIONS
import com.uncmorfi.shared.*
import kotlinx.android.synthetic.main.fragment_servings.*
import java.time.ZoneId

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 */

class ServingsFragment : Fragment() {
    private lateinit var mRootView: View
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, i: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_servings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        swipeRefresh.init { viewModel.updateServings() }
        servingTimeChart.init(requireContext())
        servingAccumulatedChart.init(requireContext())

        servingTimeChart.onChartGestureListener = SyncChartsGestureListener(
            servingTimeChart, servingAccumulatedChart
        )

        servingAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
            servingAccumulatedChart, servingTimeChart
        )

        // Parametros especiales de ambos LineChart
        servingAccumulatedChart.xAxis.setDrawGridLines(false)

        observe(viewModel.getServings()) {
            servingsPieChart.set(it)
            updateCharts(it)
        }

        observe(viewModel.status) {
            swipeRefresh.isRefreshing = it == StatusCode.UPDATING
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_servings)
        viewModel.updateServings()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.servings, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.serving_update -> {
                viewModel.updateServings(); true
            }
            R.id.serving_browser -> requireActivity().startBrowser(COCINA_URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateCharts(items: List<Serving>) {
        if (items.isNotEmpty()) {
            val data = items.map { s -> Entry(s.toFloat(), s.serving.toFloat()) }
            if (data.size > 1) {
                // Algunas veces el medidor se cae, y las raciones aparecen cargadas a las 00:00hs
                // así que al primer elemento lo ponemos 1 min antes del segundo elemento
                // para que no haya un espacio vacío en el medio.
                data[0].x = data[1].x - 60f
            }

            val timeData = StyledLineDataSet(
                requireContext(),
                data,
                getString(R.string.servings_chart_label_time),
                RATIONS
            )
            val cumulativeData = StyledLineDataSet(
                requireContext(),
                accumulate(data),
                getString(R.string.servings_chart_label_accumulated),
                CUMULATIVE
            )

            servingTimeChart.update(timeData)
            servingAccumulatedChart.update(cumulativeData)
        } else {
            servingTimeChart.clear()
            servingAccumulatedChart.clear()
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

}