package com.example.firebaseauth.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firebaseauth.R
import com.example.firebaseauth.ui.theme.UthBg
import com.example.firebaseauth.ui.theme.UthBlue
import com.example.firebaseauth.ui.theme.UthBlueSoft
import com.example.firebaseauth.ui.theme.UthInk
import com.example.firebaseauth.ui.theme.UthInkFaint
import com.example.firebaseauth.ui.theme.UthInkSoft
import com.example.firebaseauth.ui.theme.UthLine
import com.example.firebaseauth.ui.theme.UthOrange
import com.example.firebaseauth.ui.theme.UthOrangeSoft
import com.example.firebaseauth.ui.theme.UthRed
import com.example.firebaseauth.ui.theme.UthRedSoft
import com.example.firebaseauth.ui.theme.UthRedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TaskTag(val label: String) {
    HocTap("Học tập"),
    CuocSong("Cuộc sống")
}

data class TodoTask(
    val title: String,
    val tag: TaskTag,
    val time: String,
    val dateLabel: String? = null,
    val done: Boolean = false,
    val overdue: Boolean = false
)

private val homeTasks = listOf(
    TodoTask("Nộp bài tập Lập trình di động", TaskTag.HocTap, "09:00", done = true),
    TodoTask("Ôn chương 3 – Cấu trúc dữ liệu", TaskTag.HocTap, "14:00"),
    TodoTask("Đi chợ mua đồ cho tuần", TaskTag.CuocSong, "17:30"),
    TodoTask("Tập thể dục 30 phút", TaskTag.CuocSong, "06:30", done = true)
)

private val listNeedTasks = listOf(
    TodoTask("Nộp bài tập Lập trình di động", TaskTag.HocTap, "09:00", dateLabel = "Hôm nay", overdue = true),
    TodoTask("Ôn chương 3 – Cấu trúc dữ liệu", TaskTag.HocTap, "14:00", dateLabel = "Hôm nay"),
    TodoTask("Đi chợ mua đồ cho tuần", TaskTag.CuocSong, "17:30", dateLabel = "Hôm nay"),
    TodoTask("Họp nhóm đồ án", TaskTag.HocTap, "20:00", dateLabel = "Thứ Năm")
)

private val listDoneTasks = listOf(
    TodoTask("Tập thể dục 30 phút", TaskTag.CuocSong, "06:30", done = true),
    TodoTask("Đọc 20 trang sách", TaskTag.HocTap, "22:00", done = true)
)

@Composable
fun HomeScreen(
    displayName: String?,
    photoUrl: String?,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    BackHandler(enabled = selectedTab != 0) { selectedTab = 0 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UthBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (selectedTab == 0) {
                HomeHeader(
                    displayName = displayName,
                    photoUrl = photoUrl,
                    onClickUser = onOpenProfile
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (selectedTab == 1) Modifier.statusBarsPadding().padding(top = 20.dp) else Modifier)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp)
            ) {
                if (selectedTab == 0) {
                    TodayContent()
                } else {
                    ListContent()
                }
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        BottomNavBar(
            selectedIndex = selectedTab,
            onSelect = { tab ->
                when (tab) {
                    0, 1 -> selectedTab = tab
                    4 -> onOpenProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding()
        )
    }
}

@Composable
private fun HomeHeader(
    displayName: String?,
    photoUrl: String?,
    onClickUser: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val name = displayName?.takeIf { it.isNotBlank() } ?: "bạn"
        val avatarText = if (name == "bạn") "UT" else initialsOf(name)
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onClickUser)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(UthBlueSoft),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = avatarText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = UthBlue
                )
                if (photoUrl != null) {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = todayLabel(),
                    fontSize = 14.sp,
                    color = UthInkSoft
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Chào ${givenNameOf(name)}!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = UthInk
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_bell),
                contentDescription = "Thông báo",
                tint = UthInk,
                modifier = Modifier.size(22.dp)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(UthRed)
            )
        }
    }
}

@Composable
private fun TodayContent() {
    ProgressCard(current = 3, target = 7, percent = 43)
    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        CategoryCard(
            modifier = Modifier.weight(1f),
            title = "Học tập",
            subtitle = "4 việc còn lại",
            tileColor = UthBlueSoft,
            tint = UthBlue,
            iconRes = R.drawable.ic_graduation_cap
        )
        CategoryCard(
            modifier = Modifier.weight(1f),
            title = "Cuộc sống",
            subtitle = "3 việc còn lại",
            tileColor = UthOrangeSoft,
            tint = UthOrange,
            iconRes = R.drawable.ic_figma_home_category
        )
    }

    Spacer(modifier = Modifier.height(18.dp))

    SectionHeader(title = "Việc hôm nay", action = "Xem tất cả")
    Spacer(modifier = Modifier.height(10.dp))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        homeTasks.forEach { task -> TaskRow(task = task) }
    }
}

