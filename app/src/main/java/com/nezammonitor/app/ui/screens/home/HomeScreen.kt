package com.nezammonitor.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.data.ImportEngine
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToImport: () -> Unit = {},
    onNavigateToCases: () -> Unit = {},
    onNavigateToEngineers: () -> Unit = {},
    onNavigateToFees: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    onNavigateToFollowUp: () -> Unit = {},
    onNavigateToExport: () -> Unit = {},
    onNavigateToUpdate: () -> Unit = {},
    onNavigateToReportGenerator: () -> Unit = {},
    onNavigateToReportHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    val fees by viewModel.fees.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                viewModel.setImporting(true)
                val engine = ImportEngine(context)
                val result = withContext(Dispatchers.IO) {
                    engine.importFromUri(it)
                }
                viewModel.importData(result)
                viewModel.setImporting(false)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Import + Export
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .clickable(enabled = !isImporting) {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream", "*/*"))
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(if (isImporting) "⏳" else "📥", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("ورود اطلاعات", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToExport() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📤", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("خروجی برای ویندوز", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiary)
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToUpdate() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("uD83DuDD04", fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("بروزرسانی", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        // Import result
        if (importResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (importResult?.success == true)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (importResult?.success == true) "✅ ${importResult?.message}" else "❌ ${importResult?.message}",
                            fontSize = 12.sp
                        )
                        TextButton(onClick = { viewModel.clearImportResult() }) {
                            Text("بستن")
                        }
                    }
                }
            }
        }

        // Row 2: Cases (clickable)
        item {
            StatCard("📋 پرونده\u200Cها", cases.size, MaterialTheme.colorScheme.primary, onClick = onNavigateToCases)
        }
        item {
            StatCard("👷 مهندسین", engineers.size, MaterialTheme.colorScheme.secondary, onClick = onNavigateToEngineers)
        }
        item {
            StatCard("💰 حق\u200Cالزحمه", fees.size, MaterialTheme.colorScheme.tertiary, onClick = onNavigateToFees)
        }
        item {
            StatCard("📄 گزارش\u200Cها", reports.size, MaterialTheme.colorScheme.error, onClick = onNavigateToReports)
        }

        // Row 3: FollowUp quick link
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToFollowUp() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📌", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پیگیری\u200Cها", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Row 4: Report Generator
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToReportGenerator() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تولید گزارش", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Row 5: Report History
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToReportHistory() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📦", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سابقه گزارش\u200Cها", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Row 6: Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToSettings() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚙️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنظیمات", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("$count", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
