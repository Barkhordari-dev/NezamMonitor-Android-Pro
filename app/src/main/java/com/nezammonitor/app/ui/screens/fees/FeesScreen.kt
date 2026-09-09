package com.nezammonitor.app.ui.screens.fees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.data.FeeEntity
import com.nezammonitor.app.viewmodel.MainViewModel

data class FeesGroup(
    val caseNumber: String,
    val owner: String,
    val fees: List<FeeEntity>,
    val totalAmount: Long,
    val paidAmount: Long,
    val confirmedAmount: Long,
    val pendingAmount: Long,
    val totalDisplay: String,
    val paidDisplay: String,
    val confirmedDisplay: String,
    val pendingDisplay: String
)

/** Mirror Windows FeesViewModel.ParseAmount */
private fun parseAmount(amount: String): Long {
    if (amount.isBlank()) return 0L
    val cleaned = amount
        .replace("ریال", "")
        .replace(",", "")
        .replace(" ", "")
        .trim()
        .normalizePersianDigits()
    return cleaned.toLongOrNull() ?: 0L
}

private fun String.normalizePersianDigits(): String {
    return this
        .replace("۰", "0").replace("۱", "1").replace("۲", "2")
        .replace("۳", "3").replace("۴", "4").replace("۵", "5")
        .replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9")
        .replace("٠", "0").replace("١", "1").replace("٢", "2")
        .replace("٣", "3").replace("٤", "4").replace("٥", "5")
        .replace("٦", "6").replace("٧", "7").replace("٨", "8").replace("٩", "9")
}

/** Mirror Windows FeesViewModel.FormatAmount */
private fun formatAmount(amount: Long): String {
    return "%,d".format(amount) + " ریال"
}

/** Mirror Windows CalculateSummary logic */
private fun calculateSummary(fees: List<FeeEntity>): Long {
    var total = 0L
    fees.forEach { fee -> total += parseAmount(fee.amount) }
    return total
}

private fun calculatePaid(fees: List<FeeEntity>): Long {
    var paid = 0L
    fees.forEach { fee ->
        if (fee.payStatus.contains("پرداخت")) paid += parseAmount(fee.amount)
    }
    return paid
}

private fun calculateConfirmed(fees: List<FeeEntity>): Long {
    var confirmed = 0L
    fees.forEach { fee ->
        if (fee.payStatus.contains("تایید") || fee.confirmStatus.contains("تایید"))
            confirmed += parseAmount(fee.amount)
    }
    return confirmed
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(viewModel: MainViewModel) {
    val fees by viewModel.fees.collectAsState()
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Calculate global summary (mirror Windows)
    val globalTotal = remember(fees) { calculateSummary(fees) }
    val globalPaid = remember(fees) { calculatePaid(fees) }
    val globalConfirmed = remember(fees) { calculateConfirmed(fees) }
    val globalPending = remember(fees) { globalTotal - globalPaid - globalConfirmed }

    val groups = remember(fees, cases, searchQuery) {
        val grouped = mutableMapOf<String, MutableList<FeeEntity>>()
        fees.forEach { fee ->
            grouped.getOrPut(fee.caseNumber) { mutableListOf() }.add(fee)
        }
        val result = grouped.map { (caseNum, caseFees) ->
            val case = cases.find { it.caseNumber == caseNum }
            val total = calculateSummary(caseFees)
            val paid = calculatePaid(caseFees)
            val confirmed = calculateConfirmed(caseFees)
            val pending = total - paid - confirmed
            FeesGroup(
                caseNumber = caseNum,
                owner = case?.owner ?: "",
                fees = caseFees,
                totalAmount = total, paidAmount = paid,
                confirmedAmount = confirmed, pendingAmount = pending,
                totalDisplay = formatAmount(total),
                paidDisplay = formatAmount(paid),
                confirmedDisplay = formatAmount(confirmed),
                pendingDisplay = formatAmount(pending)
            )
        }.sortedBy { it.caseNumber }

        if (searchQuery.isBlank()) result
        else result.filter { group ->
            group.caseNumber.contains(searchQuery, true) ||
            group.owner.contains(searchQuery, true) ||
            group.fees.any {
                it.discipline.contains(searchQuery, true) ||
                it.serviceType.contains(searchQuery, true)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی مالک / پرونده / رشته...") },
            singleLine = true
        )

        // Global Summary (mirror Windows CalculateSummary)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${groups.size} پرونده", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${fees.size} ردیف حق\u200Cالزحمه", fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("جمع کل:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(globalTotal.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("پرداخت:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatAmount(globalPaid), fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("تأیید:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatAmount(globalConfirmed), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("در انتظار:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatAmount(globalPending), fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups, key = { it.caseNumber }) { group ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(group.caseNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                if (group.owner.isNotBlank()) {
                                    Text("مالک: ${group.owner}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(group.totalDisplay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // Sub-totals
                        if (group.paidAmount > 0 || group.confirmedAmount > 0 || group.pendingAmount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (group.paidAmount > 0) Text("پرداخت: ${group.paidDisplay}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                if (group.confirmedAmount > 0) Text("تأیید: ${group.confirmedDisplay}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                if (group.pendingAmount > 0) Text("در انتظار: ${group.pendingDisplay}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(4.dp))

                        // Fee rows
                        group.fees.forEach { fee ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${fee.discipline} - ${fee.serviceType}", fontSize = 13.sp)
                                    Text("مرحله: ${fee.stage}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(fee.amount, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(fee.payStatus, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
