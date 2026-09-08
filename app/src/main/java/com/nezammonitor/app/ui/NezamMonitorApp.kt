package com.nezammonitor.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
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
    
    // Import launcher
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
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
                title = { Text("سیستم مانیتور نظم مهندسی", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                actions = {
                    IconButton(onClick = { 
                        importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    }) {
                        Icon(Icons.Default.Upload, contentDescription = "ورود اطلاعات")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("پرونده‌ها") },
                    selected = currentScreen == "cases",
                    onClick = { currentScreen = "cases" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("مهندسین") },
                    selected = currentScreen == "engineers",
                    onClick = { currentScreen = "engineers" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    label = { Text("حق‌الزحمه") },
                    selected = currentScreen == "fees",
                    onClick = { currentScreen = "fees" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Article, contentDescription = null) },
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
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("جستجو...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )
            
            // Import result message
            importResult?.let { result ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = result.message,
                        modifier = Modifier.padding(16.dp),
                        color = if (result.success) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Content based on current screen
            when (currentScreen) {
                "cases" -> CasesScreen(dao, searchQuery)
                "engineers" -> EngineersScreen(dao, searchQuery)
                "fees" -> FeesScreen(dao, searchQuery)
                "reports" -> ReportsScreen(dao, searchQuery)
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "پرونده: ${case.caseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "مالک: ${case.owner}")
                    Text(text = "آدرس: ${case.serial}")
                    
                    // Phone number - clickable
                    if (case.ownerMobile.isNotBlank()) {
                        Text(
                            text = "تلفن: ${case.ownerMobile}",
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${case.ownerMobile}")
                                }
                                // context.startActivity(intent)
                            },
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EngineersScreen(dao: NezamDao, searchQuery: String) {
    val engineers by dao.getAllEngineers().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(engineers) { engineer ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = engineer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "رشته: ${engineer.discipline}")
                    Text(text = "نقش: ${engineer.role}")
                }
            }
        }
    }
}

@Composable
fun FeesScreen(dao: NezamDao, searchQuery: String) {
    val fees by dao.getAllFees().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(fees) { fee ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "پرونده: ${fee.caseNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "مبلغ: ${fee.amount}")
                    Text(text = "مرحله: ${fee.stage}")
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(dao: NezamDao, searchQuery: String) {
    val reports by dao.getAllReports().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "گزارش: ${report.reportType}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "مرحله: ${report.stage}")
                    Text(text = "تاریخ بازدید: ${report.visitDate}")
                }
            }
        }
    }
}