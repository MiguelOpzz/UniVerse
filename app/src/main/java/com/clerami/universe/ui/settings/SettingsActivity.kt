package com.clerami.universe.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.clerami.universe.R
import com.clerami.universe.databinding.SettingsActivityBinding


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            val fragment = SettingsFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(binding.settings.id, fragment)
                .commit()

            // Pass logic to the fragment after attaching
            fragment.setLanguageClickListener {
                val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                startActivity(intent)
                recreate()
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        private var languageClickListener: (() -> Unit)? = null

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Find the language preference and set its click listener
            val languagePreference = findPreference<Preference>("language_settings")
            languagePreference?.setOnPreferenceClickListener {
                languageClickListener?.invoke()
                true
            }
        }

        fun setLanguageClickListener(listener: () -> Unit) {
            languageClickListener = listener
        }
    }
}
