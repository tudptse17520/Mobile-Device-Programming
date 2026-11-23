package com.example.uthsmarttasks.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Bitmap
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.provider.Settings
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.uthsmarttasks.R
import com.example.uthsmarttasks.BuildConfig
import com.example.uthsmarttasks.auth.repository.AvatarStorage
import com.example.uthsmarttasks.model.Subtask
import com.example.uthsmarttasks.model.Task
import com.example.uthsmarttasks.model.CreateTaskRequest
import com.example.uthsmarttasks.model.tasksForToday
import com.example.uthsmarttasks.viewmodel.TaskViewModel
import com.example.uthsmarttasks.viewmodel.TaskUiState
import com.example.uthsmarttasks.auth.model.AuthUiState
import com.example.uthsmarttasks.auth.ui.AuthViewModel
import com.example.uthsmarttasks.ui.theme.TodoAccents
import com.example.uthsmarttasks.notifications.TaskReminder
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private val Blue: Color @Composable get() = MaterialTheme.colorScheme.primary
private val PaleBlue: Color @Composable get() = if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF1E2A4D) else Color(0xFFE6EDFF)
private val LinkBlue: Color @Composable get() =
    if (MaterialTheme.colorScheme.background == Color.Black && Blue == Color(0xFF2F5BEA)) Color(0xFF7C9BFF) else Blue
private val Ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
private val Muted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val SurfaceGray: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
private val RowDivider: Color @Composable get() =
    if (MaterialTheme.colorScheme.background == Color.Black) SurfaceGray else Color(0xFFEDEDF0)

@Composable
private fun topAfterStatusBar(target: Dp): Dp = with(LocalDensity.current) {
    (target - WindowInsets.statusBars.getTop(this).toDp()).coerceAtLeast(0.dp)
}

private enum class Page { START, HOME, LIST, ADD, EDIT, DETAIL, SETTINGS, PROFILE, NOTIFICATIONS, ONBOARDING, LOGIN }

