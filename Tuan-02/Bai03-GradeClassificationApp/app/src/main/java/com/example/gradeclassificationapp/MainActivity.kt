package com.example.gradeclassificationapp

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
import com.example.gradeclassificationapp.ui.theme.GradeClassificationAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GradeClassificationAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GradeClassificationScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// Xếp loại học lực
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
        return Color(0xFF757575)
    }
}

@Composable
fun GradeClassificationScreen(modifier: Modifier = Modifier) {
    val score = 8.5
    val classification = getClassification(score)
    val testScores = listOf(9.0, 7.0, 5.5, 3.0, 11.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5F9))
    ) {
        // AppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF264073))
                .padding(20.dp)
        ) {
            Text(
                text = "Xếp loại học lực",
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
            // Thẻ điểm chính
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Điểm trung bình",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$score",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val mainColor = getClassificationColor(classification)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = mainColor.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = classification,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = mainColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "KIỂM TRA THÊM",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Danh sách kiểm tra thêm
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    for (i in testScores.indices) {
                        val testScore = testScores[i]
                        val result = getClassification(testScore)
                        val textColor = getClassificationColor(result)

                        val scoreText = if (testScore % 1.0 == 0.0) {
                            "${testScore.toInt()}"
                        } else {
                            "$testScore"
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Điểm $scoreText",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )

                            Box(
                                modifier = Modifier
                                    .background(
                                        color = textColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = result,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                            }
                        }

                        if (i < testScores.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GradeClassificationScreenPreview() {
    GradeClassificationAppTheme {
        GradeClassificationScreen()
    }
}
