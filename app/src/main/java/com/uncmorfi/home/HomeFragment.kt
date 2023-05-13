package com.uncmorfi.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.uncmorfi.MainActivity
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.ReserveStatus.NOCACHED
import com.uncmorfi.shared.StatusCode
import com.uncmorfi.shared.getUser
import com.uncmorfi.shared.init
import com.uncmorfi.shared.observe
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var user: User
    private var mDayMenu: DayMenu? = null
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        swipeRefresh.init { updateAll() }

        observe(viewModel.status) {
            swipeRefresh.isRefreshing = it == StatusCode.UPDATING
        }

        /*
         * Menú
         */
        observe(viewModel.getMenu()) { menu ->
            val today = menu.firstOrNull { it.isToday() }
            mDayMenu = today

            today?.let {
                homeMenu.setDayMenu(today)
                homeMenu.visibility = VISIBLE
            }
        }

        homeMenuShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_menu)
        }

        /*
         * Tarjetas
         */
        observe(viewModel.allUsers()) {
            if (it.isNotEmpty()) {
                user = it.first()
                homeCard.setUser(user)
                homeCard.visibility = VISIBLE
            }
        }
        homeCard.setOnClickListener {
            UserOptionsDialog
                .newInstance(this, USER_OPTIONS_CODE, user)
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

        /*
         * Medidor
         */
        observe(viewModel.getServings()) {
            if (it.isNotEmpty()) {
                homeServingsPieChart.set(it)
            }
        }

        homeServingsPieChart.setTouchEnabled(false)
        homeServingsPieChart.setOnClickListener {
            viewModel.updateServings()
        }
        homeServingsShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_servings)
        }
    }

    private fun updateAll() {
        if (mDayMenu == null) {
            viewModel.refreshMenu()
        }

        viewModel.updateServings()

        if (::user.isInitialized) {
            user.isLoading = true
            homeCard.setUser(user)
            viewModel.updateCards(user.card)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            USER_OPTIONS_CODE -> {
                val user = data.getUser()
                user.isLoading = true
                homeCard.setUser(user)
                viewModel.updateCards(user.card)
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home_update -> {
                updateAll(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }

    override fun onStop() {
        super.onStop()
        viewModel.reservation.value = NOCACHED
    }

    companion object {
        private const val USER_OPTIONS_CODE = 1
    }
}
