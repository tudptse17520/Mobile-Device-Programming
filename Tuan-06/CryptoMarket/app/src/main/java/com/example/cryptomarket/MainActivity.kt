package com.example.cryptomarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptomarket.ui.MarketScreen
import com.example.cryptomarket.viewmodel.MarketViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val marketViewModel: MarketViewModel = viewModel()
            val colors = darkColorScheme(primary = Color(0xFF6695FF), background = Color(0xFF191B2A), surface = Color(0xFF292C3A))
            MaterialTheme(colorScheme = colors) { MarketScreen(marketViewModel) }
        }
    }
}
