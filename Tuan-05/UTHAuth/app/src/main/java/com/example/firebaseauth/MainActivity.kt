package com.example.firebaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.ClearCredentialStateRequest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.firebaseauth.repository.AuthRepository
import com.example.firebaseauth.ui.*
import com.example.firebaseauth.ui.theme.FirebaseAuthTheme
import com.google.firebase.auth.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val tasks: TasksViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(statusBarStyle = androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT), navigationBarStyle = androidx.activity.SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT))
        setContent { FirebaseAuthTheme { UthRoot(tasks) } }
    }
}

@Composable
private fun UthRoot(tasks: TasksViewModel) {
    val auth = remember { FirebaseAuth.getInstance() }
    var user by remember { mutableStateOf(auth.currentUser) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by tasks.state.collectAsStateWithLifecycle()
    val repository = remember { AuthRepository() }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { user = it.currentUser }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    LaunchedEffect(user?.uid) { tasks.bind(user?.uid) }
    fun authenticate(method: String, action: suspend () -> Unit) {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try { action(); tasks.log("login", method) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.localizedMessage ?: "Không thể đăng nhập. Vui lòng thử lại." }
            finally { busy = false }
        }
    }
    if (user == null) {
        AuthScreen(
            busy = busy, error = error,
            onGoogle = { authenticate("google") { repository.signInWithGoogle(context).getOrThrow() } },
            onGuest = { authenticate("anonymous") { auth.signInAnonymously().await() } },
            onEmail = { email, password, register ->
                authenticate("password") {
                    if (register) { auth.createUserWithEmailAndPassword(email, password).await(); tasks.log("sign_up") }
                    else auth.signInWithEmailAndPassword(email, password).await()
                }
            },
            onReset = { email -> authenticate("password_reset") {
                auth.sendPasswordResetEmail(email).await()
                error = "Đã gửi yêu cầu đặt lại mật khẩu. Kiểm tra email nếu tài khoản tồn tại."
            } }
        )
    } else {
        TasksScreen(
            user = com.example.firebaseauth.data.Account(user!!.displayName, user!!.email, user!!.photoUrl?.toString(), user!!.isAnonymous, user!!.providerData.lastOrNull()?.providerId ?: "Firebase"), state = state,
            onSave = tasks::save, onToggle = tasks::toggle, onDelete = tasks::delete,
            onSamples = tasks::addSamples, onConfigure = tasks::configure,
            onFetch = tasks::fetchConfig, onDismiss = tasks::dismissMessage,
            onFilter = { tasks.log("category_filtered", it) },
            onSignOut = {
                auth.signOut()
                scope.launch {
                    try { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
                    catch (e: CancellationException) { throw e }
                    catch (_: Exception) { }
                }
            }
        )
    }
}
