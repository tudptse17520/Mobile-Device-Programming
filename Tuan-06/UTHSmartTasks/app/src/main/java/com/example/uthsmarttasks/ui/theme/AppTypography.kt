package com.example.uthsmarttasks.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.uthsmarttasks.R

private val BeVietnamPro = FontFamily(
    Font(R.font.be_vietnam_pro_regular, FontWeight.Normal),
    Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
    Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold),
    Font(R.font.be_vietnam_pro_bold, FontWeight.Bold)
)

private fun TextStyle.beVietnamPro() = copy(fontFamily = BeVietnamPro, letterSpacing = 0.sp)

val AppTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.beVietnamPro(),
        displayMedium = base.displayMedium.beVietnamPro(),
        displaySmall = base.displaySmall.beVietnamPro(),
        headlineLarge = base.headlineLarge.beVietnamPro(),
        headlineMedium = base.headlineMedium.beVietnamPro(),
        headlineSmall = base.headlineSmall.beVietnamPro().copy(fontSize = 28.sp, lineHeight = 35.sp, letterSpacing = (-0.4).sp),
        titleLarge = base.titleLarge.beVietnamPro(),
        titleMedium = base.titleMedium.beVietnamPro(),
        titleSmall = base.titleSmall.beVietnamPro(),
        bodyLarge = base.bodyLarge.beVietnamPro(),
        bodyMedium = base.bodyMedium.beVietnamPro().copy(fontSize = 15.sp, lineHeight = 22.sp, letterSpacing = 0.sp),
        bodySmall = base.bodySmall.beVietnamPro().copy(fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
        labelLarge = base.labelLarge.beVietnamPro(),
        labelMedium = base.labelMedium.beVietnamPro(),
        labelSmall = base.labelSmall.beVietnamPro().copy(fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.sp)
    )
}
