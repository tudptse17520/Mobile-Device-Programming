package com.example.devicepermissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

private val Orange = Color(0xFFE8783D)
private val Gold = Color(0xFFF4B832)
private val DarkText = Color(0xFF202536)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PermissionFlowScreen() }
    }
}

private data class PermissionPage(
    val title: String,
    val description: String,
    val button: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val permission: String?
)

@Composable
private fun PermissionFlowScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pages = listOf(
        PermissionPage("Location", "Allow maps to access your location while you use the app?", "Allow", Icons.Default.LocationOn, Manifest.permission.ACCESS_FINE_LOCATION),
        PermissionPage("Notification", "Please enable notifications to receive updates and reminders", "Turn on", Icons.Default.NotificationsActive, if (Build.VERSION.SDK_INT >= 33) Manifest.permission.POST_NOTIFICATIONS else null),
        PermissionPage("Camera", "We need access to your camera to scan QR codes", "Turn on", Icons.Default.CameraAlt, Manifest.permission.CAMERA)
    )
    var page by rememberSaveable { mutableIntStateOf(0) }
    var language by rememberSaveable { mutableStateOf("ENG") }
    var lastResult by rememberSaveable { mutableStateOf("") }
    var showDetails by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        val granted = result.values.any { it }
        lastResult = if (granted) "Permission enabled" else "Permission skipped or denied"
        page = (page + 1).coerceAtMost(pages.size)
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF283846), Color(0xFF111A26), Color(0xFF6E513B))))) {
        // A calm app background keeps focus on the permission card, as in the reference image.
        Column(Modifier.fillMaxSize().padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(top = 50.dp), horizontalArrangement = Arrangement.End) {
                Row(Modifier.clip(CircleShape).background(Color.White.copy(alpha = .17f)).padding(4.dp)) {
                    listOf("বাংলা", "ENG").forEach { code ->
                        TextButton(onClick = { language = code }, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), colors = ButtonDefaults.textButtonColors(contentColor = Color.White)) {
                            Text(code, fontWeight = if (language == code) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Column(Modifier.fillMaxWidth().padding(bottom = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Welcome back", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Sign in to continue", color = Color.White.copy(alpha = .8f), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
                Button(onClick = { showDetails = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text("Login") }
                OutlinedButton(onClick = { showDetails = true }, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("Continue without account") }
            }
        }

        if (page < pages.size) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .55f)), contentAlignment = Alignment.Center) {
                PermissionCard(
                    page = pages[page],
                    language = language,
                    onAllow = {
                        val permission = pages[page].permission
                        when {
                            permission == null -> { lastResult = "Notifications are available without a prompt on this Android version"; page++ }
                            permission == Manifest.permission.ACCESS_FINE_LOCATION -> permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
                            else -> permissionLauncher.launch(arrayOf(permission))
                        }
                    },
                    onSkip = { lastResult = "${pages[page].title} skipped"; page++ }
                )
            }
        } else {
            Row(Modifier.align(Alignment.TopCenter).padding(top = 10.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(lastResult.ifBlank { "Permission setup complete" }, color = DarkText, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { page = 0 }) { Text("Review") }
            }
        }
    }

    if (showDetails) {
        AlertDialog(
            onDismissRequest = { showDetails = false },
            title = { Text("Permission status") },
            text = {
                Column {
                    pages.forEach { item ->
                        val granted = item.permission == null || ContextCompat.checkSelfPermission(context, item.permission) == PackageManager.PERMISSION_GRANTED
                        Text("${item.title}: ${if (granted) "Allowed" else "Not allowed"}", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDetails = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun PermissionCard(page: PermissionPage, language: String, onAllow: () -> Unit, onSkip: () -> Unit) {
    val isBengali = language == "বাংলা"
    val title = if (isBengali) when (page.title) { "Location" -> "অবস্থান"; "Notification" -> "বিজ্ঞপ্তি"; else -> "ক্যামেরা" } else page.title
    val body = if (isBengali) when (page.title) {
        "Location" -> "অ্যাপ ব্যবহার করার সময় মানচিত্রকে আপনার অবস্থান ব্যবহার করতে দিন?"
        "Notification" -> "আপডেট এবং অনুস্মারক পেতে বিজ্ঞপ্তি চালু করুন"
        else -> "QR কোড স্ক্যান করতে ক্যামেরা ব্যবহার করতে হবে"
    } else page.description
    val primary = if (isBengali) "অনুমতি দিন" else page.button
    val skip = if (isBengali) "এখন এড়িয়ে যান" else "Skip for now"

    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(92.dp).clip(RoundedCornerShape(35.dp)).background(Color(0xFFE7F6EF)))
                Icon(page.icon, null, tint = when (page.title) { "Notification" -> Gold; "Camera" -> Orange; else -> Color(0xFFE45E48) }, modifier = Modifier.size(50.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkText)
            Text(body, Modifier.padding(top = 10.dp, bottom = 24.dp), color = Color(0xFF62636A), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onAllow, modifier = Modifier.fillMaxWidth().height(46.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Orange)) { Text(primary, color = Color.White) }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth().height(46.dp), shape = CircleShape, border = androidx.compose.foundation.BorderStroke(1.dp, Orange), colors = ButtonDefaults.outlinedButtonColors(contentColor = Orange)) { Text(skip) }
        }
    }
}
