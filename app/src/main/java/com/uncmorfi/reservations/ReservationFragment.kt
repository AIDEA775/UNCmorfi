package com.uncmorfi.reservations

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.uncmorfi.R
import com.uncmorfi.shared.SnackType
import com.uncmorfi.shared.snack
import com.uncmorfi.MainViewModel
import kotlinx.android.synthetic.main.fragment_reservation.*
import java.util.*

class ReservationFragment : Fragment() {
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_reservation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        reservation_1.initDays()
        reservation_1.setHeader(R.string.reservations_head1)
        reservation_2.initDays()
        reservation_2.setHeader(R.string.reservations_head2)

        val sharedPref = requireActivity().getSharedPreferences("prefs" ,Context.MODE_PRIVATE)
        for (i in Calendar.MONDAY..Calendar.FRIDAY) {
            val value = sharedPref.getInt(i.toString(), -1)

            if (value == 1 || value == 3) {
                reservation_1.setChecked(i, true)
            }
            if (value == 2 || value == 3) {
                reservation_2.setChecked(i, true)
            }
        }

        reservationSave.setOnClickListener {
            AlarmHelper.enableBootReceiver(requireContext())
            AlarmHelper.createNotificationChannel(requireContext())

            with (sharedPref.edit()) {
                for (day in Calendar.MONDAY..Calendar.FRIDAY) {
                    var value = if (reservation_1.getChecked(day)) 1 else 0
                    value += if (reservation_2.getChecked(day)) 2 else 0
                    if (value > 0) {
                        putInt(day.toString(), value)
                    }
                }
                apply()
            }
            val calendar = AlarmHelper.getNextAlarm(requireContext())
            calendar?.let {
                AlarmHelper.scheduleAlarm(requireContext(), calendar)
            }
            view.snack(R.string.saved, SnackType.FINISH)
        }

        reservationClear.setOnClickListener {
            with (sharedPref.edit()) {
                for (day in Calendar.MONDAY..Calendar.FRIDAY) {
                    remove(day.toString())
                    reservation_1.setChecked(day, false)
                    reservation_2.setChecked(day, false)
                }
                apply()
            }
            AlarmHelper.cancelAlarm(requireContext())
            view.snack(R.string.cleaned, SnackType.FINISH)
        }
    }


}