@Composable
private fun ListContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "Danh sách",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = UthInk
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "4 việc chưa xong",
            fontSize = 12.sp,
            color = UthInkSoft
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAEAF0))
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_figma_search),
            contentDescription = null,
            tint = UthInkFaint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Tìm công việc",
            fontSize = 16.sp,
            color = UthInkFaint
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(label = "Tất cả", selected = true, onClick = {})
        FilterChip(label = "Học tập", selected = false, onClick = {})
        FilterChip(label = "Cuộc sống", selected = false, onClick = {})
    }

    Spacer(modifier = Modifier.height(18.dp))
    SectionLabel("CẦN LÀM")
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listNeedTasks.forEach { task -> TaskRow(task = task, showChevron = false) }
    }

    Spacer(modifier = Modifier.height(18.dp))
    SectionLabel("ĐÃ XONG")
    Spacer(modifier = Modifier.height(10.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listDoneTasks.forEach { task -> TaskRow(task = task, showMeta = false, showChevron = false) }
    }
}

@Composable
private fun ProgressCard(current: Int, target: Int, percent: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(UthBlue)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Tiến độ hôm nay",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$current/$target việc",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bắt đầu từ việc nhỏ nhất nhé.",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.75f)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 8.dp.toPx()
                drawCircle(
                    color = Color.White.copy(alpha = 0.25f),
                    style = Stroke(stroke)
                )
                drawArc(
                    color = Color.White,
                    startAngle = -90f,
                    sweepAngle = percent / 100f * 360f,
                    useCenter = false,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$percent%",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    subtitle: String,
    tileColor: Color,
    tint: Color,
    modifier: Modifier = Modifier,
    iconRes: Int
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tileColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = UthInk
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = UthInkSoft
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = UthInk
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = action,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = UthBlue
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = UthInkFaint
    )
}

@Composable
private fun TaskRow(task: TodoTask, showMeta: Boolean = true, showChevron: Boolean = true) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .let {
                    if (task.done) {
                        it.background(UthBlue)
                    } else {
                        it.border(1.6.dp, Color(0xFFC7C7CC), CircleShape)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (task.done) {
                Icon(
                    painter = painterResource(R.drawable.ic_figma_check),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (task.done) UthInkSoft else UthInk,
                textDecoration = if (task.done) TextDecoration.LineThrough else null
            )
            if (showMeta) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagChip(tag = task.tag)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildString {
                            task.dateLabel?.let {
                                append(it)
                                if (task.time.isNotBlank()) append(", ")
                            }
                            append(task.time)
                        },
                        fontSize = 11.5.sp,
                        color = UthInkSoft
                    )
                }
            }
        }

        if (task.overdue) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(UthRedSoft)
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "Gấp",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = UthRedText
                )
            }
        }

        if (showChevron) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                painter = painterResource(R.drawable.ic_figma_chevron_right),
                contentDescription = null,
                tint = UthInkFaint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TagChip(tag: TaskTag) {
    val isStudy = tag == TaskTag.HocTap
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isStudy) UthBlueSoft else UthOrangeSoft)
            .padding(horizontal = 7.dp, vertical = 2.5.dp)
    ) {
        Text(
            text = tag.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isStudy) UthBlue else UthOrange
        )
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) UthInk else Color.White)
            .border(1.dp, if (selected) UthInk else Color(0xFFDCDCE0), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else UthInk
        )
    }
}

@Composable
private fun BottomNavBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(top = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavDest(label = "Trang chủ", iconRes = R.drawable.ic_figma_home, selected = selectedIndex == 0) {
                onSelect(0)
            }
            NavDest(
                label = "Danh sách",
                iconRes = R.drawable.ic_figma_list,
                selected = selectedIndex == 1
            ) {
                onSelect(1)
            }
            Spacer(modifier = Modifier.width(64.dp))
            NavDest(label = "Thông báo", iconRes = R.drawable.ic_figma_notifications, selected = false) {
            }
            NavDest(label = "Cài đặt", iconRes = R.drawable.ic_figma_settings, selected = false) {
                onSelect(4)
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-16).dp)
                .size(56.dp)
                .clip(CircleShape)
                .background(UthBlue)
                .clickable(onClick = {}),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_figma_add),
                contentDescription = "Thêm mới",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun NavDest(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = if (selected) UthBlue else UthInkFaint,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) UthBlue else UthInkFaint
        )
    }
}

private fun todayLabel(): String {
    val locale = Locale.forLanguageTag("vi")
    val text = SimpleDateFormat("EEEE, d 'tháng' M", locale).format(Date())
    return text.replaceFirstChar { c -> c.uppercase(locale) }
}

private fun givenNameOf(name: String): String {
    val tokens = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return tokens.lastOrNull() ?: "bạn"
}

internal fun initialsOf(name: String): String {
    val tokens = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return "UT"
    if (tokens.size == 1) return tokens[0].take(2).uppercase()
    return (tokens.first().first().toString() + tokens.last().first()).uppercase()
}
