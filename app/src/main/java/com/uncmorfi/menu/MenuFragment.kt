package com.uncmorfi.menu

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.uncmorfi.R
import com.uncmorfi.helpers.*
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.models.DayMenu
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_menu.*

/**
 * Menú de la semana.
 * Administra la UI.
 */
class MenuFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mMenuAdapter: MenuAdapter
    private lateinit var mViewModel: MainViewModel

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

        menuSwipeRefresh.init { refreshMenu() }
        initRecyclerAndAdapter()
        initMenu()

        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mViewModel.getMenu().observe(this, Observer {
            mMenuAdapter.updateMenu(it)
        })

        mViewModel.menuStatus.observe(this, Observer {
            menuSwipeRefresh.isRefreshing = false
            when (it) {
                BUSY -> {}
                UPDATED -> mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                EMPTY_ERROR -> mRootView.snack(context, R.string.update_fail, SnackType.ERROR)
                OK -> mRootView.snack(context, R.string.update_nothing, SnackType.FINISH)
                else -> mRootView.snack(context, it)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_update -> { refreshMenu(); true }
            R.id.menu_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initRecyclerAndAdapter() {
        menuRecyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        menuRecyclerView.layoutManager = layoutManager
    }

    private fun initMenu() {
        mMenuAdapter = MenuAdapter(requireContext(),
                { onClick(it) },
                { onLongClick(it) })
        menuRecyclerView.adapter = mMenuAdapter
    }

    private fun refreshMenu() {
        if (context.isOnline()) {
            menuSwipeRefresh.isRefreshing = true
            mViewModel.updateMenu()
        } else {
            menuSwipeRefresh.isRefreshing = false
            mRootView.snack(context, R.string.no_connection, SnackType.ERROR)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_menu)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.menuStatus.value = BUSY
    }


    private fun onClick(dayMenu: DayMenu) {
        requireActivity().shareText(
                getString(R.string.menu_share_subject),
                dayMenu.toString() + "\n\n" + getString(R.string.menu_share_banner),
                getString(R.string.menu_share_subject))
    }

    private fun onLongClick(dayMenu: DayMenu) {
        context?.copyToClipboard("food", dayMenu.toString())
        mRootView.snack(context, R.string.menu_copy_msg, SnackType.FINISH)
    }

    companion object {
        private const val URL = "https://www.unc.edu.ar/vida-estudiantil/men%C3%BA-de-la-semana"
    }

}