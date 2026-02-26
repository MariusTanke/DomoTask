package com.mariustanke.domotask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Blue status bar, green nav bar — dark scrim so icons are always white
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.parseColor("#FF1565C0")
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.parseColor("#FF4CAF50")
            )
        )

        setContent {
            DomoApp()
        }
    }
}
