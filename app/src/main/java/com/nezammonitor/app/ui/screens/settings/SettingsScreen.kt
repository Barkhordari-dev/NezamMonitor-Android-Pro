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

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("nezam_settings", Context.MODE_PRIVATE) }
    
    var isRtl by remember { mutableStateOf(prefs.getBoolean("rtl", true)) }
    var fontSize by remember { mutableFloatStateOf(prefs.getFloat("font_scale", 1.0f)) }
    var themeMode by remember { mutableIntStateOf(prefs.getInt("theme", 0)) }
    
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = themeMode == index,
                            onClick = {
                                themeMode = index
                                prefs.edit().putInt("theme", index).apply()
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(name, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // RTL
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("جهت صفحه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("راست به چپ (فارسی)", fontSize = 14.sp)
                    Switch(
                        checked = isRtl,
                        onCheckedChange = {
                            isRtl = it
                            prefs.edit().putBoolean("rtl", it).apply()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Font Size
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اندازه متن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("کوچک", fontSize = 12.sp)
                    Text("عادی", fontSize = 14.sp)
                    Text("بزرگ", fontSize = 16.sp)
                }
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    onValueChangeFinished = {
                        prefs.edit().putFloat("font_scale", fontSize).apply()
                    },
                    valueRange = 0.85f..1.3f,
                    steps = 2
                )
                Text("اندازه فعلی: ${when {
                    fontSize < 0.92f -> "کوچک"
                    fontSize < 1.1f -> "عادی"
                    else -> "بزرگ"
                }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
