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
import com.nezammonitor.app.data.*
import com.nezammonitor.app.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val specs by viewModel.specifications.collectAsState()
    val edits by viewModel.followUpEdits.collectAsState()
    val scope = rememberCoroutineScope()
    var statusMessage by remember { mutableStateOf("") }
    var isExporting by remember { mutableStateOf(false) }
    var lastExportedFile by remember { mutableStateOf<File?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("خروجی اکسل", fontSize = 16.sp) },
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
                    Text("مشخصات: ${specs.size}", fontSize = 14.sp)
                    Text("تغییرات پیگیری: ${edits.size}", fontSize = 14.sp)
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 خروجی اکسل", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("فایل اکسل شامل تمام اطلاعات پرونده\u200Cها، مهندسین، حق\u200Cالزحمه، گزارش\u200Cها و تغییرات پیگیری.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Button(
                onClick = {
                    scope.launch {
                        isExporting = true
                        statusMessage = "در حال ایجاد فایل اکسل..."
                        val result = withContext(Dispatchers.IO) {
                            createExcelExport(context, cases, engineers, fees, reports, specs, edits)
                        }
                        if (result.first) {
                            lastExportedFile = File(result.second)
                            statusMessage = "✅ فایل اکسل ایجاد شد"
                        } else {
                            statusMessage = "❌ ${result.second}"
                        }
                        isExporting = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !isExporting
            ) {
                Text(if (isExporting) "⏳ در حال ایجاد..." else "📊 ایجاد فایل اکسل", fontSize = 15.sp)
            }

            if (lastExportedFile != null) {
                Button(
                    onClick = {
                        val file = lastExportedFile ?: return@Button
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/vnd.ms-excel"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setPackage(null)
                        }
                        context.startActivity(Intent.createChooser(intent, "اشتراک\u200Cگذاری فایل اکسل"))
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
        }
    }
}

private fun createExcelExport(
    context: Context,
    cases: List<CaseEntity>,
    engineers: List<EngineerEntity>,
    fees: List<FeeEntity>,
    reports: List<ReportEntity>,
    specs: List<SpecificationEntity>,
    edits: List<FollowUpEditEntity>
): Pair<Boolean, String> {
    return try {
        val dir = File(context.getExternalFilesDir(null), "exports")
        dir.mkdirs()
        val fileName = "NezamMonitor_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.xls"
        val file = File(dir, fileName)

        // Create HTML-based Excel (xls format that Excel can open)
        val sb = StringBuilder()
        sb.append("<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:x='urn:schemas-microsoft-com:office:excel' xmlns='http://www.w3.org/TR/REC-html40'>")
        sb.append("<head><meta charset='utf-8'>")
        sb.append("<style>td{mso-number-format:\\@;}</style>")
        sb.append("</head><body>")

        // Sheet 1: Cases
        sb.append("<table border=1><caption><b>پرونده‌ها</b></caption>")
        sb.append("<tr>")
        listOf("ردیف","شماره پرونده","شماره سریال","نام مالک","شماره همراه","وضعیت","تاریخ کسر","دفتر",
            "نام پدر","کدملی","آدرس","کدپستی","تلفن","زادگاه","شماره پروانه","تاریخ پروانه",
            "کد nosazi","آدرس پروژه","بلوک","متراژ","متراژ شهرداری","متراژ نظارت","طبقات","واحدها",
            "گروه ساختمان","نوع سازه","نوع کاربری","محدوده طرح","صدور","وضعیت","نوع کار","رشته","تاریخ اجرا","شناسه DB"
        ).forEach { sb.append("<th><b>$it</b></th>") }
        sb.append("</tr>")
        cases.forEachIndexed { i, c ->
            sb.append("<tr>")
            listOf("${i+1}", c.caseNumber, c.serial, c.owner, c.ownerMobile, c.responsibility, c.capacityDate, c.office,
                c.ownerFather, c.ownerNationalCode, c.ownerAddress, c.ownerZip, c.ownerTel, c.ownerBirthLoc,
                c.permitNumber, c.permitDate, c.renovationCode, c.address, c.blockNumber, c.landArea,
                c.municipalArea, c.effectiveArea, c.floors, c.units, c.buildingGroup, c.structureType,
                c.usageType, c.planZone, c.issuer, c.stateTitle, c.workType, c.discipline, c.executionDate, c.dbId
            ).forEach { sb.append("<td>$it</td>") }
            sb.append("</tr>")
        }
        sb.append("</table><br><br>")

        // Sheet 2: Engineers
        sb.append("<table border=1><caption><b>مهندسان</b></caption>")
        sb.append("<tr><th><b>ردیف</b></th><th><b>شماره پرونده</b></th><th><b>رشته</b></th><th><b>نام</b></th></tr>")
        engineers.forEachIndexed { i, e ->
            sb.append("<tr><td>${i+1}</td><td>${e.caseNumber}</td><td>${e.discipline}</td><td>${e.name}</td></tr>")
        }
        sb.append("</table><br><br>")

        // Sheet 3: Fees
        sb.append("<table border=1><caption><b>حق‌الزحمه</b></caption>")
        sb.append("<tr><th><b>ردیف</b></th><th><b>شماره پرونده</b></th><th><b>رشته</b></th><th><b>مبلغ</b></th><th><b>وضعیت پرداخت</b></th><th><b>توضیحات</b></th></tr>")
        fees.forEachIndexed { i, f ->
            sb.append("<tr><td>${i+1}</td><td>${f.caseNumber}</td><td>${f.discipline}</td><td>${f.amount}</td><td>${f.payStatus}</td><td>${f.description}</td></tr>")
        }
        sb.append("</table><br><br>")

        // Sheet 4: Reports
        sb.append("<table border=1><caption><b>گزارش‌ها</b></caption>")
        sb.append("<tr><th><b>ردیف</b></th><th><b>شماره پرونده</b></th><th><b>نوع گزارش</b></th><th><b>مرحله</b></th><th><b>مهندس</b></th><th><b>رشته</b></th><th><b>تاریخ</b></th><th><b>سقف</b></th><th><b>اندیکاتور</b></th></tr>")
        reports.forEachIndexed { i, r ->
            sb.append("<tr><td>${i+1}</td><td>${r.caseNumber}</td><td>${r.reportType}</td><td>${r.stage}</td><td>${r.engineer}</td><td>${r.discipline}</td><td>${r.visitDate}</td><td>${r.ceilingCount}</td><td>${r.indicator}</td></tr>")
        }
        sb.append("</table><br><br>")

        // Sheet 5: Specifications
        sb.append("<table border=1><caption><b>مشخصات</b></caption>")
        sb.append("<tr><th><b>ردیف</b></th><th><b>شماره پرونده</b></th><th><b>آدرس</b></th><th><b>گروه ساختمان</b></th><th><b>کد نوسازی</b></th><th><b>شماره پروانه</b></th><th><b>تاریخ پروانه</b></th><th><b>متراژ زمین</b></th><th><b>متراژ نظارت</b></th><th><b>نوع سازه</b></th><th><b>بلوک</b></th><th><b>طبقات</b></th><th><b>واحدها</b></th><th><b>صدور</b></th><th><b>محدوده</b></th><th><b>نوع کاربری</b></th></tr>")
        specs.forEachIndexed { i, s ->
            sb.append("<tr><td>${i+1}</td><td>${s.caseNumber}</td><td>${s.address}</td><td>${s.buildingGroup}</td><td>${s.renovationCode}</td><td>${s.permitNumber}</td><td>${s.permitDate}</td><td>${s.landArea}</td><td>${s.capacityArea}</td><td>${s.structureType}</td><td>${s.blockTitle}</td><td>${s.floors}</td><td>${s.units}</td><td>${s.issuer}</td><td>${s.planZone}</td><td>${s.usageType}</td></tr>")
        }
        sb.append("</table><br><br>")

        // Sheet 6: Follow-up Edits
        sb.append("<table border=1><caption><b>تغییرات پیگیری</b></caption>")
        sb.append("<tr><th><b>ردیف</b></th><th><b>شماره پرونده</b></th><th><b>توضیحات</b></th><th><b>شماره پرونده (ویرایش)</b></th><th><b>نام مالک (ویرایش)</b></th><th><b>آدرس (ویرایش)</b></th><th><b>شماره همراه (ویرایش)</b></th></tr>")
        edits.forEachIndexed { i, e ->
            sb.append("<tr><td>${i+1}</td><td>${e.caseNumber}</td><td>${e.description}</td><td>${e.caseNumberEdit}</td><td>${e.ownerEdit}</td><td>${e.addressEdit}</td><td>${e.ownerMobileEdit}</td></tr>")
        }
        sb.append("</table>")

        sb.append("</body></html>")

        file.writeText(sb.toString(), Charsets.UTF_8)
        Pair(true, file.absolutePath)
    } catch (e: Exception) {
        Pair(false, "خطا: ${e.message}")
    }
}
