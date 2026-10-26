package com.example.librarymanagementapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.librarymanagementapp.model.*
import com.example.librarymanagementapp.ui.theme.LibraryManagementAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LibraryManagementAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LibraryManagementScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryManagementScreen() {
    val students = remember { SampleData.students }
    val books = remember { mutableStateListOf<Book>().apply { addAll(SampleData.createInitialBooks(students)) } }

    var currentStudent by remember { mutableStateOf(students[0]) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var showChangeStudentDialog by remember { mutableStateOf(false) }
    var showBorrowBookDialog by remember { mutableStateOf(false) }
    var selectedBookForDetail by remember { mutableStateOf<Book?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hệ thống\nQuản lý Thư viện",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            color = Color(0xFF1E293B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Quản lý") },
                    label = { Text("Quản lý") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "DS Sách") },
                    label = { Text("DS Sách") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Sinh viên") },
                    label = { Text("Sinh viên") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF1976D2),
                        selectedTextColor = Color(0xFF1976D2),
                        indicatorColor = Color(0xFFE3F2FD)
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
        ) {
            when (selectedTab) {
                0 -> MainManagementTab(
                    currentStudent = currentStudent,
                    books = books,
                    onChangeStudentClick = { showChangeStudentDialog = true },
                    onAddBookClick = { showBorrowBookDialog = true },
                    onBookClick = { selectedBookForDetail = it }
                )
                1 -> BookListTab(
                    books = books,
                    onBookClick = { selectedBookForDetail = it }
                )
                2 -> StudentListTab(
                    students = students,
                    books = books,
                    currentStudent = currentStudent,
                    onSelectStudent = {
                        currentStudent = it
                        selectedTab = 0
                    }
                )
            }

            statusMessage?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { statusMessage = null }) {
                            Text("Đóng", color = Color.White)
                        }
                    }
                ) {
                    Text(msg)
                }
            }
        }
    }

    if (showChangeStudentDialog) {
        AlertDialog(
            onDismissRequest = { showChangeStudentDialog = false },
            title = { Text("Chọn Sinh viên", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    students.forEach { s ->
                        val isSelected = s.id == currentStudent.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentStudent = s
                                    showChangeStudentDialog = false
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(s.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Mã SV: ${s.id} - ${s.major}", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF1976D2))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showChangeStudentDialog = false }) {
                    Text("Đóng")
                }
            }
        )
    }

    if (showBorrowBookDialog) {
        val availableBooks = books.filter { it.isAvailable() }
        AlertDialog(
            onDismissRequest = { showBorrowBookDialog = false },
            title = {
                Text("Chọn sách để mượn cho ${currentStudent.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                if (availableBooks.isEmpty()) {
                    Text("Hiện tại tất cả sách trong thư viện đều đã có người mượn!")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val success = book.borrow(currentStudent)
                                        statusMessage = if (success) {
                                            "Mượn '${book.title}' thành công!"
                                        } else {
                                            "Không thể mượn sách này."
                                        }
                                        showBorrowBookDialog = false
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(book.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            book.getBookTypeLabel(),
                                            fontSize = 11.sp,
                                            color = Color(0xFF1976D2),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Tác giả: ${book.author}", fontSize = 12.sp, color = Color.Gray)
                                    Text(book.getSpecialAttribute(), fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showBorrowBookDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    selectedBookForDetail?.let { book ->
        AlertDialog(
            onDismissRequest = { selectedBookForDetail = null },
            title = { Text(book.title, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Mã sách: ${book.id}")
                    Text("Tác giả: ${book.author}")
                    Text("Loại sách: ${book.getBookTypeLabel()}")
                    Text(book.getSpecialAttribute())
                    Text("Thời hạn mượn: ${book.getLoanDurationDays()} ngày")
                    Text("Phí phạt trễ hạn: ${book.calculateLateFee(1)} VNĐ/ngày")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    if (book.isAvailable()) {
                        Text("Trạng thái: Khả dụng", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    } else {
                        Text("Trạng thái: Đang mượn", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                        Text("Người mượn: ${book.borrowedByStudentName ?: "N/A"}")
                    }
                }
            },
            confirmButton = {
                if (!book.isAvailable() && book.borrowedByStudentId == currentStudent.id) {
                    Button(
                        onClick = {
                            book.returnItem()
                            statusMessage = "Đã trả '${book.title}' thành công!"
                            selectedBookForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Trả sách này")
                    }
                } else if (book.isAvailable()) {
                    Button(
                        onClick = {
                            book.borrow(currentStudent)
                            statusMessage = "Đã mượn '${book.title}' thành công!"
                            selectedBookForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text("Mượn sách")
                    }
                } else {
                    TextButton(onClick = { selectedBookForDetail = null }) {
                        Text("Đóng")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBookForDetail = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}

@Composable
fun MainManagementTab(
    currentStudent: Student,
    books: List<Book>,
    onChangeStudentClick: () -> Unit,
    onAddBookClick: () -> Unit,
    onBookClick: (Book) -> Unit
) {
    val borrowedBooks = books.filter { it.borrowedByStudentId == currentStudent.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Sinh viên",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = currentStudent.name,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF94A3B8),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Button(
                    onClick = onChangeStudentClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text("Thay đổi", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Danh sách sách",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD9D9D9)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                if (borrowedBooks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Bạn chưa mượn quyển sách nào\nNhấn 'Thêm' để bắt đầu hành trình đọc sách!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(borrowedBooks) { book ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onBookClick(book) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(Color(0xFFB71C1C), RoundedCornerShape(4.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = book.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF212121)
                                        )
                                        Text(
                                            text = "${book.getBookTypeLabel()} • Hạn: ${book.getLoanDurationDays()} ngày",
                                            fontSize = 11.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Chi tiết",
                                        tint = Color(0xFF9E9E9E),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddBookClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            modifier = Modifier
                .width(160.dp)
                .height(46.dp)
        ) {
            Text(
                text = "Thêm",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BookListTab(
    books: List<Book>,
    onBookClick: (Book) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Toàn bộ Sách trong Thư viện (${books.size})",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(books) { book ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onBookClick(book) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (book is TextBook) Color(0xFFE3F2FD) else Color(0xFFEDE7F6)
                            ) {
                                Text(
                                    text = book.getBookTypeLabel(),
                                    color = if (book is TextBook) Color(0xFF1976D2) else Color(0xFF673AB7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Tác giả: ${book.author}", fontSize = 13.sp, color = Color.Gray)
                        Text(book.getSpecialAttribute(), fontSize = 12.sp, color = Color(0xFF475569))

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hạn mượn: ${book.getLoanDurationDays()} ngày | Phạt: ${book.calculateLateFee(1)} đ/ngày",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            if (book.isAvailable()) {
                                Text(
                                    text = "Khả dụng",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Đang mượn: ${book.borrowedByStudentName}",
                                    color = Color(0xFFC62828),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentListTab(
    students: List<Student>,
    books: List<Book>,
    currentStudent: Student,
    onSelectStudent: (Student) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Danh sách Sinh viên",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(students) { student ->
                val borrowedCount = books.count { it.borrowedByStudentId == student.id }
                val isCurrent = student.id == currentStudent.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectStudent(student) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) Color(0xFFE3F2FD) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "(Đang chọn)",
                                        color = Color(0xFF1976D2),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("MSSV: ${student.id} - ${student.major}", fontSize = 12.sp, color = Color.Gray)
                            Text("Số sách đang mượn: $borrowedCount quyển", fontSize = 12.sp, color = Color(0xFF1976D2))
                        }

                        Button(
                            onClick = { onSelectStudent(student) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) Color(0xFF1976D2) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Text(
                                text = if (isCurrent) "Đã chọn" else "Chọn",
                                color = if (isCurrent) Color.White else Color(0xFF334155),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, name = "Quản lý - Có sách mượn (Nguyen Van A)")
@Composable
fun MainManagementTabPreview() {
    LibraryManagementAppTheme {
        val students = SampleData.students
        val books = SampleData.createInitialBooks(students)
        MainManagementTab(
            currentStudent = students[0],
            books = books,
            onChangeStudentClick = {},
            onAddBookClick = {},
            onBookClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Quản lý - Chưa mượn sách (Nguyen Van C)")
@Composable
fun MainManagementTabEmptyPreview() {
    LibraryManagementAppTheme {
        val students = SampleData.students
        val books = SampleData.createInitialBooks(students)
        MainManagementTab(
            currentStudent = students[2],
            books = books,
            onChangeStudentClick = {},
            onAddBookClick = {},
            onBookClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Danh sách Sách (DS Sách)")
@Composable
fun BookListTabPreview() {
    LibraryManagementAppTheme {
        val students = SampleData.students
        val books = SampleData.createInitialBooks(students)
        BookListTab(
            books = books,
            onBookClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Danh sách Sinh viên")
@Composable
fun StudentListTabPreview() {
    LibraryManagementAppTheme {
        val students = SampleData.students
        val books = SampleData.createInitialBooks(students)
        StudentListTab(
            students = students,
            books = books,
            currentStudent = students[0],
            onSelectStudent = {}
        )
    }
}
