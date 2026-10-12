package com.example.studentlistapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studentlistapp.ui.theme.StudentListAppTheme

data class Student(
    val name: String,
    val email: String?,
    val phone: String?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudentListAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StudentListScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun StudentListScreen(modifier: Modifier = Modifier) {
    val students = listOf(
        Student("Nguyễn Văn An", "an@sv.edu.vn", "0901 234 567"),
        Student("Trần Thị Bình", null, "0912 888 999"),
        Student("Lê Minh Chi", "chi@sv.edu.vn", null),
        Student("Phạm Quốc Dũng", null, null),
        Student("Võ Thanh Em", "em@sv.edu.vn", "0933 111 222")
    )

    // Đếm số sinh viên thiếu email
    var missingEmailCount = 0
    for (s in students) {
        if (s.email == null) {
            missingEmailCount++
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        // AppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Danh sách sinh viên",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Danh sách sinh viên
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            for (student in students) {
                StudentCard(student = student)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            SummaryCard(
                totalCount = students.size,
                missingEmailCount = missingEmailCount
            )
        }
    }
}

@Composable
fun StudentCard(student: Student) {
    // Xử lý null
    val displayEmail = student.email ?: "Chưa cập nhật"
    val displayPhone = student.phone ?: "Chưa cập nhật"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = student.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row {
                Text(text = "Email: ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = displayEmail,
                    fontSize = 14.sp,
                    fontStyle = if (student.email == null) FontStyle.Italic else FontStyle.Normal,
                    color = if (student.email == null) Color.Red else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row {
                Text(text = "SĐT: ", fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = displayPhone,
                    fontSize = 14.sp,
                    fontStyle = if (student.phone == null) FontStyle.Italic else FontStyle.Normal,
                    color = if (student.phone == null) Color.Red else Color.Black
                )
            }
        }
    }
}

@Composable
fun SummaryCard(totalCount: Int, missingEmailCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(text = "Tổng: ", fontSize = 15.sp, color = Color.Black)
                Text(
                    text = "$totalCount sinh viên",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
            }

            Row {
                Text(text = "Thiếu email: ", fontSize = 15.sp, color = Color.Black)
                Text(
                    text = "$missingEmailCount",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentListScreenPreview() {
    StudentListAppTheme {
        StudentListScreen()
    }
}
