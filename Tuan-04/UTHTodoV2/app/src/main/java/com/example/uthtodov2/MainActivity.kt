package com.example.uthtodov2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.uthtodov2.model.OnboardingPage
import com.example.uthtodov2.model.TaskCategory
import com.example.uthtodov2.model.TodoTask
import com.example.uthtodov2.ui.theme.UTHTodoV2Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UTHTodoV2Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UTHTodoAppNavigation()
                }
            }
        }
    }
}

val sampleTasks = listOf(
    TodoTask("1", "Nộp bài tập Lập trình di động", TaskCategory.STUDY, initialCompleted = true, time = "Hôm nay, 09:00"),
    TodoTask("2", "Ôn chương 3 – Cấu trúc dữ liệu", TaskCategory.STUDY, initialCompleted = false, time = "Hôm nay, 14:00"),
    TodoTask("3", "Đi chợ mua đồ cho tuần", TaskCategory.LIFE, initialCompleted = false, time = "Hôm nay, 17:30"),
    TodoTask("4", "Họp nhóm đồ án", TaskCategory.STUDY, initialCompleted = false, time = "Thứ Năm, 20:00"),
    TodoTask("5", "Tập thể dục 30 phút", TaskCategory.LIFE, initialCompleted = true, time = "06:30"),
    TodoTask("6", "Đọc 20 trang sách", TaskCategory.LIFE, initialCompleted = true, time = "21:00")
)

val onboardingPages = listOf(
    OnboardingPage(
        step = 1,
        title = "Mọi việc, gọn trong một chỗ",
        description = "Ghi lại bài tập, việc nhà, lịch hẹn. Không còn quên việc nào.",
        buttonText = "Tiếp tục"
    ),
    OnboardingPage(
        step = 2,
        title = "Chia theo Học tập và Cuộc sống",
        description = "Mỗi việc có nhóm riêng, nhìn là biết hôm nay cần lo gì.",
        buttonText = "Tiếp tục"
    ),
    OnboardingPage(
        step = 3,
        title = "Nhắc đúng lúc",
        description = "Bật thông báo để UTH Todo nhắc bạn trước hạn chót.",
        buttonText = "Bắt đầu ngay"
    )
)

