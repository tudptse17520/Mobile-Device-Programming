package com.example.gradebookapp

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradebookapp.ui.theme.GradebookAppTheme

data class Student(
    val name: String,
    val score: Double?
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradebookAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GradebookScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun getClassification(score: Double): String {
    if (score < 0 || score > 10) {
        return "Không hợp lệ"
    } else if (score >= 8.0) {
        return "Giỏi"
    } else if (score >= 6.5) {
        return "Khá"
    } else if (score >= 5.0) {
        return "Trung bình"
    } else {
        return "Yếu"
    }
}

fun getClassificationColor(classification: String): Color {
    if (classification == "Giỏi") {
        return Color(0xFF2E7D32)
    } else if (classification == "Khá") {
        return Color(0xFF1976D2)
    } else if (classification == "Trung bình") {
        return Color(0xFFED6C02)
    } else if (classification == "Yếu") {
        return Color(0xFFD32F2F)
    } else {
        return Color.Gray
    }
}

@Composable
fun GradebookScreen(modifier: Modifier = Modifier) {
    val students = listOf(
        Student("Nguyễn Văn An", 9.0),
        Student("Trần Thị Bình", 7.2),
        Student("Lê Minh Chi", null),
        Student("Phạm Quốc Dũng", 4.5),
        Student("Võ Thanh Em", 5.8)
    )

    var totalScore = 0.0
    var scoredCount = 0
    var excellentCount = 0
    var noScoreCount = 0
    var maxScore = 0.0

    for (s in students) {
        if (s.score != null) {
            totalScore += s.score
            scoredCount++

            if (s.score >= 8.0) {
                excellentCount++
            }

            if (s.score > maxScore) {
                maxScore = s.score
            }
        } else {
            noScoreCount++
        }
    }

    val averageScore = if (scoredCount > 0) totalScore / scoredCount else 0.0
    val averageScoreText = "%.2f".format(averageScore)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF264073))
                .padding(20.dp)
        ) {
            Text(
                text = "Bảng điểm lớp",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF264073))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Điểm trung bình lớp",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = averageScoreText,
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Giỏi",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$excellentCount",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "Chưa có điểm",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$noScoreCount",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = "Cao nhất",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$maxScore",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "DANH SÁCH • ${students.size} SINH VIÊN",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            for (student in students) {
                StudentScoreCard(student = student)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun StudentScoreCard(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = student.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (student.score != null) {
                    val classification = getClassification(student.score)
                    Text(
                        text = classification,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = getClassificationColor(classification)
                    )
                } else {
                    Text(
                        text = "Chưa có điểm",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            if (student.score != null) {
                Text(
                    text = "${student.score}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            } else {
                Text(
                    text = "—",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GradebookScreenPreview() {
    GradebookAppTheme {
        GradebookScreen()
    }
}
