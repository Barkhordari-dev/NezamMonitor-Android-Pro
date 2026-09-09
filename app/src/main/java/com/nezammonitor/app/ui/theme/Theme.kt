package com.nezammonitor.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colors
val Primary = Color(0xFF1565C0)
val PrimaryDark = Color(0xFF0D47A1)
val Secondary = Color(0xFF26A69A)
val Background = Color(0xFFF5F5F5)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1A1A)
val OnSurfaceVariant = Color(0xFF666666)
val Error = Color(0xFFD32F2F)
val Success = Color(0xFF388E3C)
val Warning = Color(0xFFF57C00)
val CardBorder = Color(0xFFE0E0E0)
val Divider = Color(0xFFE0E0E0)

// Typography sizes
object AppTypography {
    val TitleLarge = 22.sp
    val TitleMedium = 18.sp
    val TitleSmall = 16.sp
    val BodyLarge = 15.sp
    val BodyMedium = 14.sp
    val BodySmall = 12.sp
    val LabelLarge = 14.sp
    val LabelMedium = 12.sp
    val LabelSmall = 10.sp
    val HeadlineLarge = 28.sp
}

// Font sizes for accessibility
object FontScale {
    const val Normal = 1.0f
    const val Large = 1.15f
    const val ExtraLarge = 1.3f
}

// Spacing
object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

// Corner radius
object AppCorners {
    val small = 8
    val medium = 12
    val large = 16
}
