package com.uncmorfi.ui.reminders

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.uncmorfi.MainViewModel
import com.uncmorfi.R

class RemindersFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.reminders_screen, rootKey)

        val reservation = findPreference<MultiSelectListPreference>("reminder_reservation")!!
        reservation.summaryProvider = SummaryProvider()
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_reminders)
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPref: SharedPreferences, key: String) {
        viewModel.refreshWorkers()
    }

    class SummaryProvider : Preference.SummaryProvider<MultiSelectListPreference> {

        override fun provideSummary(pref: MultiSelectListPreference): CharSequence {
            return pref.values
                .joinToString(", ") { pref.entries[pref.findIndexOfValue(it)] }
                .ifEmpty { pref.context.getString(R.string.reminders_reservation_empty) }
        }
    }

}
