package com.nezammonitor.app.ui.screens.reportgen

import android.content.Context
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
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportGeneratorScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    val specs by viewModel.specifications.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedStage by remember { mutableIntStateOf(0) }
    var useLtr by remember { mutableStateOf(false) }
    var selectedCases by remember { mutableStateOf(setOf<String>()) }
    var selectAll by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var resultFiles by remember { mutableStateOf(listOf<File>()) }
    var showDuplicateWarning by remember { mutableStateOf(false) }
    var pendingGeneration by remember { mutableStateOf(false) }

    val templatesDir = File("/storage/emulated/0/NezamMonitor/templates")
    val outputsDir = File("/storage/emulated/0/NezamMonitor/outputs")
    val templatesExist = templatesDir.exists() && (templatesDir.listFiles()?.any { it.name.endsWith(".docx") } == true)

    fun doGenerate() {
        if (selectedStage == 0 || selectedCases.isEmpty()) {
            statusText = "لطفاً مرحله و حداقل یک پرونده را انتخاب کنید"
            return
        }
        isGenerating = true
        statusText = ""
        resultFiles = emptyList()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                generateReports(
                    context = context,
                    cases = cases.filter { it.caseNumber in selectedCases },
                    engineers = engineers,
                    specs = specs,
                    stage = selectedStage,
                    useLtr = useLtr,
                    templatesDir = templatesDir,
                    outputsDir = outputsDir
                )
            }
            resultFiles = result.files
            statusText = result.message
            isGenerating = false
        }
    }

    // Duplicate warning dialog
    if (showDuplicateWarning) {
        AlertDialog(
            onDismissRequest = { showDuplicateWarning = false; pendingGeneration = false },
            title = { Text("⚠️ هشدار") },
            text = { Text("برخی گزارش‌ها قبلاً تولید شده‌اند. آیا مایلید دوباره تولید کنید؟\nفایل‌های قبلی بازنویسی خواهند شد.") },
            confirmButton = {
                TextButton(onClick = { showDuplicateWarning = false; pendingGeneration = false; doGenerate() }) {
                    Text("تولید مجدد", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDuplicateWarning = false; pendingGeneration = false }) { Text("لغو") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تولید گزارش") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Generate button at top
            item {
                Button(
                    onClick = {
                        if (selectedStage == 0 || selectedCases.isEmpty()) {
                            statusText = "لطفاً مرحله و حداقل یک پرونده را انتخاب کنید"
                            return@Button
                        }
                        // Check for duplicates
                        val stageName = when(selectedStage) { 1 -> "اول"; 2 -> "دوم"; 3 -> "سوم"; else -> "اول" }
                        val hasDuplicates = cases.filter { it.caseNumber in selectedCases }.any { case ->
                            val rowNum = String.format("%02d", cases.indexOf(case) + 1)
                            val reversedCase = reverseCaseNumber(case.caseNumber)
                            val safeOwner = case.owner.replace(Regex("[/\\\\:*?\"<>|]"), "_")
                            val folder = File(outputsDir, "$rowNum - $safeOwner - $reversedCase")
                            folder.exists() && folder.listFiles()?.any { it.name.contains("مرحله $stageName") } == true
                        }
                        if (hasDuplicates) {
                            showDuplicateWarning = true
                            pendingGeneration = true
                        } else {
                            doGenerate()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isGenerating && selectedStage > 0 && selectedCases.isNotEmpty() && templatesExist
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("📊 تولید گزارش", fontSize = 15.sp)
                }
            }

            // Template warning
            if (!templatesExist) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⚠️ فایل تمپلیت یافت نشد!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("فایل‌های تمپلیت (.docx) را در پوشه زیر قرار دهید:", fontSize = 12.sp)
                            Text(templatesDir.absolutePath, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("نام فایل‌ها:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("• مکانیک مرحله اول.docx", fontSize = 11.sp)
                            Text("• مکانیک مرحله دوم.docx", fontSize = 11.sp)
                            Text("• مکانیک مرحله سوم.docx", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Stage selection
            item {
                Text("انتخاب مرحله:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1 to "مرحله اول", 2 to "مرحله دوم", 3 to "مرحله سوم").forEach { (num, name) ->
                        FilterChip(
                            selected = selectedStage == num,
                            onClick = { selectedStage = num },
                            label = { Text(name, fontSize = 13.sp) }
                        )
                    }
                }
            }

            // LTR checkbox
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = useLtr, onCheckedChange = { useLtr = it })
                    Text("تولید چپ‌چین (LTR)", fontSize = 14.sp)
                }
            }

            // Info
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("📂 تمپلیت‌ها:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(templatesDir.absolutePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("📂 خروجی:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(outputsDir.absolutePath, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Select all
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectAll,
                        onCheckedChange = {
                            selectAll = it
                            selectedCases = if (it) cases.map { c -> c.caseNumber }.toSet() else emptySet()
                        }
                    )
                    Text("انتخاب همه پرونده‌ها (${cases.size})", fontSize = 14.sp)
                }
            }

            // Case list
            items(cases) { case ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedCases = if (case.caseNumber in selectedCases)
                            selectedCases - case.caseNumber
                        else selectedCases + case.caseNumber
                    }
                ) {
                    Checkbox(
                        checked = case.caseNumber in selectedCases,
                        onCheckedChange = {
                            selectedCases = if (case.caseNumber in selectedCases)
                                selectedCases - case.caseNumber
                            else selectedCases + case.caseNumber
                        }
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(case.owner, fontSize = 14.sp)
                        Text(case.caseNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Status
            if (statusText.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (statusText.startsWith("✅")) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(statusText, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
                    }
                }
            }

            // Result files
            if (resultFiles.isNotEmpty()) {
                item { Text("فایل‌های تولید شده:", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(resultFiles) { file ->
                    Card(modifier = Modifier.fillMaxWidth().clickable {
                        try {
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("📄 ${file.name}", fontSize = 13.sp)
                                Text(file.parentFile?.name ?: "", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("باز کردن →", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

// ==================== REPORT GENERATION LOGIC ====================

private data class GenerateResult(val files: List<File>, val message: String)

private fun generateReports(
    context: Context,
    cases: List<CaseEntity>,
    engineers: List<EngineerEntity>,
    specs: List<SpecificationEntity>,
    stage: Int,
    useLtr: Boolean,
    templatesDir: File,
    outputsDir: File
): GenerateResult {
    val files = mutableListOf<File>()
    val stageName = when(stage) { 1 -> "اول"; 2 -> "دوم"; 3 -> "سوم"; else -> "اول" }

    val templateFile = findTemplate(templatesDir, stageName)
        ?: return GenerateResult(emptyList(), "❌ فایل تمپلیت مرحله $stageName یافت نشد\n\nفایل‌های تمپلیت (.docx) را در پوشه زیر قرار دهید:\n${templatesDir.absolutePath}\n\nنام فایل‌ها:\n• مکانیک مرحله اول.docx\n• مکانیک مرحله دوم.docx\n• مکانیک مرحله سوم.docx")

    outputsDir.mkdirs()
    val now = Date()
    val persianDate = toPersianDate(now)
    val englishDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
    var count = 0

    for ((index, case) in cases.withIndex()) {
        val rowNum = String.format("%02d", index + 1)
        val reversedCase = reverseCaseNumber(case.caseNumber)
        val safeOwner = case.owner.replace(Regex("[/\\\\:*?\"<>|]"), "_")

        // Folder naming: RTL = arabic numerals, LTR = english numerals
        val folderNum = if (useLtr) String.format("%02d", index + 1) else toPersianDigits(rowNum)
        val folderCase = if (useLtr) toEnglishDigits(reversedCase) else reversedCase.replace(Regex("[0-9]")) { toPersianDigits(it.value) }
        val folderName = "$folderNum - $safeOwner - $folderCase"
        val folder = File(outputsDir, folderName)
        folder.mkdirs()

        // Get engineers
        val caseEngineers = engineers.filter { it.caseNumber == case.caseNumber }
        val spec = specs.firstOrNull { it.caseNumber == case.caseNumber }

        // File naming
        val reportDate = if (useLtr) englishDate else persianDate
        val ltrSuffix = if (useLtr) " - LTR" else ""
        val fileName = "گزارش مرحله $stageName $safeOwner - $reportDate$ltrSuffix.docx"
        val outputFile = File(folder, fileName)

        try {
            templateFile.copyTo(outputFile, overwrite = true)
            replacePlaceholders(outputFile, case, spec, caseEngineers, persianDate, useLtr)
            files.add(outputFile)
            count++
        } catch (e: Exception) {
            return GenerateResult(files, "❌ خطا در تولید گزارش ${case.owner}: ${e.message}")
        }
    }

    return GenerateResult(files, "✅ $count گزارش مرحله $stageName تولید شد\n📁 ${outputsDir.absolutePath}")
}

private fun findTemplate(templatesDir: File, stageName: String): File? {
    if (!templatesDir.exists()) return null
    val patterns = listOf("مکانیک مرحله $stageName.docx", "مرحله $stageName.docx")
    for (pattern in patterns) {
        val file = File(templatesDir, pattern)
        if (file.exists()) return file
    }
    val allTemplates = templatesDir.listFiles { f -> f.name.endsWith(".docx") } ?: return null
    return allTemplates.firstOrNull { it.name.contains(stageName) }
}

private fun replacePlaceholders(docxFile: File, case: CaseEntity, spec: SpecificationEntity?, engineers: List<EngineerEntity>, persianDate: String, useLtr: Boolean) {
    try {
        val doc = XWPFDocument(FileInputStream(docxFile))
        for (para in doc.paragraphs) replaceInParagraph(para, case, spec, engineers, persianDate, useLtr)
        for (table in doc.tables) for (row in table.rows) for (cell in row.tableCells) for (para in cell.paragraphs) replaceInParagraph(para, case, spec, engineers, persianDate, useLtr)
        val out = FileOutputStream(docxFile)
        doc.write(out)
        out.close()
        doc.close()
    } catch (_: Exception) {}
}

private fun replaceInParagraph(para: org.apache.poi.xwpf.usermodel.XWPFParagraph, case: CaseEntity, spec: SpecificationEntity?, engineers: List<EngineerEntity>, persianDate: String, useLtr: Boolean) {
    val runs = para.runs
    if (runs.isEmpty()) return
    var fullText = runs.joinToString("") { it.text() }

    // Only 13 main placeholders + 4 engineer placeholders
    val rawReplacements = mapOf(
        "«کد_نوسازی»" to (spec?.renovationCode ?: ""),
        "«تاریخ_گزارش»" to persianDate,
        "«شماره_پرونده»" to case.caseNumber,
        "«عنوان_بلوک»" to (spec?.blockTitle ?: ""),
        "«شماره_پروانه»" to (spec?.permitNumber ?: ""),
        "«تاریخ_صدور_پروانه»" to (spec?.permitDate ?: ""),
        "«تعداد_بلوک»" to (spec?.blockCount ?: ""),
        "«نوع_کاربری»" to (spec?.usageType ?: ""),
        "«نوع_سازه»" to (spec?.structureType ?: ""),
        "«گروه_ساختمانی»" to (spec?.buildingGroup ?: ""),
        "«تعداد_طبقات»" to (spec?.floors ?: ""),
        "«مالک»" to case.owner,
        "«هماهنگکننده»" to (engineers.firstOrNull { it.discipline.contains("هماهنگ") }?.name ?: ""),
        // Engineers - only if in template
        "«ناظر_معماری»" to (engineers.firstOrNull { it.discipline.contains("معمار") }?.name ?: ""),
        "«ناظر_عمران»" to (engineers.firstOrNull { it.discipline.contains("عمران") }?.name ?: ""),
        "«ناظر_مکانیک»" to (engineers.firstOrNull { it.discipline.contains("مکانیک") || it.discipline.contains("مكانيك") }?.name ?: ""),
        "«ناظر_برق»" to (engineers.firstOrNull { it.discipline.contains("برق") }?.name ?: "")
    )

    // Apply LTR transformation
    val replacements = if (useLtr) {
        rawReplacements.mapValues { (key, value) ->
            if (key in listOf("«شماره_پرونده»", "«شماره_پروانه»", "«تاریخ_گزارش»", "«تاریخ_صدور_پروانه»", "«کد_نوسازی»", "«تعداد_بلوک»", "«تعداد_طبقات»")) {
                toEnglishDigits(reverseSlash(toAsciiDigits(value)))
            } else value
        }
    } else {
        rawReplacements.mapValues { (key, value) ->
            if (key in listOf("«شماره_پرونده»", "«شماره_پروانه»", "«تاریخ_گزارش»", "«تاریخ_صدور_پروانه»", "«کد_نوسازی»", "«تعداد_بلوک»", "«تعداد_طبقات»")) {
                toPersianDigits(value)
            } else value
        }
    }

    for ((key, value) in replacements) fullText = fullText.replace(key, value)

    if (runs.isNotEmpty()) {
        runs[0].setText(fullText, 0)
        for (i in 1 until runs.size) runs[i].setText("", 0)
    }
}

// ==================== DIGIT CONVERSION ====================

private fun toPersianDigits(input: String): String = input
    .replace("0", "۰").replace("1", "۱").replace("2", "۲").replace("3", "۳").replace("4", "۴")
    .replace("5", "۵").replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")

private fun toEnglishDigits(input: String): String = input
    .replace("۰", "0").replace("۱", "1").replace("۲", "2").replace("۳", "3").replace("۴", "4")
    .replace("۵", "5").replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9")

private fun toAsciiDigits(input: String): String = input
    .replace("۰", "0").replace("۱", "1").replace("۲", "2").replace("۳", "3").replace("۴", "4")
    .replace("۵", "5").replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9")

private fun reverseSlash(input: String): String {
    if (!input.contains("/")) return input
    val parts = input.split("/")
    return parts.reversed().joinToString("/")
}

private fun reverseCaseNumber(caseNumber: String): String {
    if (!caseNumber.contains("/")) return caseNumber
    val parts = caseNumber.split("/")
    return parts.reversed().joinToString("-")
}

private fun toPersianDate(date: Date): String {
    val cal = java.util.Calendar.getInstance()
    cal.time = date
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH) + 1
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val jy = year - 621
    return "$jy/${String.format("%02d", month)}/${String.format("%02d", day)}"
}

// ==================== HISTORY ====================

data class GeneratedReportRecord(
    val caseNumber: String = "",
    val owner: String = "",
    val stage: Int = 0,
    val createdAt: String = "",
    val filePath: String = "",
    val folderPath: String = "",
    val fileExists: Boolean = true
)

internal fun saveReportHistory(context: Context, records: List<GeneratedReportRecord>) {
    try {
        val prefs = context.getSharedPreferences("nezam_report_history", Context.MODE_PRIVATE)
        val existing = loadReportHistory(context).toMutableList()
        existing.addAll(records)
        val json = com.google.gson.Gson().toJson(existing)
        prefs.edit().putString("history", json).apply()
    } catch (_: Exception) {}
}

internal fun loadReportHistory(context: Context): List<GeneratedReportRecord> {
    return try {
        val prefs = context.getSharedPreferences("nezam_report_history", Context.MODE_PRIVATE)
        val json = prefs.getString("history", "[]") ?: "[]"
        com.google.gson.Gson().fromJson(json, Array<GeneratedReportRecord>::class.java).toList()
    } catch (_: Exception) { emptyList() }
}

internal fun deleteReportHistoryItem(context: Context, index: Int) {
    try {
        val history = loadReportHistory(context).toMutableList()
        if (index in history.indices) {
            val item = history.removeAt(index)
            try { File(item.filePath).delete() } catch (_: Exception) {}
            try { File(item.folderPath).delete() } catch (_: Exception) {}
            val json = com.google.gson.Gson().toJson(history)
            context.getSharedPreferences("nezam_report_history", Context.MODE_PRIVATE)
                .edit().putString("history", json).apply()
        }
    } catch (_: Exception) {}
}
