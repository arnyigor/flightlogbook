package com.arny.flightlogbook.presentation.settings

import android.os.Bundle
import android.preference.PreferenceActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import com.arny.flightlogbook.R


class Preferences : PreferenceActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = findViewById<View>(android.R.id.list).parent.parent.parent as LinearLayout
        val bar = LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false) as Toolbar
        root.addView(bar, 0)
        bar.setNavigationOnClickListener { v -> finish() }
        addPreferencesFromResource(R.xml.preferences)
    }

}