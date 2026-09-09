package com.nezammonitor.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

data class AppPreferences(
    val themeMode: Int = 0, // 0=Light, 1=Dark, 2=System
    val isRtl: Boolean = true,
    val fontScale: Float = 1.0f
) {
    companion object {
        fun load(context: Context): AppPreferences {
            val prefs = context.getSharedPreferences("nezam_settings", Context.MODE_PRIVATE)
            return AppPreferences(
                themeMode = prefs.getInt("theme", 0),
                isRtl = prefs.getBoolean("rtl", true),
                fontScale = prefs.getFloat("font_scale", 1.0f)
            )
        }
    }

    fun save(context: Context) {
        context.getSharedPreferences("nezam_settings", Context.MODE_PRIVATE).edit()
            .putInt("theme", themeMode)
            .putBoolean("rtl", isRtl)
            .putFloat("font_scale", fontScale)
            .apply()
    }
}

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var prefs by remember { mutableStateOf(AppPreferences.load(context)) }

    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    val fees by viewModel.fees.collectAsState()
    val reports by viewModel.reports.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("تنظیمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        // Theme
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("پوسته (Theme)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val themes = listOf("روشن", "تاریک", "سیستمی")
                themes.forEachIndexed { index, name ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = prefs.themeMode == index,
                            onClick = {
                                prefs = prefs.copy(themeMode = index)
                                prefs.save(context)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, fontSize = 14.sp)
                    }
                }
                // Preview
                Spacer(modifier = Modifier.height(8.dp))
                val previewBg = when(prefs.themeMode) {
                    1 -> MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    else -> MaterialTheme.colorScheme.surface
                }
                Card(colors = CardDefaults.cardColors(containerColor = previewBg)) {
                    Text("پیش‌نمایش متن", modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RTL
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("جهت صفحه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("راست به چپ (فارسی)", fontSize = 14.sp)
                    Switch(checked = prefs.isRtl, onCheckedChange = {
                        prefs = prefs.copy(isRtl = it)
                        prefs.save(context)
                    })
                }
                Text("تأیید: ${if (prefs.isRtl) "راست به چپ ✅" else "چپ به راست ✅"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Font Size
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اندازه متن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("کوچک", fontSize = 12.sp); Text("عادی", fontSize = 14.sp); Text("بزرگ", fontSize = 16.sp)
                }
                Slider(
                    value = prefs.fontScale,
                    onValueChange = { prefs = prefs.copy(fontScale = it) },
                    onValueChangeFinished = { prefs.save(context) },
                    valueRange = 0.85f..1.3f,
                    steps = 2
                )
                Text("اندازه فعلی: ${when {
                    prefs.fontScale < 0.92f -> "کوچک"
                    prefs.fontScale < 1.1f -> "عادی"
                    else -> "بزرگ"
                }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                // Preview
                Spacer(modifier = Modifier.height(8.dp))
                Text("پیش‌نمایش: ${"این متن با اندازه تغییر یافته نمایش داده می‌شود."}", fontSize = (14 * prefs.fontScale).sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Data Info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اطلاعات داده", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("پرونده\u200Cها: ${cases.size}", fontSize = 14.sp)
                Text("مهندسین: ${engineers.size}", fontSize = 14.sp)
                Text("حق\u200Cالزحمه: ${fees.size}", fontSize = 14.sp)
                Text("گزارش\u200Cها: ${reports.size}", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("درباره برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("نسخه: 1.0.0", fontSize = 14.sp)
                Text("پکیج: com.nezammonitor.app", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ساخته شده توسط مهندس برخورداری", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
