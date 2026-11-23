package com.example.firebaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.firebaseauth.R
import com.example.firebaseauth.ui.theme.UthBlue
import com.example.firebaseauth.ui.theme.UthInk
import com.example.firebaseauth.ui.theme.UthInkSoft
import com.example.firebaseauth.ui.theme.UthLine
import com.example.firebaseauth.ui.theme.UthRed
import com.example.firebaseauth.ui.theme.UthRedSoft
import com.example.firebaseauth.ui.theme.UthRedText

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleSignIn: () -> Unit,
    onTryWithoutAccount: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize().background(Color.White).systemBarsPadding()
    ) {
        val viewportHeight = maxHeight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = viewportHeight)
                .padding(horizontal = 28.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .offset(x = (-12).dp)
                    .clip(CircleShape)
                    .clickable(onClick = onClose),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_chevron_back),
                    contentDescription = "Quay lại",
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 28.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_figma_logo),
                    contentDescription = null,
                    modifier = Modifier.size(88.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(UthBlue)
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    text = "Chào mừng đến với\nUTH Todo",
                    fontSize = 30.sp,
                    lineHeight = 38.sp,
                    letterSpacing = (-0.4).sp,
                    fontWeight = FontWeight.Bold,
                    color = UthInk
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Đăng nhập để lưu công việc và đồng bộ trên mọi thiết bị của bạn.",
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF5C5C62)
                )
                Spacer(Modifier.height(28.dp))
                FeatureRow("Tự động sao lưu, không lo mất dữ liệu", R.drawable.ic_figma_sync)
                Spacer(Modifier.height(10.dp))
                FeatureRow("Dùng trên iPhone, iPad và Mac", R.drawable.ic_figma_phone)
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(58.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFDADCE0), RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLoading, onClick = onGoogleSignIn),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = UthBlue,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(26.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(26.dp)
                                .border(1.dp, Color(0xFFC7C7CC), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = UthInkSoft)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Tiếp tục với Google",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = UthInk
                    )
                }
            }
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                ErrorCard(errorMessage)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(UthLine))
                Text("hoặc", fontSize = 13.sp, color = UthInkSoft, modifier = Modifier.padding(horizontal = 12.dp))
                Box(Modifier.weight(1f).height(1.dp).background(UthLine))
            }
            Spacer(Modifier.height(14.dp))
            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !isLoading, onClick = onTryWithoutAccount),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Dùng thử không cần tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = UthBlue,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                text = buildAnnotatedString {
                    append("Khi tiếp tục, bạn đồng ý với ")
                    withStyle(SpanStyle(color = Color(0xFF3A3A3C), textDecoration = TextDecoration.Underline)) {
                        append("Điều khoản sử dụng")
                    }
                    append(" và ")
                    withStyle(SpanStyle(color = Color(0xFF3A3A3C), textDecoration = TextDecoration.Underline)) {
                        append("Chính sách quyền riêng tư")
                    }
                    append(" của UTH Todo.")
                },
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = UthInkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FeatureRow(text: String, iconRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF3A3A3C))
    }
}

@Composable
private fun ErrorCard(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(UthRedSoft)
            .border(1.dp, UthRed.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = UthRedText)
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Đăng nhập thất bại", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = UthRedText)
            Spacer(Modifier.height(2.dp))
            Text(message, fontSize = 12.sp, lineHeight = 16.sp, color = UthInkSoft)
        }
    }
}
