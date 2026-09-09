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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nezammonitor.app.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(db: NezamDatabase) {
    val dao = db.nezamDao()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("cases") }
    var importMessage by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val engine = ImportEngine(context)
            val result = kotlinx.coroutines.runBlocking {
                engine.importFromUri(it)
            }
            importMessage = if (result.success) {
                "✅ موفق: ${result.casesCount} پرونده، ${result.feesCount} حق‌الزحمه، ${result.reportsCount} گزارش"
            } else {
                "❌ خطا: ${result.message}"
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
                NavigationBarItem(
                    selected = currentScreen == "followup",
                    onClick = { currentScreen = "followup" },
                    icon = { Text("📌") },
                    label = { Text("پیگیری") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Import button
            Button(
                onClick = {
                    importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("📱 وارد کردن اطلاعات از ویندوز", modifier = Modifier.padding(8.dp))
            }

            // Import message
            if (importMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (importMessage.startsWith("✅"))
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = importMessage,
                        modifier = Modifier.padding(12.dp),
                        color = if (importMessage.startsWith("✅"))
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (currentScreen) {
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
fun CasesScreen(dao: NezamDao) {
    val cases by dao.getAllCases().collectAsState(initial = emptyList())
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
                                // context.startActivity(intent)
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

    // Build follow-up items: for each case, check if there's an edit
    val followUpItems = remember(edits, cases) {
        cases.map { case ->
            val edit = edits.find { it.caseNumber == case.caseNumber }
            FollowUpDisplayItem(
                caseNumber = edit?.caseNumberEdit?.ifBlank { case.caseNumber } ?: case.caseNumber,
                owner = edit?.ownerEdit?.ifBlank { case.owner } ?: case.owner,
                address = edit?.addressEdit?.ifBlank { "" } ?: "",
                mobile = edit?.ownerMobileEdit?.ifBlank { case.ownerMobile } ?: case.ownerMobile,
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
                        Text(
                            text = "توضیحات: ${item.description}",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

data class FollowUpDisplayItem(
    val caseNumber: String,
    val owner: String,
    val address: String,
    val mobile: String,
    val description: String,
    val hasEdit: Boolean
)