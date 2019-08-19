package com.uncmorfi.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.uncmorfi.R
import com.uncmorfi.balance.showOn
import com.uncmorfi.helpers.StatusCode
import com.uncmorfi.helpers.colorOf
import com.uncmorfi.helpers.compareToToday
import com.uncmorfi.helpers.toFormat
import com.uncmorfi.models.User
import com.uncmorfi.servings.init
import com.uncmorfi.servings.update
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.menu_item.*

class HomeFragment : Fragment() {
    private lateinit var mViewModel: MainViewModel
    private lateinit var mUser: User

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        mViewModel.getMenu().observe(this, Observer { menuList ->
            val today = menuList.firstOrNull { it.date.compareToToday() == 0 }
            today?.let {
                val colorDay = requireContext().colorOf(R.color.white)
                val colorFood = requireContext().colorOf(R.color.white)
                val colorBack = requireContext().colorOf(R.color.accent)
                menuCard.setCardBackgroundColor(colorBack)
                menuDayNumber.setTextColor(colorDay)
                menuDayName.setTextColor(colorDay)
                menuFood1.setTextColor(colorFood)
                menuFood2.setTextColor(colorFood)
                menuFood3.setTextColor(colorFood)
                menuDayNumber.text = it.date.toFormat("dd")
                menuDayName.text = it.date.toFormat("EEEE").capitalize()
                menuFood1.text = it.food.getOrNull(0)
                menuFood2.text = it.food.getOrNull(1)
                menuFood3.text = it.food.getOrNull(2)
            }
        })

        homeCard.setOnClickListener { mViewModel.downloadUsers(mUser) }

        mViewModel.allUsers().observe(this, Observer {
            mUser = it[0]
            mUser.showOn(view)
        })

        counterPieChart.init(requireContext())
        mViewModel.updateServings()
        mViewModel.getServings().observe(this, Observer {
            counterPieChart.update(requireContext(), it)
        })

    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.app_name)
    }

    override fun onStop() {
        super.onStop()
        mViewModel.servingStatus.value = StatusCode.BUSY
    }

}
