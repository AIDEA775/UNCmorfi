package com.uncmorfi.reservations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.uncmorfi.R
import com.uncmorfi.helpers.SnackType
import com.uncmorfi.helpers.snack
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_reservation.*
import java.text.DateFormatSymbols
import java.util.*

class ReservationFragment : Fragment() {
    private lateinit var mViewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        val sharedPref = requireActivity().getSharedPreferences("prefs" ,Context.MODE_PRIVATE)

        val days = DateFormatSymbols.getInstance().shortWeekdays

        // Fixme debe haber una mejor forma de hacer esto
        // MONDAY=2, por eso el [i-2], así indexamos desde 0
        for (i in Calendar.MONDAY..Calendar.FRIDAY) {
            val chip = chipGroup[i-2] as Chip
            chip.text = days[i].capitalize()
            val scheduled = sharedPref.getBoolean(i.toString(), false)
            chip.isChecked = scheduled
        }

        button.setOnClickListener {
            AlarmHelper.enableBootReceiver(requireContext())
            AlarmHelper.createNotificationChannel(requireContext())

            with (sharedPref.edit()) {
                for (day in Calendar.MONDAY..Calendar.FRIDAY) {
                    val chip = chipGroup[day - 2] as Chip
                    val schedule = chip.isChecked
                    AlarmHelper.scheduleAlarm(requireContext(), day, schedule)
                    putBoolean(day.toString(), schedule)
                }
                apply()
            }
            view.snack(context, R.string.saved, SnackType.FINISH)
        }

        button2.setOnClickListener {
            AlarmHelper.makeNotification(context!!)
        }
    }

}
