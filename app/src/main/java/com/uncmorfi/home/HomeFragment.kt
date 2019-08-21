package com.uncmorfi.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.helpers.SnackType
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.helpers.compareToToday
import com.uncmorfi.helpers.getUser
import com.uncmorfi.helpers.snack
import com.uncmorfi.models.User
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var mUser: User
    private lateinit var mRootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        /*
         * Menú
         */
        mViewModel.getMenu().observe(this, Observer { menuList ->
            val today = menuList.firstOrNull { it.date.compareToToday() == 0 }
            today?.let {
                homeMenu.setDayMenu(it)
            }
        })

        /*
         * Tarjetas
         */
        mViewModel.allUsers().observe(this, Observer {
            if (it.isNotEmpty()) {
                mUser = it[0]
                homeCard.setUser(mUser)
                homeCardContainer.visibility = VISIBLE
                homeCardTitle.visibility = VISIBLE
            } else {
                homeCardContainer.visibility = GONE
                homeCardTitle.visibility = GONE
            }
        })
        homeCard.setOnClickListener {
            showUserOptionsDialog(mUser)
        }
        homeCard.setOnLongClickListener {
            mUser.isLoading = true
            homeCard.setUser(mUser)
            mViewModel.downloadUsers(mUser)
            true
        }
        mViewModel.userStatus.observe(this, Observer {
            when (it) {
                BUSY -> {}
                UPDATED -> {
                    mRootView.snack(context, R.string.update_success, SnackType.FINISH)
                }
                INSERTED -> {
                    mRootView.snack(context, R.string.new_user_success, SnackType.FINISH)
                }
                DELETED -> mRootView.snack(context, R.string.balance_delete_user_msg, SnackType.FINISH)
                else -> mRootView.snack(context, it)
            }
        })

        /*
         * Medidor
         */
        mViewModel.getServings().observe(this, Observer {
            servingsPieChart.set(it)
        })
        servingsPieChart.setTouchEnabled(true)
        servingsPieChart.setTouchListener { mViewModel.updateServings() }
    }

    private fun showUserOptionsDialog(user: User) {
        UserOptionsDialog.newInstance(this, USER_OPTIONS_CODE, user)
                .show(fragmentManager!!, "UserOptionsDialog")
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

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.menuStatus.value = BUSY
        mViewModel.userStatus.value = BUSY
        mViewModel.servingStatus.value = BUSY
    }
    companion object {
        private const val USER_OPTIONS_CODE = 1
    }
}
