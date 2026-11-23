package com.example.productapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.productapp.ui.screen.ProductDetailScreen
import com.example.productapp.ui.theme.ProductAppTheme

/**
 * Activity chính của ứng dụng.
 * Sử dụng Jetpack Compose để hiển thị giao diện.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProductAppTheme {
                ProductDetailScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}
