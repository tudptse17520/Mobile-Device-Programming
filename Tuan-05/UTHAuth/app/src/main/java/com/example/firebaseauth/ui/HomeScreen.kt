package com.example.firebaseauth.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.firebaseauth.R
import com.example.firebaseauth.data.*
import com.example.firebaseauth.ui.theme.*
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TasksScreen(
    user: Account, state: TasksState,
    onSave: (TaskItem) -> Unit, onToggle: (TaskItem) -> Unit, onDelete: (TaskItem) -> Unit,
    onSamples: () -> Unit, onConfigure: (String) -> Unit, onFetch: () -> Unit,
    onDismiss: () -> Unit, onFilter: (String) -> Unit, onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf("Tất cả") }
    var query by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf("Tất cả") }
    var editing by remember { mutableStateOf<TaskItem?>(null) }
    var deleting by remember { mutableStateOf<TaskItem?>(null) }
    var url by remember(state.databaseUrl) { mutableStateOf(state.databaseUrl) }
    val context = LocalContext.current
    BackHandler(tab != 0) { tab = 0 }
    val todaysTasks = state.tasks.filter { it.date == today() }
    val visible = (if (tab == 0) todaysTasks else state.tasks).filter {
        (tab == 0 || category == "Tất cả" || it.category == category) &&
        (tab == 0 || it.title.contains(query, ignoreCase = true)) &&
        (tab == 0 || status == "Tất cả" || it.completed == (status == "Đã xong"))
    }
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.error) { state.error?.let { snackbar.showSnackbar(it); onDismiss() } }
    Scaffold(
        modifier = modifier.imePadding(), containerColor = UthBg,
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { UthBottomBar(tab, { tab = it }, { editing = TaskItem() }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (tab == 0) {
                item { HomeHeader(user.displayName ?: if (user.isAnonymous) "bạn" else user.email?.substringBefore("@") ?: "bạn",
                    user.photoUrl, { tab = 3 }, { tab = 2 }) }
                item {
                    val progress = state.tasks.progress(today())
                    ProgressHero(progress, when {
                        progress.total > 0 && progress.done == progress.total -> state.quoteDone
                        progress.fraction >= .5f -> state.quoteHalf
                        else -> state.quoteStart
                    })
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Học tập", "Cuộc sống").forEach { label ->
                            CategoryTile(label, todaysTasks.count { it.category == label && !it.completed },
                                { category = label; tab = 1; onFilter(label) }, Modifier.weight(1f))
                        }
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Việc hôm nay", fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        TextButton(onClick = { tab = 1; category = "Tất cả"; status = "Tất cả"; query = "" }) { Text("Xem tất cả") }
                    }
                }
            } else {
                item { Text(listOf("Trang chủ", "Danh sách", "Thông báo", "Cài đặt")[tab],
                    style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
            }
            if (tab == 0 || tab == 1) {
                if (tab == 1) {
                    item { OutlinedTextField(query, { query = it }, label = { Text("Tìm công việc") }, modifier = Modifier.fillMaxWidth(), singleLine = true) }
                    item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tất cả", "Học tập", "Cuộc sống").forEach { label ->
                            FilterChip(category == label, { category = label; onFilter(label) }, { Text(label) })
                        }
                    } }
                    item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Tất cả", "Chưa xong", "Đã xong").forEach { label ->
                            FilterChip(status == label, { status = label }, { Text(label) })
                        }
                    } }
                }
                item {
                    Text(when {
                        state.databaseUrl.isBlank() -> "Chưa cấu hình database. Mở Cài đặt để kết nối."
                        state.loading -> "Đang tải công việc…"
                        state.connected -> "Đã kết nối • Đồng bộ thời gian thực"
                        else -> "Chưa kết nối • Thay đổi được lưu trên máy và chờ đồng bộ"
                    }, fontSize = 12.sp, color = UthInkSoft)
                }
                if (visible.isEmpty()) item {
                    Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp)) {
                            Text("Chưa có công việc phù hợp", fontWeight = FontWeight.Bold)
                            Text("Nhấn + để thêm công việc, hoặc tạo dữ liệu mẫu trong Cài đặt.", color = UthInkSoft)
                        }
                    }
                }
                items(visible, key = { it.id }) { task ->
                    TaskRow(task, { onToggle(task) }, { editing = task }, showDate = tab == 1)
                }
            }
            if (tab == 2) {
                item { InfoCard("Hoạt động hôm nay", "Bạn đã hoàn thành ${todaysTasks.count { it.completed }}/${todaysTasks.size} công việc.") }
                item { InfoCard("Thông báo", "Phiên bản này chưa đăng ký FCM. Các công việc đến hạn vẫn hiển thị trong Danh sách; chưa có nhắc việc hệ thống.") }
            }
            if (tab == 3) {
                item { InfoCard(user.displayName ?: "Tài khoản UTH", user.email ?: "Tài khoản khách Firebase. Đăng xuất có thể làm bạn mất quyền truy cập dữ liệu khách.") }
                item { Text("Kết nối Firebase", fontWeight = FontWeight.Bold) }
                item { InfoCard("Authentication", "Đã đăng nhập • ${if (user.isAnonymous) "Anonymous" else user.provider}") }
                item {
                    OutlinedTextField(url, { url = it }, label = { Text("URL Realtime Database") },
                        placeholder = { Text("https://…firebasedatabase.app") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
                item { Button(onClick = { onConfigure(url) }, modifier = Modifier.fillMaxWidth()) { Text("Lưu và kết nối") } }
                item { InfoCard("Realtime Database", if (state.connected) "Đã kết nối. Dữ liệu tách theo tài khoản." else "Chưa kết nối. Kiểm tra URL, mạng và Rules.") }
                item { OutlinedButton(onClick = onSamples, enabled = state.databaseUrl.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Thêm 4 công việc mẫu hôm nay") } }
                item { OutlinedButton(onClick = onFetch, modifier = Modifier.fillMaxWidth()) { Text("Tải lời động viên từ Remote Config") } }
                item { InfoCard("Analytics", "Đã tích hợp sự kiện đăng nhập, tạo, sửa, hoàn thành, xóa và lọc công việc.") }
                item { TextButton(onClick = onSignOut) { Text("Đăng xuất", color = MaterialTheme.colorScheme.error) } }
            }
        }
    }
    editing?.let { task ->
        TaskEditor(task, onDismiss = { editing = null }, onSave = { onSave(it); editing = null },
            onDelete = { deleting = task }, onShare = {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "${task.title}\n${task.category} • ${task.date} ${task.time}\n${task.note}")
                }, "Chia sẻ công việc"))
            })
    }
    deleting?.let { task ->
        AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Xóa công việc?") },
            text = { Text(task.title) },
            confirmButton = { TextButton(onClick = { onDelete(task); deleting = null; editing = null }) { Text("Xóa") } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Hủy") } })
    }
}

