package io.dolby.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.dolby.app.common.ui.MillicastTheme
import io.dolby.app.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MillicastTheme {
                AppNavigation()
            }
        }
    }
}
