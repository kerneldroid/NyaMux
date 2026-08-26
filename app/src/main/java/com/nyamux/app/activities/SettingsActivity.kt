package com.nyamux.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nyamux.app.compose.setSettingsContent
import com.nyamux.shared.activity.media.AppCompatActivityUtils
import com.nyamux.shared.theme.NightMode

open class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatActivityUtils.setNightMode(this, NightMode.getAppNightMode().name, true)

        setSettingsContent(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
