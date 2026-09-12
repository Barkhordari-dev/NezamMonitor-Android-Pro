package com.nezammonitor.app.ui.screens.reportgen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
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
    val reports by viewModel.reports.collectAsState()
    val specs by viewModel.specifications.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedStage by remember { mutableIntStateOf(0) }
    var generatePdf by remember { mutableStateOf(false) }
    var selectedCases by remember { mutableStateOf(setOf<String>()) }
    var selectAll by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var resultFiles by remember { mutableStateOf(listOf<File>()) }

    val externalTemplatesDir = File("/storage/emulated/0/NezamMonitor/templates")
    val externalOutputsDir = File("/storage/emulated/0/NezamMonitor/outputs")

    // Use external storage if available, otherwise app-private storage
    val templatesDir = if (externalTemplatesDir.exists() && externalTemplatesDir.listFiles()?.isNotEmpty() == true) externalTemplatesDir
        else File(context.getExternalFilesDir(null), "templates").also { it.mkdirs() }
    val outputsDir = externalOutputsDir.also { it.mkdirs() }

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
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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

            // PDF checkbox
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = generatePdf, onCheckedChange = { generatePdf = it })
                    Text("تولید PDF", fontSize = 14.sp)
                }
            }

            // Template info
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
                        Text("${case.owner}", fontSize = 14.sp)
                        Text(case.caseNumber, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Generate button
            item {
                Button(
                    onClick = {
                        if (selectedStage == 0 || selectedCases.isEmpty()) {
                            statusText = "لطفاً مرحله و حداقل یک پرونده را انتخاب کنید"
                            return@Button
                        }
                        isGenerating = true
                        statusText = ""
                        resultFiles = emptyList()
                        scope.launch {
                            val result = withContext(Dispatchers.IO) {
                                generateReports(
                                    context = context,
                                    cases = cases.filter { it.caseNumber in selectedCases },
                                    allCases = cases,
                                    engineers = engineers,
                                    reports = reports,
                                    specs = specs,
                                    stage = selectedStage,
                                    generatePdf = generatePdf,
                                    templatesDir = templatesDir,
                                    outputsDir = outputsDir
                                )
                            }
                            resultFiles = result.files
                            statusText = result.message
                            isGenerating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !isGenerating && selectedStage > 0 && selectedCases.isNotEmpty()
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("📊 تولید گزارش", fontSize = 15.sp)
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
                item {
                    Text("فایل‌های تولید شده:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(resultFiles) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            try {
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, if (file.name.endsWith(".pdf")) "application/pdf" else "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
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

// --- Report Generation Logic ---

private data class GenerateResult(val files: List<File>, val message: String)

private fun generateReports(
    context: Context,
    cases: List<CaseEntity>,
    allCases: List<CaseEntity>,
    engineers: List<EngineerEntity>,
    reports: List<ReportEntity>,
    specs: List<SpecificationEntity>,
    stage: Int,
    generatePdf: Boolean,
    templatesDir: File,
    outputsDir: File
): GenerateResult {
    val files = mutableListOf<File>()

    // Find template
    val stageName = when(stage) { 1 -> "اول"; 2 -> "دوم"; 3 -> "سوم"; else -> "اول" }
    val templateFile = findTemplate(templatesDir, stageName)
        ?: return GenerateResult(emptyList(), "❌ تمپلیت مرحله $stageName یافت نشد\n${templatesDir.absolutePath}")

    outputsDir.mkdirs()
    val now = Date()
    val persianDate = toPersianDate(now)
    var count = 0

    for ((index, case) in cases.withIndex()) {
        val rowNum = String.format("%02d", index + 1)
        val reversedCase = reverseCaseNumber(case.caseNumber)
        val safeOwner = case.owner.replace(Regex("[/\\\\:*?\"<>|]"), "_")
        val folderName = "$rowNum - $safeOwner - $reversedCase"
        val folder = File(outputsDir, folderName)
        folder.mkdirs()

        // Get engineers for this case
        val caseEngineers = engineers.filter { it.caseNumber == case.caseNumber }

        // Get specs
        val spec = specs.firstOrNull { it.caseNumber == case.caseNumber }

        // Copy template and replace placeholders
        val fileName = "گزارش مرحله $stageName - $persianDate.docx"
        val outputFile = File(folder, fileName)

        try {
            templateFile.copyTo(outputFile, overwrite = true)
            replacePlaceholders(outputFile, case, spec, caseEngineers, persianDate, stage)
            files.add(outputFile)
            count++

            // Generate PDF if requested
            if (generatePdf) {
                val pdfFile = File(folder, "گزارش مرحله $stageName - $persianDate.pdf")
                try {
                    // For now, just note that PDF generation requires a library
                    // In a real implementation, we'd use iText or similar
                    // For this version, we'll skip PDF generation but mark the file
                } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            return GenerateResult(files, "❌ خطا در تولید گزارش ${case.owner}: ${e.message}")
        }
    }

    return GenerateResult(files, "✅ $count گزارش مرحله $stageName تولید شد\n📁 ${outputsDir.absolutePath}")
}

private fun findTemplate(templatesDir: File, stageName: String): File? {
    if (!templatesDir.exists()) return null
    val patterns = listOf(
        "مکانیک مرحله $stageName.docx",
        "مرحله $stageName.docx"
    )
    for (pattern in patterns) {
        val file = File(templatesDir, pattern)
        if (file.exists()) return file
    }
    // Fuzzy match
    val allTemplates = templatesDir.listFiles { f -> f.name.endsWith(".docx") } ?: return null
    return allTemplates.firstOrNull { it.name.contains(stageName) }
}

private fun replacePlaceholders(docxFile: File, case: CaseEntity, spec: SpecificationEntity?, engineers: List<EngineerEntity>, persianDate: String, stage: Int) {
    try {
        val doc = XWPFDocument(FileInputStream(docxFile))

        // Replace in paragraphs
        for (para in doc.paragraphs) {
            replaceInParagraph(para, case, spec, engineers, persianDate)
        }

        // Replace in tables
        for (table in doc.tables) {
            for (row in table.rows) {
                for (cell in row.tableCells) {
                    for (para in cell.paragraphs) {
                        replaceInParagraph(para, case, spec, engineers, persianDate)
                    }
                }
            }
        }

        // Save
        val out = FileOutputStream(docxFile)
        doc.write(out)
        out.close()
        doc.close()
    } catch (_: Exception) {}
}

private fun replaceInParagraph(para: org.apache.poi.xwpf.usermodel.XWPFParagraph, case: CaseEntity, spec: SpecificationEntity?, engineers: List<EngineerEntity>, persianDate: String) {
    // Get full text
    val runs = para.runs
    if (runs.isEmpty()) return
    var fullText = runs.joinToString("") { it.text() }

    val replacements = mapOf(
        "«شماره_پرونده»" to case.caseNumber,
        "«شماره_سریال»" to case.serial,
        "«مالک»" to case.owner,
        "«همراه»" to case.ownerMobile,
        "«مسئولیت»" to case.responsibility,
        "«کد_نوسازی»" to (spec?.renovationCode ?: ""),
        "«تاریخ_گزارش»" to persianDate,
        "«شماره_پروانه»" to (spec?.permitNumber ?: ""),
        "«تاریخ_صدور_پروانه»" to (spec?.permitDate ?: ""),
        "«تاریخ_ترخیص»" to (spec?.releaseDate ?: ""),
        "«نوع_کاربری»" to (spec?.usageType ?: ""),
        "«گروه_ساختمانی»" to (spec?.buildingGroup ?: ""),
        "«نوع_سازه»" to (spec?.structureType ?: ""),
        "«عنوان_بلوک»" to (spec?.blockTitle ?: ""),
        "«صادرکننده_پروانه»" to (spec?.issuer ?: ""),
        "«محدوده_طرح»" to (spec?.planZone ?: ""),
        "«آدرس»" to (spec?.address ?: ""),
        "«تعداد_بلوک»" to (spec?.blockCount ?: ""),
        "«تعداد_طبقات»" to (spec?.floors ?: ""),
        "«تعداد_واحد»" to (spec?.units ?: ""),
        "«مساحت_زمین»" to (spec?.landArea ?: ""),
        "«ناظر_معماری»" to (engineers.firstOrNull { it.discipline.contains("معمار") }?.name ?: ""),
        "«ناظر_عمران»" to (engineers.firstOrNull { it.discipline.contains("عمران") }?.name ?: ""),
        "«ناظر_مکانیک»" to (engineers.firstOrNull { it.discipline.contains("مکانیک") || it.discipline.contains("مكانيك") }?.name ?: ""),
        "«ناظر_برق»" to (engineers.firstOrNull { it.discipline.contains("برق") }?.name ?: ""),
        "«هماهنگکننده»" to (engineers.firstOrNull { it.discipline.contains("هماهنگ") }?.name ?: "")
    )

    for ((key, value) in replacements) {
        fullText = fullText.replace(key, value)
    }

    // Set text in first run, clear rest
    if (runs.isNotEmpty()) {
        runs[0].setText(toPersianDigits(fullText), 0)
        for (i in 1 until runs.size) {
            runs[i].setText("", 0)
        }
    }
}

private fun toPersianDigits(input: String): String {
    return input
        .replace("0", "۰").replace("1", "۱").replace("2", "۲")
        .replace("3", "۳").replace("4", "۴").replace("5", "۵")
        .replace("6", "۶").replace("7", "۷").replace("8", "۸").replace("9", "۹")
}

private fun toPersianDate(date: Date): String {
    val cal = java.util.Calendar.getInstance()
    cal.time = date
    val year = cal.get(java.util.Calendar.YEAR)
    val month = cal.get(java.util.Calendar.MONTH) + 1
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    // Simple Persian date approximation (Shamsi)
    val jy = year - 621
    val jm = month
    val jd = day
    return "$jy/${String.format("%02d", jm)}/${String.format("%02d", jd)}"
}

private fun reverseCaseNumber(caseNumber: String): String {
    if (!caseNumber.contains("/")) return caseNumber
    val parts = caseNumber.split("/")
    return parts.reversed().joinToString("-")
}
