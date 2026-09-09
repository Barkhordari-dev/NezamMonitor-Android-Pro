package com.nezammonitor.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen() {
    var isRtl by remember { mutableStateOf(true) }
    var fontSize by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("تنظیمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // RTL/LTR Setting
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("جهت نمایش", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("راست به چپ (فارسی)", fontSize = 14.sp)
                    Switch(
                        checked = isRtl,
                        onCheckedChange = { isRtl = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Font Size Setting
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اندازه متن", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عادی", fontSize = 14.sp)
                    Text("بزرگ", fontSize = 16.sp)
                    Text("خیلی بزرگ", fontSize = 18.sp)
                }
                Slider(
                    value = fontSize,
                    onValueChange = { fontSize = it },
                    valueRange = 0.8f..1.3f,
                    steps = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App Info
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اطلاعات برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("نسخه: 1.0", fontSize = 14.sp)
                Text("پکیج: com.nezammonitor.app", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("ساخته شده توسط مهندس برخورداری", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
