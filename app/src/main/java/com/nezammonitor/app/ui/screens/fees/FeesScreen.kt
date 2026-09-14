package com.nezammonitor.app.ui.screens.fees

import androidx.compose.foundation.clickable
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

data class FeeCaseGroup(
    val caseNumber: String,
    val owner: String,
    val fees: List<FeeEntity>,
    val totalAmount: Long,
    val paidAmount: Long,
    val confirmedAmount: Long,
    val pendingAmount: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(viewModel: MainViewModel, onCaseClick: (String) -> Unit = {}) {
    val fees by viewModel.fees.collectAsState()
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCaseNumber by remember { mutableStateOf<String?>(null) }
    var selectedFees by remember { mutableStateOf(setOf<Long>()) }

    // Group fees by case with correct ordering (API order preserved by DB)
    val groups = remember(fees, cases, searchQuery) {
        val grouped = mutableMapOf<String, MutableList<FeeEntity>>()
        fees.forEach { grouped.getOrPut(it.caseNumber) { mutableListOf() }.add(it) }
        grouped.map { (caseNum, caseFees) ->
            val case = cases.find { it.caseNumber == caseNum }
            val total = caseFees.sumOf { parseAmount(it.amount) }
            val paid = caseFees.filter { it.payStatus.contains("پرداخت") }.sumOf { parseAmount(it.amount) }
            val confirmed = caseFees.filter { it.payStatus.contains("تایید") || it.confirmStatus.contains("تایید") }.sumOf { parseAmount(it.amount) }
            FeeCaseGroup(caseNum, case?.owner ?: "", caseFees, total, paid, confirmed, total - paid - confirmed)
        }.filter { g -> searchQuery.isBlank() || g.caseNumber.contains(searchQuery, true) || g.owner.contains(searchQuery, true) }
    }

    // Calculate selected total
    val selectedTotal = remember(fees, selectedFees) {
        fees.filter { it.id in selectedFees }.sumOf { parseAmount(it.amount) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("جستجوی پرونده / مالک...") },
            singleLine = true
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${groups.size} پرونده | ${fees.size} ردیف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (selectedFees.isNotEmpty()) {
                    Text("انتخاب: ${selectedFees.size} | ${formatAmount(selectedTotal)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                val isExpanded = selectedCaseNumber == group.caseNumber
                Card(modifier = Modifier.fillMaxWidth().clickable {
                    selectedCaseNumber = if (isExpanded) null else group.caseNumber
                }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(group.owner, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("پرونده: ${group.caseNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatAmount(group.totalAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                Text("${group.fees.size} ردیف", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (group.paidAmount > 0) Text("پرداخت: ${formatAmount(group.paidAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            if (group.confirmedAmount > 0) Text("تأیید: ${formatAmount(group.confirmedAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            if (group.pendingAmount > 0) Text("انتظار: ${formatAmount(group.pendingAmount)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
                        }

                        // Expanded fee details
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(4.dp))
                            group.fees.forEach { fee ->
                                val isSelected = fee.id in selectedFees
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedFees = if (checked) selectedFees + fee.id else selectedFees - fee.id
                                        }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${fee.discipline} - ${fee.amountType}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        Text("مرحله: ${fee.stage} | ${fee.payStatus}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(fee.amount, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Select all for this case
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("انتخاب همه", fontSize = 11.sp)
                                Checkbox(
                                    checked = group.fees.all { it.id in selectedFees },
                                    onCheckedChange = { checked ->
                                        selectedFees = if (checked)
                                            selectedFees + group.fees.map { it.id }.toSet()
                                        else
                                            selectedFees - group.fees.map { it.id }.toSet()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
