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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(db: NezamDatabase) {
    val dao = db.nezamDao()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("home") }
    var importMessage by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val engine = ImportEngine(context)
            val result = kotlinx.coroutines.runBlocking {
                engine.importFromUri(it)
            }
            importMessage = if (result.success) {
                "موفق: ${result.casesCount} پرونده، ${result.feesCount} حق‌الزحمه، ${result.reportsCount} گزارش"
            } else {
                "خطا: ${result.message}"
            }
            currentScreen = "home"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("سیستم مانیتور نظام مهندسی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("ساخته شده توسط مهندس برخورداری", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    if (currentScreen != "home") {
                        IconButton(onClick = { currentScreen = "home" }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen == "home") {
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentScreen = "cases" },
                        icon = { Text("📋") },
                        label = { Text("پرونده‌ها") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentScreen = "engineers" },
                        icon = { Text("👷") },
                        label = { Text("مهندسین") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentScreen = "fees" },
                        icon = { Text("💰") },
                        label = { Text("حق‌الزحمه") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentScreen = "reports" },
                        icon = { Text("📄") },
                        label = { Text("گزارش‌ها") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { currentScreen = "followup" },
                        icon = { Text("📌") },
                        label = { Text("پیگیری") }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    onImportClick = {
                        importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                    },
                    importMessage = importMessage,
                    dao = dao
                )
                "cases" -> CasesScreen(dao)
                "engineers" -> EngineersScreen(dao)
                "fees" -> FeesScreen(dao)
                "reports" -> ReportsScreen(dao)
                "followup" -> FollowUpScreen(dao)
            }
        }
    }
}

@Composable
fun HomeScreen(onImportClick: () -> Unit, importMessage: String, dao: NezamDao) {
    val casesCount by dao.getCasesCount().collectAsState(initial = 0)
    val engineersCount by dao.getEngineersCount().collectAsState(initial = 0)
    val feesCount by dao.getFeesCount().collectAsState(initial = 0)
    val reportsCount by dao.getReportsCount().collectAsState(initial = 0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("خوش آمدید", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Import button
        Button(
            onClick = onImportClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("ورود اطلاعات از ویندوز", modifier = Modifier.padding(8.dp), fontSize = 16.sp)
        }

        // Import message
        if (importMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (importMessage.startsWith("موفق"))
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = importMessage,
                    modifier = Modifier.padding(12.dp),
                    color = if (importMessage.startsWith("موفق"))
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Stats
        Text("آمار داده‌ها", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("📋 پرونده‌ها", casesCount)
            StatCard("👷 مهندسین", engineersCount)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCard("💰 حق‌الزحمه", feesCount)
            StatCard("📄 گزارش‌ها", reportsCount)
        }
    }
}

@Composable
fun StatCard(title: String, count: Int) {
    Card(modifier = Modifier.width(150.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("$count", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CasesScreen(dao: NezamDao) {
    val cases by dao.getAllCases().collectAsState(initial = emptyList())
    val context = LocalContext.current
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
                        Text(
                            text = "تلفن: ${case.ownerMobile}",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:${case.ownerMobile}")
                                context.startActivity(intent)
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

@Composable
fun FollowUpScreen(dao: NezamDao) {
    val edits by dao.getAllFollowUpEdits().collectAsState(initial = emptyList())
    val cases by dao.getAllCases().collectAsState(initial = emptyList())

    val followUpItems = remember(edits, cases) {
        cases.map { case ->
            val edit = edits.find { it.caseNumber == case.caseNumber }
            FollowUpDisplayItem(
                caseNumber = edit?.caseNumberEdit?.ifBlank { case.caseNumber } ?: case.caseNumber,
                owner = edit?.ownerEdit?.ifBlank { case.owner } ?: case.owner,
                description = edit?.description ?: "",
                hasEdit = edit != null
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(followUpItems) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("پرونده: ${item.caseNumber}", fontWeight = FontWeight.Bold)
                    Text("مالک: ${item.owner}")
                    if (item.description.isNotBlank()) {
                        Text("توضیحات: ${item.description}", color = MaterialTheme.colorScheme.tertiary)
                    }
                }
            }
        }
    }
}

data class FollowUpDisplayItem(
    val caseNumber: String,
    val owner: String,
    val description: String,
    val hasEdit: Boolean
)
