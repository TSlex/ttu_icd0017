package com.tslex.lifetrack

import android.os.Bundle
import android.preference.PreferenceActivity

class Preferences : PreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
    }
}
