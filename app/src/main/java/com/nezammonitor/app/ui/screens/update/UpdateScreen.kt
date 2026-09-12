package com.nezammonitor.app.ui.screens.update

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.api.NezamApi
import com.nezammonitor.app.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.gson.JsonObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = context.getSharedPreferences("nezam_update", Context.MODE_PRIVATE)

    var username by remember { mutableStateOf(prefs.getString("username", "") ?: "") }
    var password by remember { mutableStateOf(prefs.getString("password", "") ?: "") }
    var saveCredentials by remember { mutableStateOf(prefs.getBoolean("save_creds", false)) }
    var isUpdating by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("") }
    var progress by remember { mutableFloatStateOf(0f) }
    var lastUpdate by remember { mutableStateOf(prefs.getString("last_update", "هنوز بروزرسانی نشده") ?: "هنوز بروزرسانی نشده") }

    // Report save path
    var reportPath by remember { mutableStateOf(prefs.getString("report_path", Environment.getExternalStorageDirectory().absolutePath) ?: Environment.getExternalStorageDirectory().absolutePath) }
    var showPathDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بروزرسانی اطلاعات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Last update info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("آخرین بروزرسانی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(lastUpdate, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Login credentials
            Text("اطلاعات ورود", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("شماره عضویت") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("رمز عبور") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = saveCredentials,
                    onCheckedChange = { saveCredentials = it }
                )
                Text("ذخیره اطلاعات ورود", fontSize = 14.sp)
            }

            // Report save path
            Text("مسیر ذخیره‌سازی گزارش‌ها", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showPathDialog = true }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("مسیر فعلی:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(reportPath, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("برای تغییر کلیک کنید", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
            }

            // Update button
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        statusText = "لطفاً شماره عضویت و رمز عبور را وارد کنید"
                        return@Button
                    }

                    // Save credentials if checked
                    prefs.edit().apply {
                        if (saveCredentials) {
                            putString("username", username)
                            putString("password", password)
                            putBoolean("save_creds", true)
                        } else {
                            remove("username")
                            remove("password")
                            putBoolean("save_creds", false)
                        }
                        putString("report_path", reportPath)
                        apply()
                    }

                    isUpdating = true
                    scope.launch {
                        try {
                            val api = NezamApi()
                            statusText = "در حال ورود..."
                            progress = 0.1f

                            val loginResult = api.login(username, password)
                            if (loginResult.isFailure) {
                                statusText = "خطا: ${loginResult.exceptionOrNull()?.message}"
                                isUpdating = false
                                return@launch
                            }

                            statusText = "دریافت لیست پرونده‌ها..."
                            progress = 0.2f
                            val casesResult = api.getCases()
                            if (casesResult.isFailure) {
                                statusText = "خطا در دریافت پرونده‌ها"
                                isUpdating = false
                                return@launch
                            }
                            val apiCases = casesResult.getOrNull() ?: emptyList()
                            statusText = "${apiCases.size} پرونده یافت شد"

                            val db = NezamDatabase.getDatabase(context)
                            val dao = db.nezamDao()

                            // Clear old data
                            dao.deleteAllCases()
                            dao.deleteAllSpecifications()
                            dao.deleteAllEngineers()
                            dao.deleteAllFees()
                            dao.deleteAllReports()

                            // Process cases
                            val cases = mutableListOf<CaseEntity>()
                            val specs = mutableListOf<SpecificationEntity>()
                            val allEngineers = mutableListOf<EngineerEntity>()
                            val allFees = mutableListOf<FeeEntity>()
                            val allReports = mutableListOf<ReportEntity>()

                            for ((index, apiCase) in apiCases.withIndex()) {
                                progress = 0.2f + (0.5f * index / apiCases.size)
                                statusText = "پرونده ${index + 1}/${apiCases.size}: ${safe(apiCase, "own_name")} ${safe(apiCase, "own_famil")}"

                                val caseNumber = "${safe(apiCase, "das_year")}/${safe(apiCase, "das_number")}"
                                val serial = safe(apiCase, "das_serial")
                                val dbId = safe(apiCase, "db_id")

                                cases.add(CaseEntity(
                                    caseNumber = caseNumber,
                                    serial = serial,
                                    owner = "${safe(apiCase, "own_name")} ${safe(apiCase, "own_famil")}".trim(),
                                    ownerMobile = safe(apiCase, "own_mob"),
                                    responsibility = safe(apiCase, "ref_state_title"),
                                    capacityDate = safe(apiCase, "kasr_date"),
                                    office = safe(apiCase, "daf_name"),
                                    ownerFather = safe(apiCase, "own_father"),
                                    ownerNationalCode = safe(apiCase, "own_code_melli"),
                                    ownerAddress = safe(apiCase, "own_address"),
                                    ownerZip = safe(apiCase, "own_zip"),
                                    ownerTel = safe(apiCase, "own_tel"),
                                    ownerBirthLoc = safe(apiCase, "own_birth_loc"),
                                    permitNumber = safe(apiCase, "das_parvana_num"),
                                    permitDate = safe(apiCase, "das_parvana_date"),
                                    renovationCode = safe(apiCase, "das_code_nosazi"),
                                    address = safe(apiCase, "das_address"),
                                    blockNumber = safe(apiCase, "das_blocknum"),
                                    landArea = safe(apiCase, "das_masahat"),
                                    municipalArea = safe(apiCase, "db_metraj_shahrdari"),
                                    effectiveArea = safe(apiCase, "db_metraj_effective_nezarat"),
                                    floors = safe(apiCase, "db_tabagha_shahrdari"),
                                    units = safe(apiCase, "db_vahed_count"),
                                    buildingGroup = safe(apiCase, "bg_name"),
                                    structureType = safe(apiCase, "sazetyp_title"),
                                    usageType = safe(apiCase, "usetyp_title"),
                                    planZone = safe(apiCase, "mah_title"),
                                    issuer = safe(apiCase, "das_sad_title"),
                                    stateTitle = safe(apiCase, "ref_state_title"),
                                    workType = safe(apiCase, "dastyp_title"),
                                    discipline = safe(apiCase, "mas_title"),
                                    unitTitle = safe(apiCase, "db_title"),
                                    executionDate = safe(apiCase, "das_date_tarkhis"),
                                    dbId = dbId
                                ))

                                // Specification
                                specs.add(SpecificationEntity(
                                    caseNumber = caseNumber,
                                    address = safe(apiCase, "das_address"),
                                    buildingGroup = safe(apiCase, "bg_name"),
                                    renovationCode = safe(apiCase, "das_code_nosazi"),
                                    permitNumber = safe(apiCase, "das_parvana_num"),
                                    permitDate = safe(apiCase, "das_parvana_date"),
                                    releaseDate = safe(apiCase, "das_date_tarkhis"),
                                    landArea = safe(apiCase, "das_masahat"),
                                    capacityArea = safe(apiCase, "db_metraj_effective_nezarat"),
                                    structureType = safe(apiCase, "sazetyp_title"),
                                    blockTitle = safe(apiCase, "db_title"),
                                    blockCount = safe(apiCase, "das_blocknum"),
                                    floors = safe(apiCase, "db_tabagha_shahrdari"),
                                    units = safe(apiCase, "db_vahed_count"),
                                    issuer = safe(apiCase, "das_sad_title"),
                                    planZone = safe(apiCase, "mah_title"),
                                    usageType = safe(apiCase, "usetyp_title")
                                ))

                                // Engineers
                                if (dbId.toIntOrNull() != null) {
                                    val engResult = api.getEngineers(dbId.toInt())
                                    engResult.getOrNull()?.forEach { eng ->
                                        allEngineers.add(EngineerEntity(
                                            caseNumber = caseNumber,
                                            discipline = safe(eng, "mas_title"),
                                            name = "${safe(eng, "ozh_name")} ${safe(eng, "ozh_famil")}".trim(),
                                            role = ""
                                        ))
                                    }
                                }

                                // Fees
                                if (dbId.toIntOrNull() != null) {
                                    val feeResult = api.getFees(dbId.toInt())
                                    feeResult.getOrNull()?.forEach { fee ->
                                        allFees.add(FeeEntity(
                                            caseNumber = caseNumber,
                                            discipline = safe(fee, "mas_title"),
                                            serviceType = safe(fee, "dbm_p_type"),
                                            stage = safe(fee, "dbm_marhala_id"),
                                            amount = safe(fee, "dbm_mablagh"),
                                            payStatus = safe(fee, "payed"),
                                            confirmStatus = safe(fee, "selected"),
                                            description = "${safe(fee, "ozv_name")} ${safe(fee, "ozv_num")}"
                                        ))
                                    }
                                }

                                // Reports
                                if (dbId.toIntOrNull() != null) {
                                    val rptResult = api.getReports(dbId.toInt())
                                    rptResult.getOrNull()?.forEach { rpt ->
                                        allReports.add(ReportEntity(
                                            caseNumber = caseNumber,
                                            reportType = safe(rpt, "brt_report_title"),
                                            stage = safe(rpt, "brt_marhale_title"),
                                            engineer = "${safe(rpt, "ozh_name")} ${safe(rpt, "ozh_famil")}".trim(),
                                            discipline = safe(rpt, "mas_title"),
                                            visitDate = safe(rpt, "br_bazdid_date"),
                                            ceilingCount = safe(rpt, "br_exec_ceil"),
                                            indicator = safe(rpt, "br_andicator"),
                                            hasFile = safe(rpt, "image_name").isNotEmpty()
                                        ))
                                    }
                                }
                            }

                            // Save to database
                            statusText = "ذخیره‌سازی در پایگاه داده..."
                            progress = 0.8f
                            dao.insertCases(cases)
                            dao.insertSpecifications(specs)
                            dao.insertEngineers(allEngineers)
                            dao.insertFees(allFees)
                            dao.insertReports(allReports)

                            // Download report files
                            statusText = "دانلود فایل‌های گزارش‌ها..."
                            progress = 0.85f
                            var downloadedFiles = 0
                            var caseNum = 0
                            for ((index, apiCase) in apiCases.withIndex()) {
                                caseNum++
                                val dbId = safe(apiCase, "db_id")
                                if (dbId.toIntOrNull() == null) continue

                                val rptResult = api.getReports(dbId.toInt())
                                val rptList = rptResult.getOrNull() ?: continue
                                if (rptList.isEmpty()) continue

                                val owner = "${safe(apiCase, "own_name")} ${safe(apiCase, "own_famil")}".trim()
                                val caseNum2 = "${safe(apiCase, "das_year")}/${safe(apiCase, "das_number")}"
                                val safeOwner = owner.replace("/", "_").replace("\\", "_").replace(":", "_")
                                val safeCase = caseNum2.replace("/", "_")
                                val caseFolder = java.io.File(reportPath, "NezamMonitor/$caseNum" + "_${safeOwner}_$safeCase")
                                caseFolder.mkdirs()

                                for ((rptIdx, rpt) in rptList.withIndex()) {
                                    val imageName = safe(rpt, "image_name")
                                    if (imageName.isEmpty()) continue
                                    val reportType = safe(rpt, "brt_report_title")
                                    val safeType = reportType.replace("/", "_").replace("\\", "_").replace(":", "_")
                                    val ext = if (imageName.contains(".")) ".${imageName.split(".").last()}" else ".jpg"
                                    val fileName = "${rptIdx + 1}_${safeType}$ext"

                                    try {
                                        val fileResult = api.downloadFile(imageName)
                                        if (fileResult.isSuccess) {
                                            val file = java.io.File(caseFolder, fileName)
                                            file.writeBytes(fileResult.getOrNull() ?: byteArrayOf())
                                            downloadedFiles++
                                        }
                                    } catch (_: Exception) {}
                                }

                                progress = 0.85f + (0.15f * (index + 1) / apiCases.size)
                                if ((index + 1) % 10 == 0 || index == apiCases.size - 1) {
                                    statusText = "دانلود فایل‌ها: ${index + 1}/${apiCases.size} ($downloadedFiles فایل)"
                                }
                            }

                            // Done
                            val now = java.text.SimpleDateFormat("yyyy/MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            prefs.edit().putString("last_update", now).apply()
                            lastUpdate = now
                            statusText = "✅ بروزرسانی کامل شد!\n${cases.size} پرونده | ${allEngineers.size} مهندس | ${allFees.size} حق‌الزحمه | ${allReports.size} گزارش | $downloadedFiles فایل"
                            progress = 1f
                        } catch (e: Exception) {
                            statusText = "خطا: ${e.message}"
                        } finally {
                            isUpdating = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUpdating && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("در حال بروزرسانی...")
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بروزرسانی اطلاعات")
                }
            }

            // Progress bar
            if (isUpdating) {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Status text
            if (statusText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusText.startsWith("✅")) Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else if (statusText.startsWith("خطا")) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // Path selection dialog
    if (showPathDialog) {
        AlertDialog(
            onDismissRequest = { showPathDialog = false },
            title = { Text("انتخاب مسیر ذخیره‌سازی") },
            text = {
                Column {
                    Text("مسیر فعلی:", fontSize = 12.sp)
                    Text(reportPath, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("مسیرهای پیشنهادی:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    val suggestions = listOf(
                        Environment.getExternalStorageDirectory().absolutePath to "حافظه اصلی",
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath to "Documents",
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath to "Downloads",
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath to "DCIM"
                    )

                    suggestions.forEach { (path, label) ->
                        TextButton(onClick = {
                            reportPath = path
                            prefs.edit().putString("report_path", path).apply()
                            showPathDialog = false
                        }) {
                            Text("$label\n$path", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPathDialog = false }) {
                    Text("بستن")
                }
            }
        )
    }
}

private fun safe(obj: JsonObject, key: String): String {
    return try {
        if (obj.has(key) && !obj.get(key).isJsonNull) {
            obj.get(key).asString
        } else ""
    } catch (_: Exception) { "" }
}
