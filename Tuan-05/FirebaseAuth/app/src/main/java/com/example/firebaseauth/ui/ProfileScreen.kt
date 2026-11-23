package com.example.firebaseauth.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.firebaseauth.R
import com.example.firebaseauth.ui.theme.UthBg
import com.example.firebaseauth.ui.theme.UthBlue
import com.example.firebaseauth.ui.theme.UthBlueSoft
import com.example.firebaseauth.ui.theme.UthGreen
import com.example.firebaseauth.ui.theme.UthGreenSoft
import com.example.firebaseauth.ui.theme.UthInk
import com.example.firebaseauth.ui.theme.UthInkFaint
import com.example.firebaseauth.ui.theme.UthInkSoft
import com.example.firebaseauth.ui.theme.UthLine

@Composable
fun ProfileScreen(
    name: String?,
    email: String?,
    photoUrl: String?,
    linked: Boolean,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(UthBg).statusBarsPadding().navigationBarsPadding()
    ) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier.fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight)
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onBack)
                        .heightIn(min = 48.dp)
                        .align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_figma_back),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(UthBlue),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cài đặt",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = UthBlue
                    )
                }
                Text(
                    text = "Hồ sơ",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UthInk,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = "Sửa",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = UthBlue,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .padding(6.dp)
                        .align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(UthBlueSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(name ?: "KH"),
                        fontSize = 32.sp,
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
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(UthBlue)
                        .border(3.dp, UthBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_figma_camera),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = name?.takeIf { it.isNotBlank() } ?: "Chưa đặt tên",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = UthInk,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = email?.takeIf { it.isNotBlank() } ?: "Chưa có tài khoản được liên kết",
                fontSize = 14.sp,
                color = UthInkSoft,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (linked) UthGreenSoft else UthLine)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_figma_linked),
                        contentDescription = null,
                        tint = if (linked) UthGreen else UthInkFaint,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (linked) "Đã liên kết tài khoản Google" else "Chưa liên kết tài khoản",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (linked) UthGreen else UthInkSoft
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("128", "Việc đã xong", Modifier.weight(1f))
                StatCard("12 ngày", "Chuỗi liên tiếp", Modifier.weight(1f))
                StatCard("86%", "Đúng hạn", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Tuần này",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = UthInk
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "18/22 việc",
                            fontSize = 11.sp,
                            color = UthInkSoft
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val bars = listOf(54, 72, 36, 72, 54, 18, 18)
                    val labels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                    ) {
                        val slot = size.width / bars.size
                        val max = bars.max()
                        bars.forEachIndexed { index, value ->
                            val barHeight = size.height * (value / max.toFloat())
                            drawRoundRect(
                                color = when (index) {
                                    0 -> UthBlue.copy(alpha = 0.35f)
                                    1 -> UthBlue
                                    else -> Color(0xFFE4E4EA)
                                },
                                topLeft = Offset(
                                    x = slot * index + (slot - 26.dp.toPx()) / 2,
                                    y = size.height - barHeight
                                ),
                                size = Size(26.dp.toPx(), barHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        labels.forEachIndexed { index, label ->
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (index == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == 1) UthBlue else UthInkFaint,
                                modifier = Modifier.weight(1f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
            ) {
                Column {
                    SettingRow(
                        icon = { tint, size ->
                            Icon(
                                painter = painterResource(R.drawable.ic_figma_profile_sync),
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(size)
                            )
                        },
                        title = "Đồng bộ dữ liệu",
                        trailing = { Text(text = "Vừa xong", fontSize = 12.sp, color = UthInkSoft) }
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(UthLine)
                    )
                    SettingRow(
                        icon = { tint, size ->
                            Icon(
                                painter = painterResource(R.drawable.ic_figma_lock),
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(size)
                            )
                        },
                        title = "Quyền riêng tư",
                        trailing = {
                            Icon(
                                painter = painterResource(R.drawable.ic_figma_profile_chevron),
                                contentDescription = null,
                                tint = UthInkFaint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f).heightIn(min = 24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .clickable(onClick = onSignOut)
                    .padding(vertical = 15.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_figma_sign_out),
                    contentDescription = null,
                    tint = Color(0xFFC4321F),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đăng xuất",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFC4321F)
                )
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = UthInk
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = UthInkSoft
            )
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    trailing: @Composable () -> Unit,
    icon: @Composable (Color, androidx.compose.ui.unit.Dp) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            icon(UthInkSoft, 20.dp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = UthInk,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}
