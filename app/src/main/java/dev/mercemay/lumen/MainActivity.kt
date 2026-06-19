package dev.mercemay.lumen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dev.mercemay.lumen.ui.navigation.LumenNavHost
import dev.mercemay.lumen.ui.theme.LumenTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LumenTheme {
                LumenNavHost()
            }
        }
    }
}
