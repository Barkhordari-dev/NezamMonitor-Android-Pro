package com.nezammonitor.app.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nezammonitor.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(db: NezamDatabase) {
    val dao = db.nezamDao()

    var currentScreen by remember { mutableStateOf("cases") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سیستم مانیtor نظم مهندسی", fontWeight = FontWeight.Bold) }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "cases",
                    onClick = { currentScreen = "cases" },
                    icon = { Text("📋") },
                    label = { Text("پرونده‌ها") }
                )
                NavigationBarItem(
                    selected = currentScreen == "engineers",
                    onClick = { currentScreen = "engineers" },
                    icon = { Text("👷") },
                    label = { Text("مهندسین") }
                )
                NavigationBarItem(
                    selected = currentScreen == "fees",
                    onClick = { currentScreen = "fees" },
                    icon = { Text("💰") },
                    label = { Text("حق‌الزحمه") }
                )
                NavigationBarItem(
                    selected = currentScreen == "reports",
                    onClick = { currentScreen = "reports" },
                    icon = { Text("📄") },
                    label = { Text("گزارش‌ها") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    Text("پرونده: ${case.caseNumber}", fontWeight = FontWeight.Bold)
                    Text("مالک: ${case.owner}")
                    Text("آدرس: ${case.serial}")
                    if (case.ownerMobile.isNotBlank()) {
                        Text("تلفن: ${case.ownerMobile}", color = MaterialTheme.colorScheme.primary)
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
        items(engineers) { eng ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(eng.name, fontWeight = FontWeight.Bold)
                    Text("رشته: ${eng.discipline}")
                    Text("نقش: ${eng.role}")
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
                    Text("پرونده: ${fee.caseNumber}", fontWeight = FontWeight.Bold)
                    Text("مبلغ: ${fee.amount}")
                    Text("مرحله: ${fee.stage}")
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
        items(reports) { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("گزارش: ${r.reportType}", fontWeight = FontWeight.Bold)
                    Text("مرحله: ${r.stage}")
                    Text("تاریخ: ${r.visitDate}")
                }
            }
        }
    }
}