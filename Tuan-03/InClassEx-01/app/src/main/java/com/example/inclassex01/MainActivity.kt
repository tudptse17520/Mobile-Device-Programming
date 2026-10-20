package com.example.inclassex01

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.inclassex01.ui.theme.InClassEx01Theme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InClassEx01Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ExScreen(modifier: Modifier = Modifier) {
    var message by remember {
        mutableStateOf("Hello")
    }
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My First App",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message
        )
        Button(onClick = {message = "I'm Thanh Tu"}) {
            Text(text = "Say Hi!")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ExScreenPreview() {
    InClassEx01Theme {
        ExScreen()
    }
}