@Composable
fun UTHTodoAppNavigation() {
    val navController = rememberNavController()
    val tasks = remember { mutableStateListOf<TodoTask>().apply { addAll(sampleTasks) } }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateNext = {
                    navController.navigate("onboarding/1") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "onboarding/{step}",
            arguments = listOf(navArgument("step") { type = NavType.IntType; defaultValue = 1 })
        ) { backStackEntry ->
            val step = backStackEntry.arguments?.getInt("step") ?: 1
            OnboardingScreen(
                step = step,
                onSkip = {
                    navController.navigate("todo") {
                        popUpTo("onboarding/1") { inclusive = true }
                    }
                },
                onNext = {
                    if (step < 3) {
                        navController.navigate("onboarding/${step + 1}")
                    } else {
                        navController.navigate("todo") {
                            popUpTo("onboarding/1") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("todo") {
            TodoScreen(
                tasks = tasks,
                onAddTask = { title, category ->
                    val newTask = TodoTask(
                        id = (tasks.size + 1).toString(),
                        title = title,
                        category = category
                    )
                    tasks.add(0, newTask)
                }
            )
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateNext: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        delay(1800)
        onNavigateNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2F5BEA))
            .clickable { onNavigateNext() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.size(110.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    UthLogoIconContent(color = Color(0xFF2F5BEA))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "UTH Todo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Việc học, việc nhà – gọn gàng mỗi ngày",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    step: Int,
    onSkip: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val pageData = onboardingPages.getOrNull(step - 1) ?: onboardingPages[0]

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF2F5BEA)),
                        contentAlignment = Alignment.Center
                    ) {
                        UthLogoIconContent(color = Color.White, scale = 0.6f)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "UTH ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C1C1E)
                    )
                    Text(
                        text = "Todo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2F5BEA)
                    )
                }

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Bỏ qua",
                        color = Color(0xFF6B7280),
                        fontSize = 14.sp
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isCurrent = i == step
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(6.dp)
                                .width(if (isCurrent) 22.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) Color(0xFF2F5BEA) else Color(0xFFD1D5DB))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F5BEA))
                ) {
                    Text(
                        text = pageData.buttonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5FD)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when (step) {
                        1 -> OnboardingIllustrationStep1()
                        2 -> OnboardingIllustrationStep2()
                        3 -> OnboardingIllustrationStep3()
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = pageData.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C1C1E),
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = pageData.description,
                    fontSize = 15.sp,
                    color = Color(0xFF6B7280),
                    lineHeight = 22.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustrationStep1() {
    Card(
        modifier = Modifier
            .width(220.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2F5BEA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustrationStep2() {
    Card(
        modifier = Modifier
            .width(220.dp)
            .padding(12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEBF0FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Học tập",
                        color = Color(0xFF2F5BEA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
            Column {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Cuộc sống",
                        color = Color(0xFFE65100),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .height(10.dp)
                        .fillMaxWidth(0.75f)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color(0xFFE2E8F0))
                )
            }
        }
    }
}

@Composable
fun OnboardingIllustrationStep3() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF2F5BEA),
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Ôn bài Toán lúc 19:00",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun UthLogoIconContent(color: Color, scale: Float = 1.0f) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)
        ) {
            Box(
                modifier = Modifier
                    .size((22 * scale).dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color == Color.White) Color(0xFF2F5BEA) else Color.White,
                    modifier = Modifier.size((14 * scale).dp)
                )
            }
            Box(
                modifier = Modifier
                    .height((8 * scale).dp)
                    .width((34 * scale).dp)
                    .clip(RoundedCornerShape((4 * scale).dp))
                    .background(color)
            )
        }

        Spacer(modifier = Modifier.height((6 * scale).dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy((6 * scale).dp)
        ) {
            Box(
                modifier = Modifier
                    .size((22 * scale).dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.5f))
            )
            Box(
                modifier = Modifier
                    .height((8 * scale).dp)
                    .width((34 * scale).dp)
                    .clip(RoundedCornerShape((4 * scale).dp))
                    .background(color.copy(alpha = 0.5f))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    tasks: List<TodoTask>,
    onAddTask: (String, TaskCategory) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Tất cả") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskCategory by remember { mutableStateOf(TaskCategory.STUDY) }

    val filteredTasks = tasks.filter { task ->
        when (selectedFilter) {
            "Học tập" -> task.category == TaskCategory.STUDY
            "Cuộc sống" -> task.category == TaskCategory.LIFE
            else -> true
        }
    }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UTH ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF1C1C1E)
                            )
                            Text(
                                text = "Todo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(0xFF2F5BEA)
                            )
                        }
                        Text(
                            text = "Thực hành Navigation & OOP",
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2F5BEA),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm công việc")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2F5BEA))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tiến độ hôm nay",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedCount/$totalCount việc",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (completedCount == totalCount && totalCount > 0)
                                "Xuất sắc! Bạn đã xong hết việc!"
                            else
                                "Bắt đầu từ việc nhỏ nhất nhé.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    val percent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$percent%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Tất cả", "Học tập", "Cuộc sống").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2F5BEA),
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Danh sách công việc (${filteredTasks.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredTasks, key = { it.id }) { task ->
                    TodoTaskItemCard(
                        task = task,
                        onToggle = { task.toggleCompleted() }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Thêm việc mới", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTaskTitle,
                        onValueChange = { newTaskTitle = it },
                        label = { Text("Tên công việc") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Danh mục:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskCategory.entries.forEach { cat ->
                            val isSelected = newTaskCategory == cat
                            OutlinedButton(
                                onClick = { newTaskCategory = cat },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) Color(0xFF2F5BEA) else Color.Transparent
                                )
                            ) {
                                Text(
                                    cat.displayName,
                                    color = if (isSelected) Color.White else Color(0xFF1C1C1E)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            onAddTask(newTaskTitle, newTaskCategory)
                            newTaskTitle = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2F5BEA))
                ) {
                    Text("Lưu")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun TodoTaskItemCard(
    task: TodoTask,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) Color(0xFF2F5BEA) else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Hoàn thành",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (task.isCompleted) Color(0xFF9E9E9E) else Color(0xFF1C1C1E),
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(task.category.badgeBgColor))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category.displayName,
                            color = Color(task.category.badgeColor),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = task.time,
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Splash Screen")
@Composable
fun SplashScreenPreview() {
    UTHTodoV2Theme {
        SplashScreen()
    }
}

@Preview(showBackground = true, name = "Onboarding Bước 1")
@Composable
fun OnboardingStep1Preview() {
    UTHTodoV2Theme {
        OnboardingScreen(step = 1)
    }
}

@Preview(showBackground = true, name = "Onboarding Bước 2")
@Composable
fun OnboardingStep2Preview() {
    UTHTodoV2Theme {
        OnboardingScreen(step = 2)
    }
}

@Preview(showBackground = true, name = "Onboarding Bước 3")
@Composable
fun OnboardingStep3Preview() {
    UTHTodoV2Theme {
        OnboardingScreen(step = 3)
    }
}

@Preview(showBackground = true, name = "Màn hình Todo chính")
@Composable
fun TodoScreenPreview() {
    UTHTodoV2Theme {
        TodoScreen(
            tasks = sampleTasks,
            onAddTask = { _, _ -> }
        )
    }
}
