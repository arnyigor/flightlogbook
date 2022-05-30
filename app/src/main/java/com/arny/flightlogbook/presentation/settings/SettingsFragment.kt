package com.arny.flightlogbook.presentation.settings

import android.os.Bundle
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.arny.flightlogbook.R

class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        val customPref = findPreference<Preference>(getString(R.string.backups_settings_open_key))
        customPref?.setOnPreferenceClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToNavBackups()
            )
            true
        }
    }
}