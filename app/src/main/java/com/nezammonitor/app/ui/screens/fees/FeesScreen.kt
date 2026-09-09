package com.nezammonitor.app.ui.screens.fees

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(viewModel: MainViewModel) {
    val fees by viewModel.fees.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredFees = remember(fees, searchQuery) {
        if (searchQuery.isBlank()) fees
        else fees.filter {
            it.caseNumber.contains(searchQuery, true) ||
            it.discipline.contains(searchQuery, true) ||
            it.serviceType.contains(searchQuery, true) ||
            it.amount.contains(searchQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی حق‌الزحمه...") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredFees, key = { "${it.caseNumber}_${it.discipline}_${it.stage}_${it.amountType}" }) { fee ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fee.caseNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(fee.amount, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مرحله: ${fee.stage} | رشته: ${fee.discipline}", fontSize = 13.sp)
                        Text("خدمت: ${fee.serviceType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("وضعیت: ${fee.payStatus} | تأیید: ${fee.confirmStatus}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
