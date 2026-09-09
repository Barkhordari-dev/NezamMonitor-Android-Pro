package com.nezammonitor.app.ui.screens.cases

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
fun CaseDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val case by viewModel.caseDetail.collectAsState()
    val spec by viewModel.caseSpec.collectAsState()
    val engineers by viewModel.caseEngineers.collectAsState()
    val fees by viewModel.caseFees.collectAsState()
    val reports by viewModel.caseReports.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(case?.caseNumber ?: "جزئیات پرونده") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("اطلاعات اصلی", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("شماره پرونده", case?.caseNumber ?: "")
                        DetailRow("سریال", case?.serial ?: "")
                        DetailRow("مالک", case?.owner ?: "")
                        DetailRow("همراه", case?.ownerMobile ?: "")
                        DetailRow("آدرس", spec?.address ?: "")
                        DetailRow("دفتر", case?.office ?: "")
                        DetailRow("مسئولیت", case?.responsibility ?: "")
                        DetailRow("تاریخ کسر ظرفیت", case?.capacityDate ?: "")
                    }
                }
            }

            // Building Specs
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مشخصات ساختمان", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        DetailRow("گروه ساختمانی", spec?.buildingGroup ?: "")
                        DetailRow("کد نوسازی", spec?.renovationCode ?: "")
                        DetailRow("نوع دستور", spec?.planInstructionType ?: "")
                        DetailRow("شماره دستور", spec?.planInstructionNo ?: "")
                        DetailRow("تاریخ دستور", spec?.planInstructionDate ?: "")
                        DetailRow("نوع سازه", spec?.structureType ?: "")
                        DetailRow("مساحت زمین", spec?.landArea ?: "")
                        DetailRow("متراژ پاراف", spec?.parafArea ?: "")
                        DetailRow("عنوان بلوک", spec?.blockTitle ?: "")
                        DetailRow("تعداد طبقات", spec?.floors ?: "")
                        DetailRow("تعداد واحد", spec?.units ?: "")
                        DetailRow("شماره پروانه", spec?.permitNumber ?: "")
                        DetailRow("تاریخ صدور", spec?.permitDate ?: "")
                        DetailRow("نوع کاربری", spec?.usageType ?: "")
                    }
                }
            }

            // Engineers
            if (engineers.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("مهندسین (${engineers.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(engineers) { eng ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(eng.name, fontWeight = FontWeight.Bold)
                            Text("رشته: ${eng.discipline}", fontSize = 13.sp)
                            if (eng.role.isNotBlank()) {
                                Text("نقش: ${eng.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Fees
            if (fees.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("حق‌الزحمه (${fees.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(fees) { fee ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${fee.discipline} - ${fee.serviceType}", fontWeight = FontWeight.Bold)
                            Text("مرحله: ${fee.stage} | مبلغ: ${fee.amount}", fontSize = 13.sp)
                            Text("وضعیت: ${fee.payStatus}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Reports
            if (reports.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("گزارش‌ها (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(reports) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${report.reportType} - مرحله ${report.stage}", fontWeight = FontWeight.Bold)
                            Text("مهندس: ${report.engineer}", fontSize = 13.sp)
                            Text("تاریخ: ${report.visitDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
