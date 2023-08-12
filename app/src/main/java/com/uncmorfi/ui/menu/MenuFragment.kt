package com.uncmorfi.ui.menu

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.shared.*
import kotlinx.android.synthetic.main.fragment_menu.*

/**
 * Menú de la semana.
 * Administra la UI.
 */
class MenuFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var adapter: MenuAdapter
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        swipeRefresh.init { viewModel.forceRefreshMenu() }
        initRecyclerAndAdapter()
        initMenu()
        initInformationMessage()

        observe(viewModel.getMenu()) { menu ->
            adapter.updateMenu(menu)
            val today = menu.indexOfFirst { it.isToday() }
            menuRecyclerView.scrollToPosition(today)
        }

        observe(viewModel.status) {
            swipeRefresh.isRefreshing = it == StatusCode.UPDATING
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_update -> {
                viewModel.forceRefreshMenu(); true
            }
            R.id.menu_clear -> {
                viewModel.clearMenu(); true
            }
            R.id.menu_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerAndAdapter() {
        menuRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        menuRecyclerView.layoutManager = layoutManager
        menuRecyclerView.isNestedScrollingEnabled = false
    }

    private fun initMenu() {
        adapter = MenuAdapter(::onClick, ::onLongClick)
        menuRecyclerView.adapter = adapter
    }

    private fun initInformationMessage() {
        informationCard.apply {
            setMessage(getString(R.string.menu_information))
            setButtonText(getString(R.string.go))
            setOnClickListener {
                requireActivity().openInstagram(SEC_BIMO_INSTAGRAM)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_menu)
        viewModel.refreshMenu()
    }

    private fun onClick(dayMenu: DayMenu) {
        requireActivity().shareText(
            getString(R.string.menu_share_subject),
            dayMenu.toString() + "\n\n" + getString(R.string.menu_share_banner),
            getString(R.string.menu_share_subject)
        )
    }

    private fun onLongClick(dayMenu: DayMenu) {
        context?.copyToClipboard("food", dayMenu.toString())
        mRootView.snack(R.string.snack_copied, SnackType.FINISH)
    }

    companion object {
        private const val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"
    }

}