@Composable
fun TodoApp(
    viewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    requestNotificationPermissionOnHome: Boolean = false
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val profileError by authViewModel.profileError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPrefs = remember { context.getSharedPreferences("uth_notifications", android.content.Context.MODE_PRIVATE) }
    val notificationReadKey = state.cloudUid?.let { "read_$it" } ?: "read"
    var readNotificationKeys by remember(notificationReadKey) {
        mutableStateOf(notificationPrefs.getStringSet(notificationReadKey, emptySet())?.toSet() ?: emptySet())
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) TaskReminder.restore(context)
    }
    val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
    fun needsNotificationPermission() = Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, notificationPermission) != PackageManager.PERMISSION_GRANTED

    fun requestNotificationPermission() {
        if (!needsNotificationPermission()) return
        notificationPermissionLauncher.launch(notificationPermission)
        notificationPrefs.edit().putBoolean("permission_requested", true).apply()
    }

    var guestMode by rememberSaveable { mutableStateOf(false) }
    var page by rememberSaveable { mutableStateOf(if (authState is AuthUiState.Success) Page.HOME.name else Page.START.name) }
    var selectedTaskId by rememberSaveable { mutableIntStateOf(2) }
    val selectedTask = state.tasks.firstOrNull { it.id == selectedTaskId }
    var detailReturn by rememberSaveable { mutableStateOf(Page.LIST.name) }
    var addReturn by rememberSaveable { mutableStateOf(Page.HOME.name) }
    var notificationsReturn by rememberSaveable { mutableStateOf(Page.HOME.name) }
    var listFilter by rememberSaveable { mutableStateOf("Tất cả") }
    val go = { next: Page ->
        if (next == Page.DETAIL && page in listOf(Page.HOME.name, Page.LIST.name)) detailReturn = page
        if (next == Page.ADD) addReturn = page
        if (next == Page.NOTIFICATIONS) notificationsReturn = page
        if (next == Page.LIST && page != Page.LIST.name) viewModel.loadTasksOnListEntry()
        page = next.name
    }
    BackHandler(enabled = page != Page.HOME.name && page != Page.START.name) {
        page = when (Page.valueOf(page)) {
            Page.DETAIL -> detailReturn
            Page.ADD -> addReturn
            Page.EDIT -> Page.DETAIL.name
            Page.PROFILE -> Page.SETTINGS.name
            Page.NOTIFICATIONS -> notificationsReturn
            Page.LOGIN -> Page.ONBOARDING.name
            Page.ONBOARDING -> Page.LOGIN.name
            else -> Page.HOME.name
        }
    }
    LaunchedEffect(state.message) {
        if (state.message != null && !state.messageIsError) { delay(3_000); viewModel.clearMessage() }
    }
    LaunchedEffect(guestMode, state.cloudUid) {
        if (guestMode) viewModel.startGuestSession()
    }
    LaunchedEffect(page, state.remindersEnabled, state.dailySummary) {
        val remindersInUse = state.remindersEnabled || state.dailySummary
        if (requestNotificationPermissionOnHome && page == Page.HOME.name && remindersInUse &&
            !notificationPrefs.getBoolean("permission_requested", false)
        ) {
            requestNotificationPermission()
        }
    }
    val activity = context as? Activity
    val isDark = MaterialTheme.colorScheme.background == Color.Black
    val screenStatusBarColor = when {
        page == Page.START.name -> Blue
        page == Page.ONBOARDING.name || page == Page.LOGIN.name -> Color.White
        else -> MaterialTheme.colorScheme.background
    }
    SideEffect {
        activity?.window?.apply {
            statusBarColor = screenStatusBarColor.toArgb()
            navigationBarColor = if (isDark) Color.Black.toArgb() else Color.White.toArgb()
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightStatusBars = !isDark && page != Page.START.name
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success && page in listOf(Page.START.name, Page.ONBOARDING.name, Page.LOGIN.name)) {
            guestMode = false
            go(Page.HOME)
        } else if (authState is AuthUiState.Idle && !guestMode && page == Page.HOME.name) {
            go(Page.START)
        }
    }
    LaunchedEffect(page, authState) {
        if (page == Page.START.name && authState !is AuthUiState.Success) {
            delay(2_000)
            if (page == Page.START.name) go(Page.ONBOARDING)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = screenStatusBarColor,
        bottomBar = {
            if (page == Page.HOME.name || page == Page.LIST.name || page == Page.SETTINGS.name || page == Page.NOTIFICATIONS.name) {
                Box(Modifier.background(MaterialTheme.colorScheme.surface)) {
                    HorizontalDivider(color = if (isDark) SurfaceGray else Color(0xFFE5E5EA))
                    Row(Modifier.fillMaxWidth().navigationBarsPadding().height(62.dp).padding(start = 8.dp, end = 8.dp, top = 6.dp), verticalAlignment = Alignment.Top) {
                        BottomNavItem("Trang chủ", R.drawable.ic_figma_home, page == Page.HOME.name, { go(Page.HOME) })
                        BottomNavItem("Danh sách", R.drawable.ic_figma_list, page == Page.LIST.name, { go(Page.LIST) })
                        Box(Modifier.weight(1f), contentAlignment = Alignment.TopCenter) {
                            FloatingActionButton(onClick = { go(Page.ADD) }, modifier = Modifier.offset(y = (-20).dp).size(56.dp), shape = CircleShape, containerColor = Blue, contentColor = Color.White, elevation = FloatingActionButtonDefaults.elevation(8.dp)) {
                                Icon(painterResource(R.drawable.ic_figma_add), "Thêm mới", modifier = Modifier.size(26.dp))
                            }
                        }
                        BottomNavItem("Thông báo", R.drawable.ic_figma_notifications, page == Page.NOTIFICATIONS.name, { go(Page.NOTIFICATIONS) })
                        BottomNavItem("Cài đặt", R.drawable.ic_figma_settings, page == Page.SETTINGS.name, { go(Page.SETTINGS) })
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            val showTaskStatus = page !in listOf(Page.START.name, Page.ONBOARDING.name, Page.LOGIN.name)
            if (showTaskStatus && state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.message?.takeIf { showTaskStatus }?.let { message ->
                Surface(color = if (state.demoMode) PaleBlue else MaterialTheme.colorScheme.surfaceVariant, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        if (state.messageIsError && page in listOf(Page.HOME.name, Page.LIST.name, Page.NOTIFICATIONS.name, Page.PROFILE.name)) TextButton(onClick = viewModel::loadTasks, enabled = !state.loading) { Text("Thử lại") }
                        TextButton(onClick = viewModel::clearMessage) { Text("Đóng") }
                    }
                }
            }
            when (Page.valueOf(page)) {
                Page.HOME -> HomeScreen(
                    tasks = state.tasks,
                    loading = state.loading,
                    readNotificationKeys = readNotificationKeys,
                    displayName = (authState as? AuthUiState.Success)?.displayName,
                    onTask = { id -> selectedTaskId = id; viewModel.loadTask(id) { page = detailReturn }; go(Page.DETAIL) },
                    onDone = viewModel::toggleDone,
                    onRefresh = { listFilter = "Tất cả"; go(Page.LIST) },
                    onNotifications = { go(Page.NOTIFICATIONS) },
                    onCategory = { category -> listFilter = category; go(Page.LIST) },
                    onProfile = { go(Page.PROFILE) }
                )
                Page.LIST -> TaskListScreen(state.tasks, state.loading, { id -> selectedTaskId = id; viewModel.loadTask(id) { page = detailReturn }; go(Page.DETAIL) }, viewModel::toggleDone, filter = listFilter, onFilter = { listFilter = it })
                Page.ADD -> AddTaskScreen(loading = state.loading, onBack = { page = addReturn }, onSave = { request -> viewModel.createTask(request) { id -> selectedTaskId = id; detailReturn = addReturn; go(Page.DETAIL) } })
                Page.EDIT -> selectedTask?.let { task -> AddTaskScreen(initialTask = task, loading = state.loading, onBack = { go(Page.DETAIL) }, onSave = { request -> viewModel.updateTask(task, request) { go(Page.DETAIL) } }) } ?: EmptyDetail { go(Page.LIST) }
                Page.DETAIL -> if (selectedTask != null) DetailScreen(
                    task = selectedTask,
                    backLabel = when (Page.valueOf(detailReturn)) {
                        Page.HOME -> "Trang chủ"
                        Page.SETTINGS -> "Cài đặt"
                        Page.NOTIFICATIONS -> "Thông báo"
                        else -> "Danh sách"
                    },
                    onBack = { page = detailReturn },
                    onEdit = { go(Page.EDIT) },
                    onDone = viewModel::toggleDone,
                    onAddSubtask = viewModel::addSubtask,
                    onToggleSubtask = viewModel::toggleSubtask,
                    onEditSubtask = viewModel::editSubtask,
                    onDeleteSubtask = viewModel::deleteSubtask,
                    onDelete = { task -> viewModel.deleteTask(task) { page = detailReturn } }
                ) else EmptyDetail { page = detailReturn }
                Page.SETTINGS -> SettingsScreen(
                    settings = state,
                    authState = authState,
                    onTheme = viewModel::setTheme,
                    onAccent = viewModel::setAccent,
                    onFontScale = viewModel::setFontScale,
                    onReminders = viewModel::setReminderEnabled,
                    onSound = viewModel::setNotificationSound,
                    onDailySummary = viewModel::setDailySummary,
                    onNotifications = { go(Page.NOTIFICATIONS) },
                    onRequestNotifications = {
                        if (needsNotificationPermission()) requestNotificationPermission()
                        else TaskReminder.restore(context)
                    },
                    onNotificationSettings = {
                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    },
                    onExactAlarmSettings = if (Build.VERSION.SDK_INT >= 31) {
                        {
                            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                Uri.fromParts("package", context.packageName, null)))
                        }
                    } else null,
                    onProfile = { go(Page.PROFILE) },
                    onOnboarding = { go(Page.ONBOARDING) },
                    onSignOut = {
                        guestMode = false
                        authViewModel.signOut(context)
                        go(Page.LOGIN)
                    }
                )
                Page.PROFILE -> ProfileScreen(
                    tasks = state.tasks,
                    taskListLoadFailed = state.taskListLoadFailed,
                    syncStatus = state.syncStatus,
                    authState = authState,
                    avatarUid = state.cloudUid,
                    onBack = { go(Page.SETTINGS) },
                    onLogin = { go(Page.LOGIN) },
                    onUpdateName = authViewModel::updateDisplayName,
                    profileError = profileError,
                    onDismissProfileError = authViewModel::dismissProfileError,
                    onSync = viewModel::loadTasks,
                    onSignOut = { guestMode = false; authViewModel.signOut(context); go(Page.LOGIN) }
                )
                Page.NOTIFICATIONS -> NotificationsScreen(state.tasks, readNotificationKeys, onMarkRead = { keys ->
                    readNotificationKeys = readNotificationKeys + keys
                    notificationPrefs.edit().putStringSet(notificationReadKey, readNotificationKeys).apply()
                })
                Page.START -> StartScreen(onContinue = { go(Page.ONBOARDING) })
                Page.ONBOARDING -> OnboardingScreen(onContinue = { go(if (authState is AuthUiState.Success) Page.HOME else Page.LOGIN) }, onSkip = { go(if (authState is AuthUiState.Success) Page.HOME else Page.LOGIN) })
                Page.LOGIN -> LoginScreen(
                    onBack = { go(Page.ONBOARDING) },
                    onContinue = { authViewModel.signInWithGoogle(context) },
                    onGuest = { guestMode = true; go(Page.HOME) },
                    loading = authState is AuthUiState.Loading,
                    errorMessage = (authState as? AuthUiState.Error)?.message
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    tasks: List<Task>, loading: Boolean, readNotificationKeys: Set<String>, displayName: String?, onTask: (Int) -> Unit,
    onDone: (Task) -> Unit, onRefresh: () -> Unit, onNotifications: () -> Unit,
    onProfile: () -> Unit, onCategory: (String) -> Unit
) {
    val todayTasks = tasksForToday(tasks)
    val nowKey = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
    val hasUpcomingReminder = tasks.any { task ->
        task.date.isNotBlank() && (task.done || task.remind || "${task.date} ${task.time}" < nowKey) &&
            notificationKey(task, nowKey) !in readNotificationKeys
    }
    val done = todayTasks.count { it.done }
    val percent = if (todayTasks.isEmpty()) 0 else (done * 100 / todayTasks.size)
    val studyLeft = tasks.count { it.category == "study" && !it.done }
    val lifeLeft = tasks.count { it.category == "life" && !it.done }
    val today = SimpleDateFormat("EEEE, d 'tháng' M", Locale.forLanguageTag("vi")).format(Date())
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(topAfterStatusBar(60.dp)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(PaleBlue).semantics { contentDescription = "Hồ sơ của bạn" }.clickable(onClick = onProfile), contentAlignment = Alignment.Center) {
                Text(displayName?.trim()?.split(Regex("\\s+"))?.takeLast(2)?.mapNotNull { it.firstOrNull()?.uppercase() }?.joinToString("") ?: "K", color = if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF9DB5FF) else Color(0xFF1F4FD1), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(today.replaceFirstChar { it.uppercase(Locale.forLanguageTag("vi")) }, color = Muted, fontSize = 14.sp, lineHeight = 18.sp)
                val shortName = displayName?.trim()?.split(Regex("\\s+"))?.takeLast(2)?.joinToString(" ")?.takeIf { it.isNotEmpty() }
                Text("Chào ${shortName ?: "bạn"}!", fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.3).sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            val bellSurface = MaterialTheme.colorScheme.surface
            Box(Modifier.size(44.dp).clip(CircleShape).background(bellSurface)
                .clickable(role = Role.Button, onClick = onNotifications), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_figma_bell), "Thông báo", tint = Ink, modifier = Modifier.size(22.dp))
                if (hasUpcomingReminder) {
                    val badge = RoundedCornerShape(4.dp)
                    Box(Modifier.align(Alignment.TopEnd).offset(x = (-11).dp, y = 10.dp).size(12.dp)
                        .background(Color(0xFFE5484D), badge).border(2.dp, bellSurface, badge))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth().heightIn(min = 121.dp), colors = CardDefaults.cardColors(containerColor = Blue), shape = RoundedCornerShape(22.dp)) {
            Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Tiến độ hôm nay", color = Color.White.copy(alpha = .9f), fontSize = 14.sp, lineHeight = 18.sp)
                    Text("$done/${todayTasks.size} việc", color = Color.White, fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.Bold)
                    Text(when { percent == 100 -> "Xong hết rồi, nghỉ ngơi thôi!"; percent >= 50 -> "Đã hơn nửa chặng, cố lên!"; else -> "Bắt đầu từ việc nhỏ nhất nhé." }, color = Color.White.copy(alpha = .9f), fontSize = 14.sp, lineHeight = 18.sp)
                }
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                    Canvas(Modifier.fillMaxSize()) {
                        val thickness = 8.dp.toPx()
                        val corner = Offset(thickness / 2, thickness / 2)
                        val diameter = Size(size.width - thickness, size.height - thickness)
                        val stroke = Stroke(thickness)
                        drawArc(Color.White.copy(alpha = .28f), -90f, 360f, false, corner, diameter, style = stroke)
                        drawArc(Color.White, -90f, percent * 3.6f, false, corner, diameter, style = stroke)
                    }
                    Text("$percent%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            CategorySummary("Học tập", studyLeft, R.drawable.ic_figma_study_category, PaleBlue, Modifier.weight(1f).clickable { onCategory("Học tập") })
            CategorySummary("Cuộc sống", lifeLeft, R.drawable.ic_figma_home_category, if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF3D2614) else Color(0xFFFFEBD9), Modifier.weight(1f).clickable { onCategory("Cuộc sống") })
        }
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Việc hôm nay", fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = Ink)
            Text(if (loading) "Đang tải…" else "Xem tất cả", Modifier.clickable(role = Role.Button, onClick = onRefresh), color = LinkBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        if (loading && tasks.isEmpty()) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp), color = Blue)
        }
        Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            todayTasks.forEach { task -> HomeTaskRow(task, onClick = { onTask(task.id) }, onDone = { onDone(task) }) }
        }
        if (todayTasks.isEmpty() && !loading) {
            Text("Hôm nay chưa có việc nào. Bấm + để thêm nhé.", color = Muted, modifier = Modifier.padding(vertical = 18.dp))
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CategorySummary(title: String, remaining: Int, icon: Int, background: Color, modifier: Modifier = Modifier) {
    Card(modifier.heightIn(min = 123.dp), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(background), contentAlignment = Alignment.Center) {
                val tint = if (MaterialTheme.colorScheme.background != Color.Black) Color.Unspecified
                    else if (icon == R.drawable.ic_figma_study_category) Color(0xFF9DB5FF) else Color(0xFFFFB27A)
                Icon(painterResource(icon), null, tint = tint, modifier = Modifier.size(20.dp))
            }
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 19.sp)
            Text("$remaining việc còn lại", color = Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HomeTaskRow(task: Task, onClick: () -> Unit, onDone: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.heightIn(min = 66.dp).padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TaskCompletionControl(task.done, onDone)
            Column(Modifier.weight(1f)) {
                Text(task.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (task.done) Color(0xFF8A8A8E) else Ink, fontSize = 15.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium, textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryTag(task.category)
                    Spacer(Modifier.width(7.dp))
                    Text(task.time, color = Muted, style = MaterialTheme.typography.labelSmall)
                }
            }
            Icon(painterResource(R.drawable.ic_figma_chevron_right), null, tint = Color(0xFFA1A1A6), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TaskListScreen(tasks: List<Task>, loading: Boolean, onTask: (Int) -> Unit, onDone: (Task) -> Unit, filter: String = "Tất cả", onFilter: (String) -> Unit = {}) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = tasks.filter { task ->
        (filter == "Tất cả" || (filter == "Học tập" && task.category == "study") || (filter == "Cuộc sống" && task.category == "life")) && task.title.contains(query, true)
    }
    val pending = filtered.filterNot { it.done }
    val completed = filtered.filter { it.done }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(topAfterStatusBar(60.dp)))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Danh sách", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("${tasks.count { !it.done }} việc chưa xong", color = Muted, fontSize = 14.sp)
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp)).background(SurfaceGray).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(R.drawable.ic_figma_search), null, tint = Muted, modifier = Modifier.size(18.dp))
            BasicTextField(
                query, { query = it }, modifier = Modifier.weight(1f).padding(start = 8.dp),
                singleLine = true, textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                decorationBox = { inner -> Box { if (query.isEmpty()) Text("Tìm công việc", color = Muted, style = MaterialTheme.typography.bodyMedium); inner() } }
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Tất cả", "Học tập", "Cuộc sống").forEach { label ->
                val selected = filter == label
                val shape = RoundedCornerShape(18.dp)
                Box(Modifier.height(36.dp).clip(shape).background(if (selected) Ink else MaterialTheme.colorScheme.surface)
                    .then(if (selected) Modifier else Modifier.border(1.dp, Color(0xFFDCDCE0), shape))
                    .selectable(selected = selected, role = Role.Tab, onClick = { onFilter(label) })
                    .padding(horizontal = 16.dp), contentAlignment = Alignment.Center) {
                    Text(label, color = if (selected) MaterialTheme.colorScheme.surface else Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (loading && tasks.isNotEmpty()) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = Blue)
        }
        if (loading && tasks.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue)
            }
        } else if (filtered.isEmpty()) {
            Text(
                "Không tìm thấy công việc nào.",
                color = Muted,
                modifier = Modifier.padding(top = 24.dp).weight(1f)
            )
        } else LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 12.dp)) {
            item { Text("CẦN LÀM", color = Muted, fontSize = 13.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)) }
            items(pending, key = { it.id }) { task -> TaskCard(task, onClick = { onTask(task.id) }, onDone = { onDone(task) }) }
            if (completed.isNotEmpty()) {
                item { Text("ĐÃ XONG", color = Muted, fontSize = 13.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)) }
                items(completed, key = { it.id }) { task -> TaskCard(task, onClick = { onTask(task.id) }, onDone = { onDone(task) }) }
            }
        }
    }
}

