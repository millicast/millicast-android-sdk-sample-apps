package io.dolby.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.dolby.app.ui.navigation.AppNavigation
import io.dolby.uikit.theme.MillicastTheme

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
