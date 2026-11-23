package com.example.firebaseauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    busy: Boolean, error: String?, onGoogle: () -> Unit, onGuest: () -> Unit,
    onEmail: (String, String, Boolean) -> Unit, onReset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var emailMode by rememberSaveable { mutableStateOf(false) }
    var register by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    if (!emailMode) {
        Column(modifier.fillMaxSize()) {
            LoginScreen(busy, error, onGoogle, onGuest, { emailMode = true }, Modifier.weight(1f))
            TextButton(
                onClick = { emailMode = true }, enabled = !busy,
                modifier = Modifier.fillMaxWidth().navigationBarsPadding()
            ) { Text("Đăng nhập / Đăng ký bằng email") }
        }
    } else {
        Column(
            modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("UTH SmartTasks", style = MaterialTheme.typography.headlineLarge)
            Text(if (register) "Tạo tài khoản" else "Chào mừng trở lại", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password, { password = it }, label = { Text("Mật khẩu (ít nhất 6 ký tự)") },
                visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
            if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            Button(onClick = { onEmail(email.trim(), password, register) },
                enabled = !busy && android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() && password.length >= 6,
                modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Đang xử lý…" else if (register) "Đăng ký" else "Đăng nhập") }
            TextButton(onClick = { register = !register }, enabled = !busy) { Text(if (register) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký") }
            TextButton(onClick = { onReset(email.trim()) }, enabled = !busy && email.contains("@")) { Text("Quên mật khẩu") }
            TextButton(onClick = { emailMode = false }, enabled = !busy) { Text("Quay lại đăng nhập Google") }
        }
    }
}
