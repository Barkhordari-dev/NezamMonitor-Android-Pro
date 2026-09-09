package com.nezammonitor.app.ui.screens.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val case by viewModel.caseDetail.collectAsState()
    val reports by viewModel.caseReports.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("گزارش\u200Cهای پرونده", fontSize = 16.sp)
                        Text(case?.caseNumber ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مالک: ${case?.owner ?: ""}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("پرونده: ${case?.caseNumber ?: ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            if (case?.ownerMobile?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Phone, contentDescription = "تماس", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable {
                                    val cleaned = case?.ownerMobile?.replace(Regex("[^0-9+]"), "")
                                    context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleaned")))
                                })
                            }
                        }
                    }
                }
            }

            if (reports.isNotEmpty()) {
                item { Text("گزارش\u200Cها (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                items(reports) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(report.reportType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("مرحله ${report.stage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("ردیف: ${report.rowNo}", fontSize = 12.sp)
                                Text("تاریخ: ${report.visitDate}", fontSize = 12.sp)
                            }
                            Text("مهندس: ${report.engineer}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("رشته: ${report.discipline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (report.ceilingCount.isNotBlank()) {
                                Text("تعداد سقف: ${report.ceilingCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (report.indicator.isNotBlank()) {
                                Text("شماره اندیکاتور: ${report.indicator}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                item { Text("گزارشی یافت نشد", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}
