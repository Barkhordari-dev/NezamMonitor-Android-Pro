package com.nezammonitor.app.ui.screens.reports

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
fun ReportsScreen(viewModel: MainViewModel) {
    val reports by viewModel.reports.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredReports = remember(reports, searchQuery) {
        if (searchQuery.isBlank()) reports
        else reports.filter {
            it.caseNumber.contains(searchQuery, true) ||
            it.reportType.contains(searchQuery, true) ||
            it.engineer.contains(searchQuery, true) ||
            it.discipline.contains(searchQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی گزارش...") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredReports, key = { "${it.caseNumber}_${it.stage}_${it.visitDate}" }) { report ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(report.reportType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("مرحله ${report.stage}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("پرونده: ${report.caseNumber}", fontSize = 13.sp)
                        Text("مهندس: ${report.engineer}", fontSize = 12.sp)
                        Text("رشته: ${report.discipline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("تاریخ: ${report.visitDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