@Composable
private fun TaskCard(task: Task, onClick: () -> Unit, onDone: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.heightIn(min = if (task.done) 50.dp else 66.dp).padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TaskCompletionControl(task.done, onDone)
            Column(Modifier.weight(1f)) {
                Text(task.title, color = if (task.done) Color(0xFF8A8A8E) else Ink, fontSize = 15.sp, lineHeight = 19.sp,
                    fontWeight = if (task.done) FontWeight.Normal else FontWeight.Medium,
                    textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!task.done) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryTag(task.category)
                        Spacer(Modifier.width(8.dp))
                        Text("${formatTaskDate(task.date)}, ${task.time}", color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
            if (!task.done) PriorityBadge(task.priority)
        }
    }
}

@Composable
private fun TaskCompletionControl(done: Boolean, onToggle: () -> Unit) {
    Box(Modifier.size(26.dp).semantics { contentDescription = if (done) "Đánh dấu chưa hoàn thành" else "Đánh dấu hoàn thành" }
        .toggleable(value = done, role = Role.Checkbox, onValueChange = { onToggle() }), contentAlignment = Alignment.Center) {
        if (done) {
            Box(Modifier.size(26.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_figma_check), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        } else {
            Box(Modifier.size(26.dp).border(2.dp, Color(0xFFC7C7CC), CircleShape))
        }
    }
}

@Composable
private fun CategoryTag(category: String, expanded: Boolean = false) {
    val dark = MaterialTheme.colorScheme.background == Color.Black
    val study = category != "life"
    Text(if (study) "Học tập" else "Cuộc sống",
        Modifier.clip(RoundedCornerShape(if (expanded) 8.dp else 6.dp))
            .background(if (study) PaleBlue else if (dark) Color(0xFF3A2414) else Color(0xFFFFEBD9))
            .padding(horizontal = if (expanded) 10.dp else 8.dp, vertical = if (expanded) 4.dp else 2.dp),
        color = if (study) (if (dark) Color(0xFF9DB5FF) else Color(0xFF1F4FD1)) else (if (dark) Color(0xFFFFB16B) else Color(0xFFA24A08)),
        fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PriorityBadge(priority: String) {
    if (priority == "high") Text("Gấp", Modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFFFDE7E7)).padding(horizontal = 7.dp, vertical = 3.dp), color = Color(0xFFB42318), style = MaterialTheme.typography.labelSmall)
}

private fun formatTaskDate(date: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val value = runCatching { parser.parse(date) }.getOrNull() ?: return date
    val today = Calendar.getInstance()
    val taskDay = Calendar.getInstance().apply { time = value }
    fun dayKey(c: Calendar) = parser.format(c.time)
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val sunday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, (Calendar.SUNDAY - get(Calendar.DAY_OF_WEEK) + 7) % 7) }
    return when {
        dayKey(taskDay) == dayKey(today) -> "Hôm nay"
        dayKey(taskDay) == dayKey(tomorrow) -> "Ngày mai"
        taskDay.after(today) && dayKey(taskDay) <= dayKey(sunday) -> "Tuần này"
        else -> SimpleDateFormat("EEE, dd/MM", Locale.forLanguageTag("vi")).format(value)
    }
}

