package com.neilturner.aerialviews.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.neilturner.aerialviews.R
import com.neilturner.aerialviews.utils.LoggingHelper
import com.neilturner.aerialviews.utils.toStringOrEmpty

class AppearanceMessageFragment :
    PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_appearance_message, rootKey)
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        limitTextInput()
        updateSummary()
    }

    override fun onResume() {
        super.onResume()
        LoggingHelper.logScreenView("Message", TAG)
    }

    override fun onDestroy() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        updateSummary()
    }

    private fun updateSummary() {
        // Line 1
        val line1 = findPreference<EditTextPreference>("message_line1")
        if (line1?.text.toStringOrEmpty().isNotEmpty()) {
            line1?.summary = line1?.text
        } else {
            line1?.summary = getString(R.string.appearance_message_line1_summary)
        }

        // Line 2
        val line2 = findPreference<EditTextPreference>("message_line2")
        if (line2?.text.toStringOrEmpty().isNotEmpty()) {
            line2?.summary = line2?.text
        } else {
            line2?.summary = getString(R.string.appearance_message_line1_summary)
        }
    }

    private fun limitTextInput() {
        preferenceScreen.findPreference<EditTextPreference>("message_line1")?.setOnBindEditTextListener { it.setSingleLine() }
        preferenceScreen.findPreference<EditTextPreference>("message_line2")?.setOnBindEditTextListener { it.setSingleLine() }
    }

    companion object {
        private const val TAG = "MessageFragment"
    }
}
