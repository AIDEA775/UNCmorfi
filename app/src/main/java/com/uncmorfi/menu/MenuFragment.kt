package com.uncmorfi.menu

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.shared.*
import com.uncmorfi.MainViewModel
import kotlinx.android.synthetic.main.fragment_menu.*

/**
 * Menú de la semana.
 * Administra la UI.
 */
class MenuFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mMenuAdapter: MenuAdapter
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        swipeRefresh.init { viewModel.updateMenu() }
        initRecyclerAndAdapter()
        initMenu()

        observe(viewModel.getMenu()) {
            mMenuAdapter.updateMenu(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_update -> { viewModel.updateMenu(); true }
            R.id.menu_clear -> { viewModel.clearMenu(); true }
            R.id.menu_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerAndAdapter() {
        menuRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        menuRecyclerView.layoutManager = layoutManager
        menuRecyclerView.isNestedScrollingEnabled = false
    }

    private fun initMenu() {
        mMenuAdapter = MenuAdapter({ onClick(it) }
        ) { onLongClick(it) }
        menuRecyclerView.adapter = mMenuAdapter
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_menu)
    }

    private fun onClick(dayMenu: DayMenu) {
        requireActivity().shareText(
                getString(R.string.menu_share_subject),
                dayMenu.toString() + "\n\n" + getString(R.string.menu_share_banner),
                getString(R.string.menu_share_subject))
    }

    private fun onLongClick(dayMenu: DayMenu) {
        context?.copyToClipboard("food", dayMenu.toString())
        mRootView.snack(R.string.snack_copied, SnackType.FINISH)
    }

    companion object {
        private const val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"
    }

}