@Composable
private fun DetailScreen(
    task: Task,
    backLabel: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDone: (Task) -> Unit,
    onAddSubtask: (Task, String) -> Unit,
    onToggleSubtask: (Task, Int) -> Unit,
    onEditSubtask: (Task, Int, String) -> Unit,
    onDeleteSubtask: (Task, Int) -> Unit,
    onDelete: (Task) -> Unit
) {
    var confirmDelete by remember(task.id) { mutableStateOf(false) }
    var subtaskText by rememberSaveable(task.id) { mutableStateOf("") }
    var editingSubtask by remember(task.id) { mutableStateOf<Int?>(null) }
    var editedTitle by remember(task.id) { mutableStateOf("") }
    val completedCount = task.subtasks.count { it.done }
    val focus = LocalFocusManager.current

    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(top = topAfterStatusBar(56.dp))
    ) {
        Row(Modifier.fillMaxWidth().height(44.dp).padding(start = 18.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(
                Modifier.height(44.dp).clickable(role = Role.Button, onClick = onBack),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_figma_back), null, tint = Blue, modifier = Modifier.size(22.dp))
                Text(backLabel, color = Blue, fontSize = 16.sp, letterSpacing = 0.sp)
            }
            Spacer(Modifier.weight(1f))
            Box(Modifier.height(44.dp).clickable(role = Role.Button, onClick = onEdit), contentAlignment = Alignment.Center) {
                Text("Sửa", color = Blue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                CategoryTag(task.category, expanded = true)
                PriorityTag(task.priority)
            }
            Spacer(Modifier.height(10.dp))
            Text(
                task.title, color = Ink, fontSize = 26.sp, lineHeight = 34.sp,
                letterSpacing = (-0.3).sp, fontWeight = FontWeight.Bold,
                textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None
            )
            Spacer(Modifier.height(16.dp))
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    DetailValueRow(R.drawable.ic_figma_calendar, "Hạn chót", "${formatTaskDate(task.date)}, ${task.time}")
                    HorizontalDivider(color = RowDivider)
                    DetailValueRow(R.drawable.ic_figma_reminder, "Nhắc nhở", if (task.remind) "Trước 15 phút" else "Tắt")
                    HorizontalDivider(color = RowDivider)
                    DetailValueRow(R.drawable.ic_figma_repeat, "Lặp lại", "Không")
                }
            }
            if (task.note.isNotBlank()) {
                Text("Ghi chú", Modifier.padding(top = 16.dp, bottom = 8.dp), color = Muted, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    val noteColor = if (MaterialTheme.colorScheme.background == Color.Black) Ink else Color(0xFF3A3A3C)
                    Text(task.note, Modifier.padding(horizontal = 16.dp, vertical = 14.dp), color = noteColor, fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Việc nhỏ", Modifier.weight(1f), color = Muted, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                Text("$completedCount/${task.subtasks.size}", color = Muted, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            if (task.subtasks.isNotEmpty()) {
                val track = if (MaterialTheme.colorScheme.background == Color.Black) SurfaceGray else Color(0xFFE5E5EA)
                Box(Modifier.fillMaxWidth().padding(top = 8.dp).height(6.dp).clip(CircleShape).background(track)) {
                    Box(Modifier.fillMaxWidth(completedCount.toFloat() / task.subtasks.size).fillMaxHeight().clip(CircleShape).background(Blue))
                }
            } else {
                Text("Chưa có việc nhỏ nào. Chia nhỏ để dễ bắt đầu hơn.", color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(12.dp))
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    task.subtasks.forEachIndexed { index, subtask ->
                        DetailSubtaskRow(
                            subtask,
                            onToggle = { onToggleSubtask(task, index) },
                            onEdit = { editingSubtask = index; editedTitle = subtask.title },
                            onDelete = { onDeleteSubtask(task, index) }
                        )
                        HorizontalDivider(color = RowDivider)
                    }
                    Row(
                        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 16.dp, end = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(painterResource(R.drawable.ic_figma_subtask_add), null, tint = Blue, modifier = Modifier.size(20.dp))
                        BasicTextField(
                            value = subtaskText,
                            onValueChange = { subtaskText = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
                            cursorBrush = SolidColor(Blue),
                            decorationBox = { inner ->
                                Box(Modifier.heightIn(min = 38.dp).padding(horizontal = 2.dp), contentAlignment = Alignment.CenterStart) {
                                    if (subtaskText.isEmpty()) Text("Thêm việc nhỏ", color = Color(0xFF757575), fontSize = 15.sp, letterSpacing = 0.sp)
                                    inner()
                                }
                            }
                        )
                        val canAdd = subtaskText.isNotBlank()
                        Box(
                            Modifier.height(44.dp).clickable(enabled = canAdd, role = Role.Button) {
                                onAddSubtask(task, subtaskText.trim())
                                subtaskText = ""
                                focus.clearFocus()
                            }.padding(horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Thêm", color = if (canAdd) Blue else Color(0xFF8A8A8E), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 15.dp, end = 25.dp, top = 42.dp, bottom = 12.dp)) {
            Button(
                onClick = { onDone(task) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Color.White)
            ) {
                Text(if (task.done) "Đánh dấu chưa hoàn thành" else "Đánh dấu hoàn thành", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(4.dp))
            Box(
                Modifier.fillMaxWidth().height(44.dp).clickable(role = Role.Button) { confirmDelete = true },
                contentAlignment = Alignment.Center
            ) {
                Text("Xóa công việc", color = Color(0xFFC4321F), fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
    if (confirmDelete) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Xóa việc này?") }, text = { Text("Không thể hoàn tác.") },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete(task) }) { Text("Xóa") } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Hủy") } }
    )
    editingSubtask?.let { index ->
        AlertDialog(
            onDismissRequest = { editingSubtask = null },
            title = { Text("Sửa việc nhỏ") },
            text = { OutlinedTextField(editedTitle, { editedTitle = it }, label = { Text("Tên việc") }, singleLine = true) },
            confirmButton = {
                TextButton(enabled = editedTitle.isNotBlank(), onClick = {
                    onEditSubtask(task, index, editedTitle.trim())
                    editingSubtask = null
                }) { Text("Lưu") }
            },
            dismissButton = { TextButton(onClick = { editingSubtask = null }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun DetailSubtaskRow(subtask: Subtask, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(start = 11.dp, end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(width = 32.dp, height = 44.dp)
                .semantics { contentDescription = subtask.title }
                .toggleable(value = subtask.done, role = Role.Checkbox, onValueChange = { onToggle() }),
            contentAlignment = Alignment.Center
        ) {
            val shape = RoundedCornerShape(6.dp)
            Box(
                Modifier.size(22.dp).then(
                    if (subtask.done) Modifier.background(Blue, shape)
                    else Modifier.border(2.dp, Color(0xFFC7C7CC), shape)
                ),
                contentAlignment = Alignment.Center
            ) {
                if (subtask.done) Icon(painterResource(R.drawable.ic_figma_subtask_check), null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }
        Text(
            subtask.title, Modifier.weight(1f), fontSize = 15.sp, lineHeight = 19.sp, letterSpacing = 0.sp,
            color = if (subtask.done) Color(0xFF8A8A8E) else Ink,
            textDecoration = if (subtask.done) TextDecoration.LineThrough else TextDecoration.None,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        DetailIconAction(R.drawable.ic_figma_edit, "Sửa việc nhỏ", Muted, onEdit)
        DetailIconAction(R.drawable.ic_figma_delete, "Xóa việc nhỏ", Color(0xFFC4321F), onDelete)
    }
}

@Composable
private fun DetailIconAction(icon: Int, description: String, tint: Color, onClick: () -> Unit) {
    Box(
        Modifier.size(width = 40.dp, height = 44.dp).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), description, tint = tint, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun DetailValueRow(icon: Int, label: String, value: String) {
    val labelColor = if (MaterialTheme.colorScheme.background == Color.Black) Muted else Color(0xFF5C5C62)
    Row(Modifier.fillMaxWidth().heightIn(min = 50.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = Muted, modifier = Modifier.size(20.dp))
        Text(label, Modifier.weight(1f).padding(start = 12.dp), color = labelColor, fontSize = 15.sp, letterSpacing = 0.sp)
        Text(value, color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.sp)
    }
}

@Composable
private fun AddTaskScreen(initialTask: Task? = null, loading: Boolean = false, onBack: () -> Unit, onSave: (CreateTaskRequest) -> Unit) {
    var title by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.title.orEmpty()) }
    var note by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.note.orEmpty()) }
    var category by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.category ?: "study") }
    var date by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var time by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.time ?: "19:00") }
    var priority by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.priority ?: "medium") }
    var remind by rememberSaveable(initialTask?.id) { mutableStateOf(initialTask?.remind ?: true) }
    var error by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) }.getOrNull()?.let { calendar.time = it }
    val dateText = runCatching { SimpleDateFormat("EEEE, dd/MM", Locale.forLanguageTag("vi")).format(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date) ?: Date()) }.getOrDefault(date)
    Column(Modifier.fillMaxSize().imePadding().padding(start = 20.dp, end = 20.dp, top = topAfterStatusBar(56.dp))) {
        Box(Modifier.fillMaxWidth().height(44.dp)) {
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).offset(x = (-12).dp), contentPadding = PaddingValues(0.dp)) {
                Text("Hủy", color = Blue, fontSize = 16.sp)
            }
            Text(if (initialTask == null) "Việc mới" else "Sửa việc", Modifier.align(Alignment.Center), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            TextButton(enabled = title.isNotBlank() && !loading, onClick = { if (title.isBlank()) error = true else onSave(CreateTaskRequest(title.trim(), note.trim(), category, date, time, priority, remind)) },
                modifier = Modifier.align(Alignment.CenterEnd).offset(x = 12.dp), contentPadding = PaddingValues(0.dp)) {
                Text("Lưu", color = if (title.isNotBlank() && !loading) Blue else Muted, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 12.dp)) {
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Tên công việc", color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
                        BasicTextField(title, { title = it; error = false }, modifier = Modifier.fillMaxWidth().heightIn(min = 21.dp), singleLine = true, textStyle = TextStyle(color = Ink, fontFamily = MaterialTheme.typography.bodyMedium.fontFamily, fontSize = 17.sp, fontWeight = FontWeight.Medium), decorationBox = { inner -> Box { if (title.isEmpty()) Text("Ôn chương 4 – Giải thuật", color = Muted, fontSize = 17.sp, lineHeight = 21.sp); inner() } })
                    }
                    HorizontalDivider(color = RowDivider)
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Ghi chú", color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
                        BasicTextField(note, { note = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 38.dp), textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink), decorationBox = { inner -> Box { if (note.isEmpty()) Text("Thêm ghi chú (không bắt buộc)", color = Muted, fontSize = 15.sp, lineHeight = 19.sp); inner() } })
                    }
                }
            }
            if (error) Text("Vui lòng nhập tên công việc.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 5.dp))

            FormSectionLabel("Nhóm")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("study" to "Học tập", "life" to "Cuộc sống").forEach { (value, label) ->
                    val selected = category == value
                    OutlinedButton(
                        onClick = { category = value }, modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Blue else Color(0xFFDCDCE0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selected) PaleBlue else MaterialTheme.colorScheme.surface, contentColor = if (selected) Blue else Ink)
                    ) { Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                }
            }

            val timeParts = time.split(":").mapNotNull { it.toIntOrNull() }
            Spacer(Modifier.height(18.dp))
            Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    FormValueRow(R.drawable.ic_figma_calendar, "Ngày", dateText) {
                        DatePickerDialog(context, { _, year, month, day -> date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day) }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }
                    HorizontalDivider(color = RowDivider)
                    FormValueRow(R.drawable.ic_figma_clock, "Giờ", time) {
                        TimePickerDialog(context, { _, hour, minute -> time = String.format(Locale.US, "%02d:%02d", hour, minute) }, timeParts.getOrElse(0) { 9 }, timeParts.getOrElse(1) { 0 }, true).show()
                    }
                    HorizontalDivider(color = RowDivider)
                    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_figma_reminder), null, tint = Muted, modifier = Modifier.size(20.dp))
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text("Nhắc tôi", color = Ink, fontSize = 15.sp)
                            Text("Trước 15 phút", color = Muted, style = MaterialTheme.typography.labelSmall)
                        }
                        SettingsToggle("Nhắc tôi", remind) { remind = it }
                    }
                }
            }

            FormSectionLabel("Mức ưu tiên")
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceGray).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("low" to "Thấp", "medium" to "Vừa", "high" to "Cao").forEach { (value, label) ->
                    val selected = priority == value
                    Box(Modifier.weight(1f).height(36.dp).clip(RoundedCornerShape(9.dp)).background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .selectable(selected, role = Role.Tab) { priority = value }, contentAlignment = Alignment.Center) {
                        Text(label, color = if (selected) Ink else Muted, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        Button(enabled = title.isNotBlank() && !loading, onClick = { if (title.isBlank()) error = true else onSave(CreateTaskRequest(title.trim(), note.trim(), category, date, time, priority, remind)) }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp).height(56.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
            Text(if (initialTask == null) "Lưu công việc" else "Lưu thay đổi", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun FormSectionLabel(text: String) {
    Text(text, color = Muted, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
}

@Composable
private fun FormValueRow(icon: Int, label: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = Muted, modifier = Modifier.size(20.dp))
        Text(label, Modifier.weight(1f).padding(start = 12.dp), color = Ink, fontSize = 15.sp)
        Text(value, Modifier.clip(RoundedCornerShape(8.dp)).background(SurfaceGray).padding(horizontal = 10.dp, vertical = 6.dp), color = Ink, fontSize = 14.sp)
    }
}

@Composable
private fun SettingsScreen(
    settings: TaskUiState,
    authState: AuthUiState,
    onTheme: (String) -> Unit,
    onAccent: (String) -> Unit,
    onFontScale: (Float) -> Unit,
    onReminders: (Boolean) -> Unit,
    onSound: (Boolean) -> Unit,
    onDailySummary: (Boolean) -> Unit,
    onNotifications: () -> Unit,
    onRequestNotifications: () -> Unit,
    onNotificationSettings: () -> Unit,
    onExactAlarmSettings: (() -> Unit)?,
    onProfile: () -> Unit,
    onOnboarding: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = authState as? AuthUiState.Success
    val context = LocalContext.current
    var deviceNotificationsEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var exactAlarmEnabled by remember { mutableStateOf(TaskReminder.canScheduleExactly(context)) }
    LifecycleResumeEffect(context) {
        deviceNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        exactAlarmEnabled = TaskReminder.canScheduleExactly(context)
        onPauseOrDispose { }
    }
    var showLanguageInfo by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(topAfterStatusBar(60.dp)))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Cài đặt", Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Avatar(user?.displayName ?: "Khách", size = 44.dp, onClick = onProfile)
        }
        SectionCaption("GIAO DIỆN", top = 14.dp)
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("light" to "Sáng", "dark" to "Tối", "auto" to "Tự động").forEach { (id, label) ->
                        Column(Modifier.weight(1f).clickable { onTheme(id) }, horizontalAlignment = Alignment.CenterHorizontally) {
                            ThemePreview(id, selected = settings.themeId == id)
                            Text(label, color = if (settings.themeId == id) Blue else Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
                HorizontalDivider(color = RowDivider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Màu chủ đạo", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TodoAccents.forEach { (id, color) ->
                            val selected = settings.accentId == id
                            Box(Modifier.size(44.dp).semantics { contentDescription = "Màu $id" }
                                .selectable(selected, role = Role.RadioButton) { onAccent(id) }, contentAlignment = Alignment.Center) {
                                if (selected) Box(Modifier.size(34.dp).border(2.dp, color, CircleShape))
                                Box(Modifier.size(26.dp).background(color, CircleShape))
                            }
                        }
                    }
                }
                HorizontalDivider(color = RowDivider)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cỡ chữ", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text("A", color = Muted, style = MaterialTheme.typography.labelSmall)
                    FontScaleControl(settings.fontScale, onFontScale)
                    Text("A", color = Muted, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        SectionCaption("THÔNG BÁO")
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column {
                SettingsSwitchRow("Nhắc việc", "Báo trước hạn chót 15 phút", settings.remindersEnabled) { enabled ->
                    if (enabled) onRequestNotifications()
                    onReminders(enabled)
                }
                HorizontalDivider(color = RowDivider)
                SettingsSwitchRow("Âm thanh", "Phát âm khi có thông báo", settings.notificationSound, onSound)
                HorizontalDivider(color = RowDivider)
                SettingsSwitchRow("Tổng kết cuối ngày", "Gửi lúc 21:00 mỗi tối", settings.dailySummary) { enabled ->
                    if (enabled) onRequestNotifications()
                    onDailySummary(enabled)
                }
            }
        }

        SectionCaption("CHUNG")
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column {
                SettingRow("Ngôn ngữ", "Tiếng Việt", onClick = { showLanguageInfo = true }, valueAtEnd = true)
                HorizontalDivider(color = RowDivider)
                SettingRow("Giới thiệu UTH Todo", "Phiên bản ${BuildConfig.VERSION_NAME}", onClick = onOnboarding, valueAtEnd = true)
            }
        }

        SectionCaption("THÔNG BÁO ANDROID")
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column {
                SettingRow("Thông báo trên điện thoại",
                    if (deviceNotificationsEnabled) "Cài đặt" else "Đang tắt",
                    onClick = onNotificationSettings)
                if (onExactAlarmSettings != null) {
                    HorizontalDivider(color = SurfaceGray)
                    SettingRow("Nhắc đúng giờ", if (exactAlarmEnabled) "Đã bật" else "Cấp quyền",
                        onClick = onExactAlarmSettings)
                }
                if (BuildConfig.DEBUG) {
                    HorizontalDivider(color = SurfaceGray)
                    PushTestControl()
                }
            }
        }

        SectionCaption("TÀI KHOẢN")
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            SettingRow("Hồ sơ", user?.email ?: "Khách · Chưa liên kết", onClick = onProfile)
            HorizontalDivider(color = SurfaceGray)
            SettingRow("Thông báo", "Danh sách lời nhắc", onClick = onNotifications)
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color(0xFFD64200))
        ) {
            Icon(painterResource(R.drawable.ic_figma_sign_out), null)
            Spacer(Modifier.width(8.dp))
            Text(if (user == null) "Thoát chế độ khách" else "Đăng xuất", fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(18.dp))
    }
    if (showLanguageInfo) AlertDialog(
        onDismissRequest = { showLanguageInfo = false },
        title = { Text("Ngôn ngữ") },
        text = { Text("Ứng dụng hiện hỗ trợ tiếng Việt.") },
        confirmButton = { TextButton(onClick = { showLanguageInfo = false }) { Text("Đóng") } }
    )
}

