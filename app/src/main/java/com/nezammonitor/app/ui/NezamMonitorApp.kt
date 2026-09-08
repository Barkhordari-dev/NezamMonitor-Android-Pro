package com.nezammonitor.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import com.nezammonitor.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp() {
    val context = LocalContext.current
    val db = remember { NezamDatabase.getDatabase(context) }
    val dao = db.nezamDao()
    
    var currentScreen by remember { mutableStateOf("cases") }
    var searchQuery by remember { mutableStateOf("") }
    var importResult by remember { mutableStateOf<ImportResult?>(null) }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val engine = ImportEngine(context)
            importResult = kotlinx.coroutines.runBlocking {
                engine.importFromUri(it)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سیستم مانیتور نظم مهندسی", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Text("📋") },
                    label = { Text("پرونده‌ها") },
                    selected = currentScreen == "cases",
                    onClick = { currentScreen = "cases" }
                )
                NavigationBarItem(
                    icon = { Text("👷") },
                    label = { Text("مهندسین") },
                    selected = currentScreen == "engineers",
                    onClick = { currentScreen = "engineers" }
                )
                NavigationBarItem(
                    icon = { Text("💰") },
                    label = { Text("حق‌الزحمه") },
                    selected = currentScreen == "fees",
                    onClick = { currentScreen = "fees" }
                )
                NavigationBarItem(
                    icon = { Text("📄") },
                    label = { Text("گزارش‌ها") },
                    selected = currentScreen == "reports",
                    onClick = { currentScreen = "reports" }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )
            
            importResult?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            when (currentScreen) {
                "cases" -> CasesScreen(dao, searchQuery)
                "engineers" -> EngineersScreen(dao)
                "fees" -> FeesScreen(dao)
                "reports" -> ReportsScreen(dao)
            }
        }
    }
}

@Composable
fun CasesScreen(dao: NezamDao, searchQuery: String) {
    val cases by if (searchQuery.isBlank()) {
        dao.getAllCases().collectAsState(initial = emptyList())
    } else {
        dao.searchCases(searchQuery).collectAsState(initial = emptyList())
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cases) { case ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "پرونده: ${case.caseNumber}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "مالک: ${case.owner}")
                    Text(text = "آدرس: ${case.serial}")
                    if (case.ownerMobile.isNotBlank()) {
                        Text(
                            text = "تلفن: ${case.ownerMobile}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:${case.ownerMobile}")
                                // context.startActivity(intent) // Will work on device
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineersScreen(dao: NezamDao) {
    val engineers by dao.getAllEngineers().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(engineers) { engineer ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = engineer.name, fontWeight = FontWeight.Bold)
                    Text(text = "رشته: ${engineer.discipline}")
                    Text(text = "نقش: ${engineer.role}")
                }
            }
        }
    }
}

@Composable
fun FeesScreen(dao: NezamDao) {
    val fees by dao.getAllFees().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(fees) { fee ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "پرونده: ${fee.caseNumber}", fontWeight = FontWeight.Bold)
                    Text(text = "مبلغ: ${fee.amount}")
                    Text(text = "مرحله: ${fee.stage}")
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(dao: NezamDao) {
    val reports by dao.getAllReports().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reports) { report ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "گزارش: ${report.reportType}", fontWeight = FontWeight.Bold)
                    Text(text = "مرحله: ${report.stage}")
                    Text(text = "تاریخ بازدید: ${report.visitDate}")
                }
            }
        }
    }
}
