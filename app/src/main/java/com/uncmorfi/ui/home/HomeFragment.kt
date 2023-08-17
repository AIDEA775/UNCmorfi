package com.uncmorfi.ui.home

import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.uncmorfi.MainActivity
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.ui.balance.dialogs.UserOptionsDialog
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.databinding.FragmentHomeBinding
import com.uncmorfi.shared.StatusCode
import com.uncmorfi.shared.addMenu
import com.uncmorfi.shared.init
import com.uncmorfi.shared.observe

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var user: User
    private var mDayMenu: DayMenu? = null
    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHomeBinding.bind(view)
        binding.setUi()

        observe(viewModel.state) { state ->
            binding.swipeRefresh.isRefreshing = state == StatusCode.UPDATING
        }

        /*
        * Menu
        */
        observe(viewModel.menu){ menu ->
            val today = menu.firstOrNull { it.isToday() }
            mDayMenu = today

            today?.let {
                binding.homeMenu.setDayMenu(today)
                binding.homeMenu.visibility = VISIBLE
            }
        }

        /*
        * Tarjetas
        */
        observe(viewModel.users){ users ->
            if (users.isNotEmpty()) {
                user = users.first()
                binding.homeCard.setUser(user)
                binding.homeCard.visibility = VISIBLE
            }
        }

        /*
        * Medidor
        */
        observe(viewModel.servings) { servings ->
            if (servings.isNotEmpty()) {
                binding.homeServingsPieChart.set(servings)
            }
        }

    }

    private fun FragmentHomeBinding.setUi() {
        swipeRefresh.init { updateAll() }

        homeMenuShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_menu)
        }

        homeCard.setOnClickListener {
            UserOptionsDialog
                .newInstance(user)
                .show(parentFragmentManager, "UserOptionsDialog")
        }
        homeCard.setOnLongClickListener {
            user.isLoading = true
            homeCard.setUser(user)
            viewModel.updateCards(user.card)
            true
        }
        homeCardShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_balance)
        }
        homeServingsPieChart.setTouchEnabled(false)
        homeServingsPieChart.setOnClickListener {
            viewModel.updateServings()
        }
        homeServingsShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_servings)
        }

        addMenu(R.menu.home) { menuItemId ->
            when (menuItemId) {
                R.id.home_update -> {
                    updateAll()
                    true
                }

                else -> false
            }
        }
    }

    private fun updateAll() {
        if (mDayMenu == null) {
            viewModel.refreshMenu()
        }

        viewModel.updateServings()

        if (::user.isInitialized) {
            user.isLoading = true
            binding.homeCard.setUser(user)
            viewModel.updateCards(user.card)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }
}