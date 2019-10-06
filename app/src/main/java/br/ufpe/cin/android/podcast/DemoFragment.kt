package br.ufpe.cin.android.podcast

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class DemoFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences_activity from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}