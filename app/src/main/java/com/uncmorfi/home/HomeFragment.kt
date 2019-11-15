package com.uncmorfi.home

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uncmorfi.MainActivity
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.models.DayMenu
import com.uncmorfi.models.User
import com.uncmorfi.shared.ReserveStatus.NOCACHED
import com.uncmorfi.shared.StatusCode.BUSY
import com.uncmorfi.shared.compareToToday
import com.uncmorfi.shared.getUser
import com.uncmorfi.shared.init
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var mUser: User
    private var mDayMenu: DayMenu? = null
    private lateinit var mRootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        swipeRefresh.init { updateAll() }

        /*
         * Menú
         */
        mViewModel.getMenu().observe(viewLifecycleOwner, Observer { menuList ->
            val today = menuList.firstOrNull { it.date.compareToToday() == 0 }
            mDayMenu = today

            if (today != null) {
                homeMenu.setDayMenu(today)
                homeMenuTitle.visibility = VISIBLE
                homeMenuContainer.visibility = VISIBLE
            } else {
                homeMenuTitle.visibility = GONE
                homeMenuContainer.visibility = GONE
            }
        })
        homeMenuShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_menu)
        }

        /*
         * Tarjetas
         */
        mViewModel.allUsers().observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                mUser = it.first()
                homeCard.setUser(mUser)
                homeCardTitle.visibility = VISIBLE
                homeCardContainer.visibility = VISIBLE
            } else {
                homeCardTitle.visibility = GONE
                homeCardContainer.visibility = GONE
            }
        })
        homeCard.setOnClickListener {
            UserOptionsDialog
                    .newInstance(this, USER_OPTIONS_CODE, mUser)
                    .show(parentFragmentManager, "UserOptionsDialog")
        }
        homeCard.setOnLongClickListener {
            mUser.isLoading = true
            homeCard.setUser(mUser)
            mViewModel.downloadUsers(mUser)
            true
        }
        homeCardShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_balance)
        }

        /*
         * Medidor
         */
        mViewModel.getServings().observe(viewLifecycleOwner, Observer {
            if (it.isNotEmpty()) {
                homeServingsPieChart.set(it)
            }
        })
        homeServingsPieChart.setTouchEnabled(false)
        homeServingsPieChart.setOnClickListener {
            mViewModel.updateServings()
        }
        homeServingsShowMore.setOnClickListener {
            (requireActivity() as MainActivity).change(R.id.nav_servings)
        }
    }

    private fun updateAll() {
        if (mDayMenu == null) {
            mViewModel.updateMenu()
        }

        mViewModel.updateServings()

        if (::mUser.isInitialized) {
            mUser.isLoading = true
            homeCard.setUser(mUser)
            mViewModel.downloadUsers(mUser)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            USER_OPTIONS_CODE -> {
                val user = data.getUser()
                user.isLoading = true
                homeCard.setUser(user)
                mViewModel.downloadUsers(user)
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
            R.id.home_update -> { updateAll(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.reservation.value = NOCACHED
    }

    companion object {
        private const val USER_OPTIONS_CODE = 1
    }
}