@Composable
private fun HomeHeader(name: String, photo: String?, onProfile: () -> Unit, onNotifications: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(UthBlueSoft).clickable(onClick = onProfile), contentAlignment = Alignment.Center) {
            Text(initialsOf(name), color = UthBlue, fontWeight = FontWeight.Bold)
            if (photo != null) AsyncImage(photo, "Hồ sơ của bạn", Modifier.fillMaxSize().clip(CircleShape),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(SimpleDateFormat("EEEE, d 'tháng' M", Locale.forLanguageTag("vi")).format(Date()), fontSize = 12.sp, color = UthInkSoft)
            Text("Chào ${name}!", fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 2)
        }
        IconButton(onClick = onNotifications, modifier = Modifier.background(Color.White, CircleShape)) {
            Icon(painterResource(R.drawable.ic_figma_bell), "Thông báo", Modifier.size(22.dp))
        }
    }
}

@Composable
fun ProgressHero(progress: Progress, quote: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(UthBlue, RoundedCornerShape(22.dp)).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Tiến độ hôm nay", color = Color.White.copy(alpha = .9f), fontSize = 13.sp)
            Text("${progress.done}/${progress.total} việc", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(quote, color = Color.White.copy(alpha = .9f), fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress.fraction }, modifier = Modifier.fillMaxSize(),
                color = Color.White, trackColor = Color.White.copy(alpha = .28f), strokeWidth = 7.dp)
            Text("${progress.percent}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun CategoryTile(label: String, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val study = label == "Học tập"
    Column(modifier.background(Color.White, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(36.dp).background(if (study) UthBlueSoft else UthOrangeSoft, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(painterResource(if (study) R.drawable.ic_graduation_cap else R.drawable.ic_figma_home_category),
                null, Modifier.size(20.dp), tint = if (study) UthBlue else UthOrange)
        }
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text("$count việc còn lại", fontSize = 12.sp, color = UthInkSoft)
    }
}

@Composable
fun TaskRow(task: TaskItem, onToggle: () -> Unit, onEdit: () -> Unit, modifier: Modifier = Modifier, showDate: Boolean = false) {
    Row(modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(horizontal = 6.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).semantics {
            contentDescription = "Hoàn thành: ${task.title}"
            role = Role.Checkbox
            toggleableState = if (task.completed) ToggleableState.On else ToggleableState.Off
        }.clickable(onClick = onToggle), contentAlignment = Alignment.Center) {
            Box(Modifier.size(26.dp).clip(CircleShape)
                .background(if (task.completed) UthBlue else Color.Transparent)
                .border(2.dp, if (task.completed) UthBlue else UthLine, CircleShape), contentAlignment = Alignment.Center) {
                if (task.completed) Icon(painterResource(R.drawable.ic_figma_check), null, Modifier.size(14.dp), tint = Color.White)
            }
        }
        Column(Modifier.weight(1f).clickable(onClick = onEdit).padding(vertical = 4.dp)) {
            Text(task.title, fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (task.completed) UthInkFaint else UthInk,
                textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(task.category, Modifier.background(if (task.category == "Học tập") UthBlueSoft else UthOrangeSoft,
                    RoundedCornerShape(6.dp)).padding(horizontal = 7.dp, vertical = 2.dp),
                    color = if (task.category == "Học tập") UthBlue else UthOrange, fontSize = 11.sp)
                Text(task.time, fontSize = 12.sp, color = UthInkSoft)
            }
            if (showDate) Text(task.date, fontSize = 11.sp, color = UthInkSoft)
        }
        IconButton(onClick = onEdit) { Icon(painterResource(R.drawable.ic_figma_chevron_right), "Sửa ${task.title}", Modifier.size(18.dp), tint = UthInkFaint) }
    }
}

@Composable
private fun UthBottomBar(tab: Int, onTab: (Int) -> Unit, onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically) {
        val icons = listOf(R.drawable.ic_figma_home, R.drawable.ic_figma_list, R.drawable.ic_figma_notifications, R.drawable.ic_figma_settings)
        val labels = listOf("Trang chủ", "Danh sách", "Thông báo", "Cài đặt")
        for (i in 0..4) {
            if (i == 2) {
                FloatingActionButton(onClick = onAdd, containerColor = UthBlue, contentColor = Color.White, shape = CircleShape) {
                    Icon(painterResource(R.drawable.ic_figma_add), "Thêm công việc", Modifier.size(24.dp))
                }
            } else {
                val index = if (i < 2) i else i - 1
                Column(Modifier.weight(1f).heightIn(min = 56.dp).clickable { onTab(index) },
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(painterResource(icons[index]), null, Modifier.size(23.dp), tint = if (tab == index) UthBlue else UthInkSoft)
                    Text(labels[index], fontSize = 10.sp, color = if (tab == index) UthBlue else UthInkSoft)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(body, color = UthInkSoft, fontSize = 13.sp)
    }
}

internal fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when (parts.size) {
        0 -> "UT"
        1 -> parts[0].take(2).uppercase()
        else -> (parts.first().first().toString() + parts.last().first()).uppercase()
    }
}

@Preview(name = "Tiến độ hôm nay", showBackground = true)
@Composable
private fun ProgressPreview() {
    FirebaseAuthTheme { ProgressHero(Progress(2, 4), "Đã hơn nửa chặng, cố lên!") }
}

@Preview(name = "UTHAuth — Trang chủ", showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    FirebaseAuthTheme {
        TasksScreen(
            user = Account(displayName = "Minh Anh"),
            state = TasksState(tasks = sampleTasks(), connected = true, databaseUrl = "preview"),
            onSave = {}, onToggle = {}, onDelete = {}, onSamples = {}, onConfigure = {},
            onFetch = {}, onDismiss = {}, onFilter = {}, onSignOut = {}
        )
    }
}
