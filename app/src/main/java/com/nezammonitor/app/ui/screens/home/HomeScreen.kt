package com.nezammonitor.app.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(viewModel: MainViewModel, onNavigateToImport: () -> Unit) {
    val context = LocalContext.current
    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    val fees by viewModel.fees.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                val engine = ImportEngine(context)
                val result = withContext(Dispatchers.IO) {
                    engine.importFromUri(it)
                }
                viewModel.importData(result)
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    modifier = Modifier
                        .clickable {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📥", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ورود اطلاعات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                    Text("از ویندوز", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        if (importResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (importResult?.success == true)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = importResult?.message ?: "",
                        modifier = Modifier.padding(12.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }

        item { StatCard("📋 پرونده‌ها", cases.size, MaterialTheme.colorScheme.primary) }
        item { StatCard("👷 مهندسین", engineers.size, MaterialTheme.colorScheme.secondary) }
        item { StatCard("💰 حق‌الزحمه", fees.size, MaterialTheme.colorScheme.tertiary) }
        item { StatCard("📄 گزارش‌ها", reports.size, MaterialTheme.colorScheme.error) }
    }
}

@Composable
fun StatCard(title: String, count: Int, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
