package com.nezammonitor.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.nezammonitor.app.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NezamMonitorApp(db: NezamDatabase) {
    val dao = db.nezamDao()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("home") }
    var importMessage by remember { mutableStateOf("") }
    var isRtl by remember { mutableStateOf(true) }

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "منوی اصلی",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Text("🏠") },
                    label = { Text("خانه") },
                    selected = currentScreen == "home",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "home"
                    }
                )
                NavigationDrawerItem(
                    icon = { Text("📋") },
                    label = { Text("پرونده‌ها") },
                    selected = currentScreen == "cases",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "cases"
                    }
                )
                NavigationDrawerItem(
                    icon = { Text("👷") },
                    label = { Text("مهندسین") },
                    selected = currentScreen == "engineers",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "engineers"
                    }
                )
                NavigationDrawerItem(
                    icon = { Text("💰") },
                    label = { Text("حق‌الزحمه") },
                    selected = currentScreen == "fees",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "fees"
                    }
                )
                NavigationDrawerItem(
                    icon = { Text("📄") },
                    label = { Text("گزارش‌ها") },
                    selected = currentScreen == "reports",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "reports"
                    }
                )
                NavigationDrawerItem(
                    icon = { Text("📌") },
                    label = { Text("پیگیری") },
                    selected = currentScreen == "followup",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "followup"
                    }
                )
                Divider()
                NavigationDrawerItem(
                    icon = { Text("⚙️") },
                    label = { Text("تنظیمات") },
                    selected = currentScreen == "settings",
                    onClick = {
                        scope.launch { drawerState.close() }
                        currentScreen = "settings"
                    }
                )
            }
        }
    ) {
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
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "منو")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (currentScreen) {
                    "home" -> HomeScreen(
                        onImportClick = {
                            importLauncher.launch(arrayOf("application/zip", "application/octet-stream"))
                        },
                        importMessage = importMessage
                    )
                    "cases" -> CasesScreen(dao, isRtl)
                    "engineers" -> EngineersScreen(dao, isRtl)
                    "fees" -> FeesScreen(dao, isRtl)
                    "reports" -> ReportsScreen(dao, isRtl)
                    "followup" -> FollowUpScreen(dao, isRtl)
                    "settings" -> SettingsScreen(
                        isRtl = isRtl,
                        onRtlChange = { isRtl = it }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onImportClick: () -> Unit, importMessage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("خوش آمدید", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onImportClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("ورود اطلاعات از ویندوز", modifier = Modifier.padding(8.dp), fontSize = 16.sp)
        }
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
    }
}

@Composable
fun CasesScreen(dao: NezamDao, isRtl: Boolean) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("پرونده: ${case.caseNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("دفتر: ${case.office}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("مالک: ${case.owner}", fontSize = 14.sp)
                    Text("آدرس: ${case.serial}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (case.ownerMobile.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📱 ${case.ownerMobile}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
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
fun EngineersScreen(dao: NezamDao, isRtl: Boolean) {
    val engineers by dao.getAllEngineers().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(engineers) { eng ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(eng.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("رشته: ${eng.discipline}", fontSize = 14.sp)
                    Text("نقش: ${eng.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun FeesScreen(dao: NezamDao, isRtl: Boolean) {
    val fees by dao.getAllFees().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(fees) { fee ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("پرونده: ${fee.caseNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("مبلغ: ${fee.amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("مرحله: ${fee.stage} | رشته: ${fee.discipline}", fontSize = 12.sp)
                    Text("خدمت: ${fee.serviceType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun ReportsScreen(dao: NezamDao, isRtl: Boolean) {
    val reports by dao.getAllReports().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(reports) { r ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("گزارش: ${r.reportType}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("مرحله: ${r.stage}", fontSize = 12.sp)
                    }
                    Text("مهندس: ${r.engineer} | رشته: ${r.discipline}", fontSize = 12.sp)
                    Text("تاریخ بازدید: ${r.visitDate}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun FollowUpScreen(dao: NezamDao, isRtl: Boolean) {
    val edits by dao.getAllFollowUpEdits().collectAsState(initial = emptyList())
    val cases by dao.getAllCases().collectAsState(initial = emptyList())

    val followUpItems = remember(edits, cases) {
        cases.map { case ->
            val edit = edits.find { it.caseNumber == case.caseNumber }
            FollowUpDisplayItem(
                caseNumber = edit?.caseNumberEdit?.ifBlank { case.caseNumber } ?: case.caseNumber,
                owner = edit?.ownerEdit?.ifBlank { case.owner } ?: case.owner,
                mobile = edit?.ownerMobileEdit?.ifBlank { case.ownerMobile } ?: case.ownerMobile,
                description = edit?.description ?: ""
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
                    Text("پرونده: ${item.caseNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("مالک: ${item.owner}", fontSize = 14.sp)
                    if (item.mobile.isNotBlank()) {
                        Text(
                            text = "📱 ${item.mobile}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:${item.mobile}")
                                // context.startActivity(intent)
                            }
                        )
                    }
                    if (item.description.isNotBlank()) {
                        Text("توضیحات: ${item.description}", color = MaterialTheme.colorScheme.tertiary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(isRtl: Boolean, onRtlChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("تنظیمات", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("جهت نمایش", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("راست به چپ (فارسی)")
                    Switch(
                        checked = isRtl,
                        onCheckedChange = { onRtlChange(it) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("اطلاعات برنامه", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("نسخه: 1.0", fontSize = 14.sp)
                Text("ساخته شده توسط مهندس برخورداری", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class FollowUpDisplayItem(
    val caseNumber: String,
    val owner: String,
    val mobile: String,
    val description: String
)