@Composable
private fun ThemePreview(theme: String, selected: Boolean) {
    val shape = RoundedCornerShape(14.dp)
    val light = Color(0xFFF5F5F7)
    val dark = Color.Black
    val split = theme == "auto"
    val darkPreview = theme == "dark"
    val panel = if (split) Brush.horizontalGradient(.5f to light, .5f to dark) else SolidColor(if (darkPreview) dark else light)
    Column(
        Modifier.fillMaxWidth().height(84.dp).clip(shape).background(panel)
            .border(2.dp, if (selected) Blue else RowDivider, shape).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.fillMaxWidth(.58f).height(8.dp).clip(CircleShape)
            .background(if (darkPreview) Color(0xFF48484A) else Color(0xFFD1D1D6)))
        val row = if (split) Brush.horizontalGradient(.5f to Color.White, .5f to Color(0xFF2C2C2E))
            else SolidColor(if (darkPreview) Color(0xFF2C2C2E) else Color.White)
        repeat(2) { Box(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(6.dp)).background(row)) }
    }
}

@Composable
private fun SectionCaption(text: String, top: Dp = 20.dp) {
    Text(text, color = Muted, fontSize = 13.sp, lineHeight = 16.sp, letterSpacing = .5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = top, bottom = 14.dp))
}

