package com.uncmorfi.ui.menu

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.databinding.FragmentMenuBinding
import com.uncmorfi.shared.*

/**
 * Menú de la semana.
 * Administra la UI.
 */
class MenuFragment : Fragment(R.layout.fragment_menu) {
    private lateinit var adapter: MenuAdapter
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding : FragmentMenuBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentMenuBinding.bind(view)
        binding.setUi()

        observe(viewModel.state) { state ->
            binding.swipeRefresh.isRefreshing = state == StatusCode.UPDATING
        }

        observe(viewModel.menu) { menu ->
            adapter.updateMenu(menu)
            val today = menu.indexOfFirst { it.isToday() }
            binding.menuRecyclerView.scrollToPosition(today)
        }
    }

    private fun FragmentMenuBinding.setUi(){
        addMenu(R.menu.menu){menuItemId ->
            when(menuItemId){
                R.id.menu_update -> {
                    viewModel.forceRefreshMenu(); true
                }
                R.id.menu_clear -> {
                    viewModel.clearMenu(); true
                }
                R.id.menu_browser -> requireActivity().startBrowser(URL)
                else -> false
            }
        }
        swipeRefresh.init { viewModel.forceRefreshMenu() }
        initRecyclerAndAdapter()
        initMenu()
    }

    private fun initRecyclerAndAdapter() {
        binding.menuRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        binding.menuRecyclerView.layoutManager = layoutManager
        binding.menuRecyclerView.isNestedScrollingEnabled = false
    }

    private fun initMenu() {
        adapter = MenuAdapter(::onClick, ::onLongClick)
        binding.menuRecyclerView.adapter = adapter
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
        binding.root.snack(R.string.snack_copied, SnackType.FINISH)
    }

    companion object {
        private const val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"
    }

}