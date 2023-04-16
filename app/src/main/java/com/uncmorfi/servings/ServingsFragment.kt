package com.uncmorfi.servings

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.data.Entry
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.servings.StyledLineDataSet.Companion.ChartStyle.CUMULATIVE
import com.uncmorfi.servings.StyledLineDataSet.Companion.ChartStyle.RATIONS
import com.uncmorfi.shared.clearDate
import com.uncmorfi.shared.init
import com.uncmorfi.shared.startBrowser
import com.uncmorfi.MainViewModel
import kotlinx.android.synthetic.main.fragment_servings.*

/**
 * Medidor de raciones.
 * Administra la UI con todas sus features.
 */

class ServingsFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mViewModel: MainViewModel

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

        swipeRefresh.init { refreshServings() }
        servingTimeChart.init(requireContext())
        servingAccumulatedChart.init(requireContext())

        servingTimeChart.onChartGestureListener = SyncChartsGestureListener(
                servingTimeChart, servingAccumulatedChart)

        servingAccumulatedChart.onChartGestureListener = SyncChartsGestureListener(
                servingAccumulatedChart, servingTimeChart)

        // Parametros especiales de ambos LineChart
        servingAccumulatedChart.xAxis.setDrawGridLines(false)

        mViewModel.getServings().observe(viewLifecycleOwner, Observer {
            servingsPieChart.set(it)
            updateCharts(it)
        })

        refreshServings()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_servings)
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
        mViewModel.updateServings()
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

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/cocina.php"
    }
}