@Composable
private fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 60.dp).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium)
            Text(subtitle, color = Muted, style = MaterialTheme.typography.labelSmall)
        }
        SettingsToggle(title, checked, onChange)
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Box(
        Modifier.size(51.dp, 31.dp)
            .semantics { contentDescription = label }
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .background(if (checked) Color(0xFF34C759) else Color(0xFFD1D1D6), CircleShape).padding(2.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(Modifier.size(27.dp).shadow(2.dp, CircleShape).background(Color.White, CircleShape))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontScaleControl(value: Float, onChange: (Float) -> Unit) {
    Slider(
        value = value, onValueChange = onChange, valueRange = .85f..1.2f,
        modifier = Modifier.width(110.dp).height(28.dp).padding(horizontal = 10.dp)
            .semantics { contentDescription = "Cỡ chữ" },
        thumb = { Box(Modifier.size(18.dp).shadow(3.dp, CircleShape).background(Color.White, CircleShape)) },
        track = {
            Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(RowDivider)) {
                Box(Modifier.fillMaxWidth(((value - .85f) / .35f).coerceIn(0f, 1f)).fillMaxHeight().background(Blue))
            }
        }
    )
}

@Composable
private fun SettingRow(title: String, subtitle: String, onClick: () -> Unit, valueAtEnd: Boolean = false) {
    Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 15.sp)
            if (!valueAtEnd && subtitle.isNotBlank()) Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (valueAtEnd) Text(subtitle, Modifier.padding(start = 6.dp, end = 6.dp), color = Muted, fontSize = 15.sp)
        Icon(painterResource(R.drawable.ic_figma_profile_chevron), null, tint = Muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ProfileScreen(
    tasks: List<Task>,
    taskListLoadFailed: Boolean,
    syncStatus: String?,
    authState: AuthUiState,
    avatarUid: String?,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onUpdateName: (String) -> Unit,
    profileError: String?,
    onDismissProfileError: () -> Unit,
    onSync: () -> Unit,
    onSignOut: () -> Unit
) {
    val user = authState as? AuthUiState.Success
    val complete = tasks.count { it.done }
    var showEditName by rememberSaveable { mutableStateOf(false) }
    var showPrivacyInfo by remember { mutableStateOf(false) }
    var editedName by rememberSaveable(user?.displayName) { mutableStateOf(user?.displayName.orEmpty()) }
    Column(Modifier.fillMaxSize().padding(top = topAfterStatusBar(56.dp))) {
        Box(Modifier.fillMaxWidth().height(44.dp).padding(start = 18.dp, end = 20.dp)) {
            Row(
                Modifier.align(Alignment.CenterStart).height(44.dp).clickable(role = Role.Button, onClick = onBack),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(painterResource(R.drawable.ic_figma_back), null, tint = Blue, modifier = Modifier.size(22.dp))
                Text("Cài đặt", color = Blue, fontSize = 16.sp)
            }
            Text("Hồ sơ", Modifier.align(Alignment.Center), color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Box(Modifier.align(Alignment.CenterEnd).height(44.dp).clickable(role = Role.Button) {
                if (user == null) onLogin() else { editedName = user.displayName.orEmpty(); showEditName = true }
            }, contentAlignment = Alignment.Center) {
                Text("Sửa", color = Blue, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            key(avatarUid) {
                ProfileAvatar(user?.displayName ?: "Khách", avatarUid)
            }
            Text(user?.displayName ?: "Khách", Modifier.padding(top = 4.dp), color = Ink, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
            Text(user?.email ?: "Chưa liên kết tài khoản", color = Muted, fontSize = 14.sp, lineHeight = 18.sp)
            if (user != null) {
                Row(Modifier.background(Color(0xFFDDF5EA), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(R.drawable.ic_figma_linked), null, tint = Color(0xFF0F7A4F), modifier = Modifier.size(13.dp))
                    Text("Đã liên kết tài khoản Google", color = Color(0xFF0F7A4F), fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(if (taskListLoadFailed) "–" else "$complete", "Việc đã xong", Modifier.weight(1f))
            StatCard("–", "Chuỗi liên tiếp", Modifier.weight(1f))
            StatCard("–", "Đúng hạn", Modifier.weight(1f))
        }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tuần này", Modifier.weight(1f), fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
                    Text("Minh họa", color = Muted, style = MaterialTheme.typography.labelSmall)
                }
                Row(Modifier.fillMaxWidth().height(107.dp).padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                    val bars = listOf(54, 72, 36, 72, 54, 18, 18)
                    val days = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    bars.forEachIndexed { index, count ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                            Box(Modifier.width(26.dp).height(count.dp).clip(RoundedCornerShape(6.dp)).background(if (index == (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7) Blue else Color(0xFFE5E5EA)))
                            Text(days[index], color = Muted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column {
                ProfileActionRow(R.drawable.ic_figma_profile_sync, "Đồng bộ dữ liệu", syncStatus ?: "Đồng bộ ngay", onSync)
                HorizontalDivider(color = RowDivider)
                ProfileActionRow(R.drawable.ic_figma_lock, "Quyền riêng tư", null) { showPrivacyInfo = true }
            }
        }
        if (user == null) {
            OutlinedButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Liên kết Google") }
        }
        }
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = Color(0xFFC4321F))
        ) {
            Icon(painterResource(R.drawable.ic_figma_sign_out), null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (user == null) "Thoát chế độ khách" else "Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    if (profileError != null) AlertDialog(
        onDismissRequest = onDismissProfileError,
        title = { Text("Không cập nhật được hồ sơ") },
        text = { Text(profileError) },
        confirmButton = { TextButton(onClick = onDismissProfileError) { Text("Đóng") } }
    )
    if (showPrivacyInfo) AlertDialog(
        onDismissRequest = { showPrivacyInfo = false },
        title = { Text("Quyền riêng tư") },
        text = { Text(
            if (user != null) "Công việc được đồng bộ qua Firestore theo tài khoản Google của bạn."
            else "Chế độ khách dùng dữ liệu mẫu TryApps. Đăng nhập Google để có danh sách công việc đồng bộ."
        ) },
        confirmButton = { TextButton(onClick = { showPrivacyInfo = false }) { Text("Đóng") } }
    )
    if (showEditName) {
        AlertDialog(
            onDismissRequest = { showEditName = false },
            title = { Text("Sửa tên hiển thị") },
            text = { OutlinedTextField(editedName, { editedName = it }, label = { Text("Tên") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { if (editedName.isNotBlank()) onUpdateName(editedName); showEditName = false }) { Text("Lưu") } },
            dismissButton = { TextButton(onClick = { showEditName = false }) { Text("Hủy") } }
        )
    }
}

@Composable
private fun PushTestControl() {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Sao chép") }
    SettingRow("Mã FCM kiểm thử", status, onClick = {
        if (!loading) {
            scope.launch {
                loading = true
                status = "Đang lấy mã…"
                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    val clip = ClipData.newPlainText("Mã FCM kiểm thử", token)
                    clip.description.extras = PersistableBundle().apply {
                        putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
                    clipboard.setClipEntry(ClipEntry(clip))
                    status = "Đã sao chép"
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    status = "Lấy mã thất bại"
                } finally {
                    loading = false
                }
            }
        }
    })
}

@Composable
private fun ProfileAvatar(name: String, uid: String?) {
    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    val canEdit = uid != null && AvatarStorage.isAvailable
    var picture by remember { mutableStateOf<Bitmap?>(null) }
    var busy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uid != null && uri != null && canEdit) {
            scope.launch {
                busy = true
                errorMessage = null
                try {
                    picture = AvatarStorage.upload(context, uid, uri)
                } catch (error: CancellationException) {
                    throw error
                } catch (_: Exception) {
                    errorMessage = "Không lưu được ảnh. Kiểm tra Storage Emulator rồi thử lại."
                } finally {
                    busy = false
                }
            }
        }
    }

    LaunchedEffect(uid, canEdit) {
        if (uid != null && canEdit) {
            busy = true
            try {
                picture = AvatarStorage.load(uid)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                errorMessage = "Không tải được ảnh từ Storage Emulator."
            } finally {
                busy = false
            }
        }
    }

    Box {
        val image = picture
        if (image == null) Avatar(name, size = 88.dp)
        else Image(image.asImageBitmap(), "Ảnh đại diện", Modifier.size(88.dp).clip(CircleShape), contentScale = ContentScale.Crop)
        Box(
            Modifier.align(Alignment.BottomEnd).offset(x = 4.dp, y = 4.dp).size(36.dp)
                .background(Blue, CircleShape).border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                .then(if (canEdit) Modifier.clickable(enabled = !busy, role = Role.Button) {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_figma_camera), if (canEdit) "Đổi ảnh đại diện" else null, tint = Color.White, modifier = Modifier.size(15.dp))
        }
    }
    if (busy) Text("Đang xử lý ảnh…", color = Muted, style = MaterialTheme.typography.labelSmall)
    errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall) }
}

@Composable
private fun ProfileActionRow(icon: Int, title: String, value: String?, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onClick).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(icon), null, tint = Muted, modifier = Modifier.size(20.dp))
        Text(title, Modifier.weight(1f).padding(start = 12.dp), color = Ink, fontSize = 15.sp)
        if (value != null) Text(value, color = Muted, style = MaterialTheme.typography.bodySmall)
        else Icon(painterResource(R.drawable.ic_figma_profile_chevron), null, tint = Muted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun Avatar(name: String, size: androidx.compose.ui.unit.Dp = 34.dp, onClick: (() -> Unit)? = null) {
    val initials = name.trim().split(Regex("\\s+")).mapNotNull { it.firstOrNull() }.takeLast(2).joinToString("").uppercase().ifBlank { "K" }
    Box(
        Modifier.size(size).clip(CircleShape).background(PaleBlue)
            .then(if (onClick != null) Modifier.semantics { contentDescription = "Hồ sơ của bạn" }.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(initials, color = if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF9DB5FF) else Color(0xFF1F4FD1),
            fontWeight = FontWeight.Bold, fontSize = if (size >= 88.dp) 32.sp else 16.sp)
    }
}

@Composable
private fun RowScope.BottomNavItem(label: String, iconRes: Int, selected: Boolean, onClick: () -> Unit) {
    Column(Modifier.weight(1f).height(50.dp).selectable(selected, role = Role.Tab, onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)) {
        Icon(painterResource(iconRes), null, tint = if (selected) LinkBlue else Muted, modifier = Modifier.size(24.dp))
        Text(label, color = if (selected) LinkBlue else Muted, fontSize = 11.sp, lineHeight = 14.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier.heightIn(min = 75.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, color = Ink, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp)
            Text(label, color = Muted, fontSize = 12.sp, lineHeight = 15.sp)
        }
    }
}

private fun notificationCategory(task: Task, now: String) = when {
    task.done -> "Đã hoàn thành"
    "${task.date} ${task.time}" < now -> "Đã quá hạn"
    else -> "Sắp đến hạn"
}

private fun notificationKey(task: Task, now: String): String {
    val base = "${task.id}:${task.date}:${task.time}"
    val category = notificationCategory(task, now)
    return if (category == "Sắp đến hạn") base else "$base:$category"
}

@Composable
private fun NotificationsScreen(tasks: List<Task>, readKeys: Set<String>, onMarkRead: (Set<String>) -> Unit) {
    val now = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())
    val reminders = tasks.filter { it.date.isNotBlank() && (it.done || it.remind || "${it.date} ${it.time}" < now) }
        .sortedWith(compareBy<Task> { it.date }.thenBy { it.time })
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = topAfterStatusBar(60.dp), bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min = 42.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Thông báo", Modifier.weight(1f), color = Ink, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (reminders.isNotEmpty()) {
                Box(
                    Modifier.heightIn(min = 42.dp).clickable(role = Role.Button) {
                        onMarkRead(reminders.map { notificationKey(it, now) }.toSet())
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đánh dấu đã đọc", color = Blue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (reminders.isEmpty()) {
            Text("Chưa có thông báo nào.", color = Muted, fontSize = 14.sp)
        } else {
            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.fillMaxWidth()) {
                    reminders.forEachIndexed { index, task ->
                        val key = notificationKey(task, now)
                        NotificationRow(task, notificationCategory(task, now), unread = key !in readKeys) { onMarkRead(setOf(key)) }
                        if (index != reminders.lastIndex) HorizontalDivider(color = RowDivider)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(task: Task, category: String, unread: Boolean, onClick: () -> Unit) {
    val dark = MaterialTheme.colorScheme.background == Color.Black
    val (icon, iconBackground, iconTint) = when (category) {
        "Đã quá hạn" -> Triple(R.drawable.ic_tryapps_overdue, if (dark) Color(0xFF42201D) else Color(0xFFFDE7E7), if (dark) Color(0xFFFFA79C) else Color(0xFFB42318))
        "Đã hoàn thành" -> Triple(R.drawable.ic_tryapps_completed, if (dark) Color(0xFF17382D) else Color(0xFFDDF5EA), if (dark) Color(0xFF8FE0B9) else Color(0xFF0F7A4F))
        else -> Triple(R.drawable.ic_tryapps_alarm, PaleBlue, if (dark) Color(0xFF9DB5FF) else Color(0xFF1F4FD1))
    }
    val title = if (task.done) "Làm tốt lắm!" else category
    val summary = when (category) {
        "Đã quá hạn" -> "${task.title} chưa xong."
        "Đã hoàn thành" -> "Bạn đã hoàn thành ${task.title}."
        else -> "${task.title} lúc ${task.time}."
    }
    val unreadBackground = if (dark) Color(0xFF182136) else Color(0xFFF5F8FF)
    Row(
        Modifier.fillMaxWidth().background(if (unread) unreadBackground else MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.size(40.dp).background(iconBackground, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                Text(title, Modifier.weight(1f), color = Ink, fontSize = 15.sp, lineHeight = 19.sp, fontWeight = if (unread) FontWeight.Bold else FontWeight.Medium)
                Text(if (formatTaskDate(task.date) == "Hôm nay") task.time else formatTaskDate(task.date), color = Muted, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Text(summary, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
        }
        if (unread) Box(Modifier.padding(top = 6.dp).size(8.dp).background(Blue, CircleShape).semantics { contentDescription = "Chưa đọc" })
    }
}

@Composable
private fun StartScreen(onContinue: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Blue).clickable(onClick = onContinue).padding(start = 28.dp, end = 28.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            val shape = RoundedCornerShape(27.dp)
            Icon(painterResource(R.drawable.ic_figma_start_logo), null, tint = Color.Unspecified,
                modifier = Modifier.size(116.dp).shadow(14.dp, shape).background(Color.White, shape))
            Spacer(Modifier.height(22.dp))
            Text(buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("UTH") }
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(" Todo") }
            }, color = Color.White, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-1).sp)
            Spacer(Modifier.height(8.dp))
            Text("Việc học, việc nhà — gọn gàng mỗi ngày", color = Color.White.copy(alpha = .9f), fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
        Box(Modifier.width(120.dp).height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = .28f))) {
            Box(Modifier.align(Alignment.CenterStart).width(48.dp).fillMaxHeight().clip(CircleShape).background(Color.White))
        }
        Text("Phiên bản ${BuildConfig.VERSION_NAME}", color = Color.White.copy(alpha = .9f), fontSize = 12.sp, modifier = Modifier.padding(top = 14.dp))
    }
}

@Composable
private fun BrandHeader(onSkip: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Blue),
            contentAlignment = Alignment.Center
        ) {
            Icon(painterResource(R.drawable.ic_figma_onboarding_logo), null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
        }
        Text("UTH", modifier = Modifier.padding(start = 10.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1C1C1E))
        Text(" Todo", color = Blue, fontWeight = FontWeight.Medium, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Box(Modifier.height(32.dp).clickable(onClick = onSkip), contentAlignment = Alignment.CenterEnd) {
            Text("Bỏ qua", color = Color(0xFF6B6B70), fontSize = 14.sp)
        }
    }
}

@Composable
private fun OnboardingScreen(onContinue: () -> Unit, onSkip: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Mọi việc, gọn trong một chỗ", "Chia theo Học tập và Cuộc sống", "Nhắc đúng lúc")
    val descriptions = listOf(
        "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
        "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì.",
        "Bật thông báo để UTH Todo nhắc bạn trước hạn chót."
    )
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.White)) {
      val minHeight = maxHeight.coerceAtLeast(720.dp)
      Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = minHeight)
            .padding(start = 28.dp, end = 28.dp, top = topAfterStatusBar(60.dp), bottom = 16.dp)
      ) {
        BrandHeader(onSkip)
        Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            OnboardingIllustration(step)
        }
        Column(Modifier.heightIn(min = 150.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(titles[step], color = Color(0xFF1C1C1E), fontSize = 28.sp, lineHeight = 36.sp,
                letterSpacing = (-0.4).sp, fontWeight = FontWeight.Bold)
            Text(descriptions[step], color = Color(0xFF5C5C62), fontSize = 16.sp, lineHeight = 24.sp)
        }
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            repeat(3) { index ->
                Box(
                    Modifier.width(if (index == step) 24.dp else 8.dp).height(8.dp)
                        .clip(CircleShape)
                        .background(if (index == step) Blue else Color(0xFFD1D1D6))
                )
            }
        }
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = { if (step < 2) step++ else onContinue() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Blue),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(if (step == 2) "Bắt đầu ngay" else "Tiếp tục", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
      }
    }
}

@Composable
private fun OnboardingIllustration(step: Int, modifier: Modifier = Modifier) {
    val illustrationBlue = Color(0xFFEAF0FD)
    Box(
        Modifier.then(modifier).size(width = 220.dp, height = 260.dp)
            .clip(RoundedCornerShape(28.dp)).background(illustrationBlue),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            0 -> Column(
                Modifier.width(200.dp).offset(y = 7.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(3) { index ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (index == 0) {
                                Box(Modifier.size(22.dp).clip(CircleShape).background(Blue), contentAlignment = Alignment.Center) {
                                    Icon(painterResource(R.drawable.ic_figma_subtask_check), null, tint = Color.White, modifier = Modifier.size(12.dp))
                                }
                            } else {
                                Box(Modifier.size(22.dp).border(2.dp, Color(0xFFC7C7CC), CircleShape))
                            }
                            Spacer(Modifier.width(12.dp))
                            Box(
                                Modifier.width(listOf(110.dp, 90.dp, 120.dp)[index]).height(8.dp)
                                    .clip(CircleShape).background(Color(0xFFE4E4EA))
                            )
                        }
                    }
                }
            }
            1 -> Column(
                Modifier.width(200.dp).offset(y = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                listOf("Học tập" to PaleBlue, "Cuộc sống" to Color(0xFFFFE9D9)).forEach { (label, color) ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(71.dp),
                        shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 3.dp
                    ) {
                        Column(Modifier.padding(start = 16.dp, top = 16.dp)) {
                            Text(
                                label,
                                Modifier.clip(RoundedCornerShape(8.dp)).background(color)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                color = if (label == "Học tập") Blue else Color(0xFFD64200),
                                fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Box(Modifier.width(if (label == "Học tập") 140.dp else 110.dp).height(8.dp).clip(CircleShape).background(Color(0xFFE4E4EA)))
                        }
                    }
                }
            }
            else -> Column(Modifier.width(180.dp).offset(y = 11.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier.size(84.dp), shape = CircleShape, color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(painterResource(R.drawable.ic_figma_onboarding_bell), null, tint = Blue, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.height(18.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 4.dp
                ) {
                    Text("Ôn bài Toán lúc 19:00", Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), color = Color(0xFF1C1C1E), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onGuest: () -> Unit,
    loading: Boolean,
    errorMessage: String?
) {
    val textColor = Color(0xFF1C1C1E)
    val subtle = Color(0xFF6B6B70)
    BoxWithConstraints(Modifier.fillMaxSize().background(Color.White)) {
        val minHeight = maxHeight.coerceAtLeast(690.dp)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = minHeight)
                .padding(start = 28.dp, end = 28.dp, top = topAfterStatusBar(60.dp), bottom = 16.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-10).dp).size(44.dp)) {
                Icon(painterResource(R.drawable.ic_figma_back), "Quay lại", tint = textColor, modifier = Modifier.size(24.dp))
            }
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically)
            ) {
                val shape = RoundedCornerShape(26.dp)
                Icon(painterResource(R.drawable.ic_figma_login_logo), null, tint = Color.Unspecified,
                    modifier = Modifier.size(88.dp).shadow(12.dp, shape, ambientColor = Blue, spotColor = Blue).background(Blue, shape))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Chào mừng đến với\nUTH Todo", color = textColor, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = (-0.4).sp, fontWeight = FontWeight.Bold)
                    Text("Đăng nhập để lưu công việc và đồng bộ trên mọi thiết bị của bạn.", color = Color(0xFF5C5C62), fontSize = 16.sp, lineHeight = 24.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BenefitRow(R.drawable.ic_figma_sync, "Tự động sao lưu, không lo mất dữ liệu")
                    BenefitRow(R.drawable.ic_figma_phone, "Dùng trên iPhone, iPad và Mac")
                }
            }
            OutlinedButton(
                onClick = onContinue, enabled = !loading, modifier = Modifier.fillMaxWidth().height(58.dp),
                shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor), contentPadding = PaddingValues(0.dp)
            ) {
                if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                else Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(26.dp).border(1.dp, Color(0xFFC7C7CC), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Text("G", color = Color(0xFF5C5C62), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("Tiếp tục với Google", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                }
            }
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp)) }
            Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E5EA))
                Text("hoặc", Modifier.padding(horizontal = 12.dp), color = subtle, fontSize = 13.sp, lineHeight = 17.sp)
                HorizontalDivider(Modifier.weight(1f), color = Color(0xFFE5E5EA))
            }
            TextButton(onClick = onGuest, modifier = Modifier.fillMaxWidth().height(48.dp), contentPadding = PaddingValues(0.dp)) {
                Text("Dùng thử không cần tài khoản", color = Blue, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Text(buildAnnotatedString {
                val link = SpanStyle(color = Color(0xFF3A3A3C), textDecoration = TextDecoration.Underline)
                append("Khi tiếp tục, bạn đồng ý với ")
                withStyle(link) { append("Điều khoản sử dụng") }
                append(" và ")
                withStyle(link) { append("Chính sách quyền riêng tư") }
                append(" của UTH Todo.")
            }, Modifier.fillMaxWidth().padding(top = 20.dp), color = subtle, fontSize = 12.sp,
                lineHeight = 18.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun BenefitRow(iconRes: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(painterResource(iconRes), null, tint = Blue, modifier = Modifier.size(18.dp))
        Text(text, Modifier.padding(start = 10.dp), color = Color(0xFF3A3A3C), fontSize = 14.sp)
    }
}

@Composable
private fun PriorityTag(priority: String) {
    val label = when (priority) { "high" -> "Ưu tiên cao"; "low" -> "Ưu tiên thấp"; else -> "Ưu tiên vừa" }
    val dark = MaterialTheme.colorScheme.background == Color.Black
    val background = when (priority) {
        "high" -> if (dark) Color(0xFF42201D) else Color(0xFFFDE7E7)
        "low" -> if (dark) Color(0xFF17382D) else Color(0xFFDDF5EA)
        else -> SurfaceGray
    }
    val textColor = when (priority) {
        "high" -> if (dark) Color(0xFFFFA79C) else Color(0xFFB42318)
        "low" -> if (dark) Color(0xFF8FE0B9) else Color(0xFF0F7A4F)
        else -> Muted
    }
    Text(label, Modifier.clip(RoundedCornerShape(8.dp)).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
        color = textColor, fontSize = 12.sp, lineHeight = 15.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun EmptyDetail(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Không tìm thấy công việc", color = Muted)
        TextButton(onClick = onBack) { Text("Quay lại") }
    }
}
