package com.nezammonitor.app.ui.screens.reports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
fun ReportsScreen(viewModel: MainViewModel) {
    val reports by viewModel.reports.collectAsState()
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filtered = remember(reports, cases, searchQuery) {
        if (searchQuery.isBlank()) reports
        else reports.filter {
            it.caseNumber.contains(searchQuery, true) ||
            it.reportType.contains(searchQuery, true) ||
            it.engineer.contains(searchQuery, true) ||
            it.discipline.contains(searchQuery, true) ||
            cases.find { c -> c.caseNumber == it.caseNumber }?.owner?.contains(searchQuery, true) == true
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی گزارش / مهندس / پرونده...") },
            singleLine = true
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                "${filtered.size} گزارش",
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { "${it.caseNumber}_${it.stage}_${it.visitDate}_${it.rowNo}" }) { report ->
                val case = cases.find { it.caseNumber == report.caseNumber }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(report.reportType, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("مرحله ${report.stage}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            if (case?.ownerMobile?.isNotBlank() == true) {
                                IconButton(onClick = {
                                    val cleaned = case.ownerMobile.replace(Regex("[^0-9+]"), "")
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$cleaned")
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Phone, contentDescription = "تماس", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("پرونده: ${report.caseNumber}", fontSize = 12.sp)
                            Text("مالک: ${case?.owner ?: "-"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("مهندس: ${report.engineer} | رشته: ${report.discipline}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("تاریخ: ${report.visitDate}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
