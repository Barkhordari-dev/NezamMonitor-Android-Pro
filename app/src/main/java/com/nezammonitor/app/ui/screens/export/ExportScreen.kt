package com.nezammonitor.app.ui.screens.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.compose.foundation.layout.*
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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.nezammonitor.app.data.*
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    val fees by viewModel.fees.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val edits by viewModel.followUpEdits.collectAsState()
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var lastExportedFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("خروجی برای ویندوز", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("اطلاعات خروجی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("پرونده\u200Cها: ${cases.size}", fontSize = 14.sp)
                    Text("مهندسین: ${engineers.size}", fontSize = 14.sp)
                    Text("حق\u200Cالزحمه: ${fees.size}", fontSize = 14.sp)
                    Text("گزارش\u200Cها: ${reports.size}", fontSize = 14.sp)
                    Text("تغییرات پیگیری: ${edits.size}", fontSize = 14.sp)
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📤 خروجی تغییرات", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("فقط تغییرات ویرایش\u200Cشده در Android (توضیحات پیگیری) به Windows منتقل می\u200Cشود.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        statusMessage = "در حال ایجاد خروجی..."
                        val result = withContext(Dispatchers.IO) {
                            createAndroidExport(context, edits)
                        }
                        if (result.first) {
                            lastExportedFile = File(result.second)
                            statusMessage = "✅ خروجی ایجاد شد"
                        } else {
                            statusMessage = "❌ ${result.second}"
                        }
                        isExporting = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isExporting && edits.isNotEmpty()
            ) {
                Text(if (isExporting) "⏳ در حال ایجاد..." else "📤 ایجاد خروجی برای ویندوز", fontSize = 15.sp)
            }

            // Share button
            if (lastExportedFile != null) {
                Button(
                    onClick = {
                        val file = lastExportedFile ?: return@Button
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/zip"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setPackage(null) // let user choose app
                        }
                        context.startActivity(Intent.createChooser(intent, "اشتراک\u200Cگذاری خروجی"))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("📱 اشتراک\u200Cگذاری (بلوتوث، تلگرام، ایتا...)", fontSize = 14.sp)
                }
            }

            if (statusMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(statusMessage, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                }
            }

            if (edits.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "تغییراتی برای ارسال وجود ندارد.\nابتدا در بخش پیگیری، توضیحاتی ویرایش کنید.",
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private suspend fun createAndroidExport(context: Context, edits: List<FollowUpEditEntity>): Pair<Boolean, String> {
    return withContext(Dispatchers.IO) {
        try {
            val changes = edits.map { edit ->
                AndroidChange(
                    entity = "FollowUp",
                    recordId = edit.caseNumber,
                    field = "description",
                    oldValue = "",
                    newValue = edit.description,
                    modifiedAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())
                )
            }

            val changeSet = AndroidChangeSet(
                format = "NezamMonitor.AndroidChanges",
                version = 1,
                source = "Android",
                createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date()),
                changes = changes
            )

            val gson: Gson = GsonBuilder().setPrettyPrinting().create()
            val json = gson.toJson(changeSet)

            val dir = File(context.getExternalFilesDir(null), "exports")
            dir.mkdirs()
            val fileName = "android_changes_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.nzmdata"
            val zipFile = File(dir, fileName)

            val tempZip = File(context.cacheDir, "temp_export.zip")
            if (tempZip.exists()) tempZip.delete()

            val zf = ZipFile(tempZip)
            val zp = ZipParameters()
            zp.fileNameInZip = "changes.json"
            zf.addStream(json.byteInputStream(), zp)

            tempZip.copyTo(zipFile, overwrite = true)
            tempZip.delete()

            Pair(true, zipFile.absolutePath)
        } catch (e: Exception) {
            Pair(false, "خطا: ${e.message}")
        }
    }
}

data class AndroidChangeSet(
    val format: String = "", val version: Int = 1, val source: String = "",
    val createdAt: String = "", val changes: List<AndroidChange> = emptyList()
)

data class AndroidChange(
    val entity: String = "", val recordId: String = "", val field: String = "",
    val oldValue: String = "", val newValue: String = "", val modifiedAt: String = ""
)
