package com.nezammonitor.app.ui.screens.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import com.nezammonitor.app.data.SinaEngineerStore
import com.nezammonitor.app.data.formatSinaLevels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ReportsScreen(viewModel: MainViewModel, onCaseClick: (String) -> Unit = {}) {
    val cases by viewModel.cases.collectAsState()
    val reports by viewModel.reports.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val groups = remember(cases, reports, searchQuery) {
        cases.map { case ->
            val caseReports = reports.filter { it.caseNumber == case.caseNumber }
            Pair(case, caseReports)
        }.filter { (_, reps) -> reps.isNotEmpty() }
         .filter { (case, _) -> searchQuery.isBlank() || case.caseNumber.contains(searchQuery, true) || case.owner.contains(searchQuery, true) }
         .sortedBy { it.first.caseNumber }
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
            Text("${groups.size} پرونده | ${reports.size} گزارش", modifier = Modifier.padding(12.dp), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(groups, key = { it.first.caseNumber }) { (case, caseReports) ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onCaseClick(case.caseNumber) }) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(case.owner, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("پرونده: ${case.caseNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Text("${caseReports.size} گزارش", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        caseReports.take(2).forEach { rep ->
                            Text("• ${rep.reportType} (${rep.stage}) - ${rep.visitDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (caseReports.size > 2) Text("+ ${caseReports.size - 2} گزارش دیگر", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
