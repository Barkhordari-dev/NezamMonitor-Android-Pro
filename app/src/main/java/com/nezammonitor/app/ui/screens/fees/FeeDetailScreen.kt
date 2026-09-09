package com.nezammonitor.app.ui.screens.fees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

/** Mirror Windows FeesViewModel.ParseAmount */
private fun parseAmount(amount: String): Long {
    if (amount.isBlank()) return 0L
    val cleaned = amount
        .replace("ریال", "").replace(",", "").replace(" ", "").trim()
        .replace("۰","0").replace("۱","1").replace("۲","2").replace("۳","3")
        .replace("۴","4").replace("۵","5").replace("۶","6").replace("۷","7")
        .replace("۸","8").replace("۹","9").replace("٠","0").replace("١","1")
        .replace("٢","2").replace("٣","3").replace("٤","4").replace("٥","5")
        .replace("٦","6").replace("٧","7").replace("٨","8").replace("٩","9")
    return cleaned.toLongOrNull() ?: 0L
}

private fun formatAmount(amount: Long): String = "%,d".format(amount) + " ریال"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val case by viewModel.caseDetail.collectAsState()
    val fees by viewModel.caseFees.collectAsState()

    val totalAmount = remember(fees) { fees.sumOf { parseAmount(it.amount) } }
    val paidAmount = remember(fees) { fees.filter { it.payStatus.contains("پرداخت") }.sumOf { parseAmount(it.amount) } }
    val confirmedAmount = remember(fees) { fees.filter { it.payStatus.contains("تایید") || it.confirmStatus.contains("تایید") }.sumOf { parseAmount(it.amount) } }
    val pendingAmount = remember(fees) { totalAmount - paidAmount - confirmedAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("حق\u200Cالزحمه پرونده", fontSize = 16.sp)
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
            // Case header
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مالک: ${case?.owner ?: ""}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("پرونده: ${case?.caseNumber ?: ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Fee summary
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("خلاصه حق\u200Cالزحمه", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("جمع کل:", fontSize = 13.sp); Text(formatAmount(totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("پرداخت:", fontSize = 13.sp); Text(formatAmount(paidAmount), color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("تأیید:", fontSize = 13.sp); Text(formatAmount(confirmedAmount), color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("در انتظار:", fontSize = 13.sp); Text(formatAmount(pendingAmount), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                        }
                        Text("${fees.size} ردیف", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            // Fee details
            items(fees) { fee ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${fee.discipline} - ${fee.serviceType}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(fee.amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مرحله: ${fee.stage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("پرداخت: ${fee.payStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("تأیید: ${fee.confirmStatus}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (fee.description.isNotBlank()) {
                            Text("توضیحات: ${fee.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            if (fees.isEmpty()) {
                item { Text("حق\u200Cالزحمه‌ای یافت نشد", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp)) }
            }
        }
    }
}
