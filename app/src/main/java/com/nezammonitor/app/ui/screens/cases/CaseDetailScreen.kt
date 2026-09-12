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

    fun dialPhone(number: String) {
        if (number.isNotBlank()) {
            val cleaned = number.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$cleaned")
            context.startActivity(intent)
        }
    }

    if (case == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(case?.caseNumber ?: "جزئیات پرونده", fontSize = 16.sp) },
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
                        DetailRow("نام پدر", case?.ownerFather ?: "")
                        DetailRow("کدملی", case?.ownerNationalCode ?: "")
                        DetailRow("آدرس مالک", case?.ownerAddress ?: "")
                        DetailRow("کدپستی", case?.ownerZip ?: "")
                        DetailRow("تلفن", case?.ownerTel ?: "")
                        DetailRow("زادگاه", case?.ownerBirthLoc ?: "")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("همراه: ${case?.ownerMobile ?: ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (case?.ownerMobile?.isNotBlank() == true) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "تماس",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { dialPhone(case?.ownerMobile ?: "") }
                                )
                            }
                        }
                        DetailRow("دفتر", case?.office ?: "")
                        DetailRow("مسئولیت", case?.responsibility ?: "")
                        DetailRow("تاریخ کسر ظرفیت", case?.capacityDate ?: "")
                        DetailRow("نوع کار", case?.workType ?: "")
                        DetailRow("رشته", case?.discipline ?: "")
                    }
                }
            }

            // Building Specs
            if (spec != null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("مشخصات ساختمان", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow("آدرس", spec?.address ?: "")
                            DetailRow("گروه ساختمانی", spec?.buildingGroup ?: "")
                            DetailRow("کد نوسازی", spec?.renovationCode ?: "")
                            DetailRow("نوع دستور", spec?.planInstructionType ?: "")
                            DetailRow("شماره دستور", spec?.planInstructionNo ?: "")
                            DetailRow("تاریخ دستور", spec?.planInstructionDate ?: "")
                            DetailRow("نوع سازه", spec?.structureType ?: "")
                            DetailRow("مساحت زمین", spec?.landArea ?: "")
                            DetailRow("متراژ نظارت", spec?.capacityArea ?: "")
                            DetailRow("متراژ شهرداری", case?.municipalArea ?: "")
                            DetailRow("عنوان بلوک", spec?.blockTitle ?: "")
                            DetailRow("تعداد بلوک", spec?.blockCount ?: "")
                            DetailRow("تعداد طبقات", spec?.floors ?: "")
                            DetailRow("تعداد واحد", spec?.units ?: "")
                            DetailRow("صادرکننده", spec?.issuer ?: "")
                            DetailRow("شماره پروانه", spec?.permitNumber ?: "")
                            DetailRow("تاریخ صدور", spec?.permitDate ?: "")
                            DetailRow("تاریخ رفع اثر", spec?.releaseDate ?: "")
                            DetailRow("ناحیه نقشه", spec?.planZone ?: "")
                            DetailRow("نوع کاربری", spec?.usageType ?: "")
                        }
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
                            Text(eng.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                            Text("حق\u200Cالزحمه (${fees.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(fees) { fee ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${fee.discipline} - ${fee.serviceType}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(fee.amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("مرحله: ${fee.stage}", fontSize = 12.sp)
                            Text("وضعیت: ${fee.payStatus} | تأیید: ${fee.confirmStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (fee.description.isNotBlank()) {
                                Text("توضیحات: ${fee.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Reports
            if (reports.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("گزارش\u200Cها (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                items(reports) { report ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${report.reportType}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("مرحله ${report.stage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("مهندس: ${report.engineer} | رشته: ${report.discipline}", fontSize = 12.sp)
                            Text("تاریخ: ${report.visitDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Empty state
            if (engineers.isEmpty() && fees.isEmpty() && reports.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "اطلاعات تکمیلی یافت نشد",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                .padding(vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
