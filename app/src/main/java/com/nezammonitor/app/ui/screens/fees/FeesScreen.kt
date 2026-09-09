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
    val totalAmount: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(viewModel: MainViewModel) {
    val fees by viewModel.fees.collectAsState()
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val groups = remember(fees, cases, searchQuery) {
        val grouped = mutableMapOf<String, MutableList<FeeEntity>>()
        fees.forEach { fee ->
            grouped.getOrPut(fee.caseNumber) { mutableListOf() }.add(fee)
        }
        val result = grouped.map { (caseNum, caseFees) ->
            val case = cases.find { it.caseNumber == caseNum }
            val total = caseFees.sumOf {
                try { it.amount.replace(",", "").replace("\u200C", "").trim().toLong() } catch (e: Exception) { 0L }
            }
            FeesGroup(
                caseNumber = caseNum,
                owner = case?.owner ?: "",
                fees = caseFees,
                totalAmount = String.format("%,d", total)
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

        // Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${groups.size} پرونده", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("${fees.size} ردیف حق\u200Cالزحمه", fontSize = 13.sp)
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
                            Text("جمع: ${group.totalAmount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
