package com.clerami.universe.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.clerami.universe.R
import com.clerami.universe.ui.profilesettings.ProfileSettingsActivity

class SettingsFragment : PreferenceFragmentCompat() {
    private var languageClickListener: (() -> Unit)? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        // Language Settings
        val languagePreference = findPreference<Preference>("language_settings")
        languagePreference?.setOnPreferenceClickListener {
            languageClickListener?.invoke()
            true
        }

        // Profile Settings
        val profileSettingsPreference = findPreference<Preference>("profile_settings")
        profileSettingsPreference?.setOnPreferenceClickListener {
            // Navigate to ProfileSettingsActivity
            val intent = Intent(requireContext(), ProfileSettingsActivity::class.java)
            startActivity(intent)
            true
        }
    }

    fun setLanguageClickListener(listener: () -> Unit) {
        languageClickListener = listener
    }
}

