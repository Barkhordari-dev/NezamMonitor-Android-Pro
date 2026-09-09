package com.nezammonitor.app.data

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

data class ImportResult(
    val success: Boolean,
    val message: String,
    val casesCount: Int = 0,
    val engineersCount: Int = 0,
    val feesCount: Int = 0,
    val reportsCount: Int = 0
)

class ImportEngine(private val context: Context) {
    private val gson = Gson()
    private val db = NezamDatabase.getDatabase(context)
    private val dao = db.nezamDao()
    
    private fun readJsonFile(file: File): String {
        val raw = file.readText(Charsets.UTF_8)
        return if (raw.isNotEmpty() && raw[0] == '\uFEFF') raw.substring(1) else raw
    }
    
    suspend fun importFromUri(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val tempFile = File(context.cacheDir, "import_temp.nzmdata")
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext ImportResult(false, "Could not read file")
            
            val zipFile = ZipFile(tempFile)
            val tempDir = File(context.cacheDir, "import_extract")
            if (tempDir.exists()) tempDir.deleteRecursively()
            tempDir.mkdirs()
            zipFile.extractAll(tempDir.absolutePath)
            
            val manifestFile = File(tempDir, "manifest.json")
            if (!manifestFile.exists()) {
                tempFile.delete()
                tempDir.deleteRecursively()
                return@withContext ImportResult(false, "Missing manifest.json")
            }
            
            val manifest = gson.fromJson(readJsonFile(manifestFile), ManifestData::class.java)
            if (manifest.format != "NezamMonitor.AndroidData") {
                tempFile.delete()
                tempDir.deleteRecursively()
                return@withContext ImportResult(false, "Invalid format: ${manifest.format}")
            }
            
            val dataFile = File(tempDir, "data.json")
            if (!dataFile.exists()) {
                tempFile.delete()
                tempDir.deleteRecursively()
                return@withContext ImportResult(false, "Missing data.json")
            }
            
            val exportData = gson.fromJson(readJsonFile(dataFile), ExportData::class.java)
            
            if (exportData.cases.isEmpty()) {
                tempFile.delete()
                tempDir.deleteRecursively()
                return@withContext ImportResult(false, "Package contains no cases")
            }
            
            db.withTransaction {
                dao.deleteAllFollowUpEdits()
                dao.deleteAllReports()
                dao.deleteAllFees()
                dao.deleteAllEngineers()
                dao.deleteAllSpecifications()
                dao.deleteAllCases()
                
                val cases = exportData.cases.map { ec ->
                    CaseEntity(
                        caseNumber = ec.caseNumber, serial = ec.serial,
                        owner = ec.owner, ownerMobile = ec.ownerMobile,
                        responsibility = ec.responsibility, capacityDate = ec.capacityDate,
                        office = ec.office, reportDate1 = ec.reportDate1,
                        reportDate2 = ec.reportDate2, reportDate3 = ec.reportDate3
                    )
                }
                dao.insertCases(cases)
                
                val specs = exportData.cases.filter { it.specification != null }.map { ec ->
                    val s = ec.specification!!
                    SpecificationEntity(
                        caseNumber = ec.caseNumber, address = s.address,
                        buildingGroup = s.buildingGroup, renovationCode = s.renovationCode,
                        planInstructionNo = s.planInstructionNo,
                        planInstructionType = s.planInstructionType,
                        planInstructionDate = s.planInstructionDate,
                        landArea = s.landArea, parafArea = s.parafArea,
                        capacityArea = s.capacityArea, structureType = s.structureType,
                        blockTitle = s.blockTitle, blockCount = s.blockCount,
                        floors = s.floors, units = s.units, issuer = s.issuer,
                        permitNumber = s.permitNumber, permitDate = s.permitDate,
                        releaseDate = s.releaseDate, planZone = s.planZone,
                        usageType = s.usageType
                    )
                }
                dao.insertSpecifications(specs)
                
                val engineers = exportData.cases.flatMap { ec ->
                    ec.engineers.map { ee ->
                        EngineerEntity(caseNumber = ec.caseNumber, discipline = ee.discipline, name = ee.name, role = ee.role)
                    }
                }
                dao.insertEngineers(engineers)
                
                val fees = exportData.cases.flatMap { ec ->
                    ec.fees.map { ef ->
                        FeeEntity(
                            caseNumber = ec.caseNumber, discipline = ef.discipline,
                            serviceType = ef.serviceType, stage = ef.stage,
                            startDate = ef.startDate, endDate = ef.endDate,
                            amount = ef.amount, payStatus = ef.payStatus,
                            confirmStatus = ef.confirmStatus, amountType = ef.amountType,
                            description = ef.description
                        )
                    }
                }
                dao.insertFees(fees)
                
                val reports = exportData.cases.flatMap { ec ->
                    ec.reports.map { er ->
                        ReportEntity(
                            caseNumber = ec.caseNumber, rowNo = er.rowNo,
                            reportType = er.reportType, stage = er.stage,
                            engineer = er.engineer, discipline = er.discipline,
                            visitDate = er.visitDate, ceilingCount = er.ceilingCount,
                            indicator = er.indicator, hasFile = er.hasFile
                        )
                    }
                }
                dao.insertReports(reports)
                
                val edits = exportData.cases.filter { it.followUpEdit != null }.map { ec ->
                    val f = ec.followUpEdit!!
                    FollowUpEditEntity(
                        caseNumber = ec.caseNumber, description = f.description,
                        caseNumberEdit = f.caseNumberEdit, ownerEdit = f.ownerEdit,
                        addressEdit = f.addressEdit, ownerMobileEdit = f.ownerMobileEdit
                    )
                }
                edits.forEach { dao.insertFollowUpEdit(it) }
            }
            
            val counts = ImportResult(
                true,
                "${cases.size} پرونده، ${engineers.size} مهندس، ${fees.size} حق\u200Cالزحمه، ${reports.size} گزارش وارد شد",
                cases.size, engineers.size, fees.size, reports.size
            )
            
            tempFile.delete()
            tempDir.deleteRecursively()
            
            counts
        } catch (e: Exception) {
            ImportResult(false, "خطا: ${e.message ?: e.javaClass.simpleName}")
        }
    }
}

data class ManifestData(
    val format: String = "", val version: String = "", val formatVersion: String = "",
    val createdAt: String = "", val sourceApplication: String = "",
    val sourceApplicationVersion: String = "", val recordCount: Int = 0,
    val schemaVersion: String = "", val checksum: String = "",
    val exportedEntities: List<String> = emptyList()
)

data class ExportData(val cases: List<ExportCase> = emptyList())

data class ExportCase(
    val caseNumber: String = "", val serial: String = "", val owner: String = "",
    val ownerMobile: String = "", val responsibility: String = "",
    val capacityDate: String = "", val office: String = "",
    val reportDate1: String = "", val reportDate2: String = "", val reportDate3: String = "",
    val specification: ExportSpecification? = null,
    val engineers: List<ExportEngineer> = emptyList(),
    val fees: List<ExportFee> = emptyList(),
    val reports: List<ExportReport> = emptyList(),
    val followUpEdit: ExportFollowUpEdit? = null
)

data class ExportSpecification(
    val address: String = "", val buildingGroup: String = "",
    val renovationCode: String = "", val planInstructionNo: String = "",
    val planInstructionType: String = "", val planInstructionDate: String = "",
    val landArea: String = "", val parafArea: String = "",
    val capacityArea: String = "", val structureType: String = "",
    val blockTitle: String = "", val blockCount: String = "",
    val floors: String = "", val units: String = "", val issuer: String = "",
    val permitNumber: String = "", val permitDate: String = "",
    val releaseDate: String = "", val planZone: String = "",
    val usageType: String = ""
)

data class ExportEngineer(val discipline: String = "", val name: String = "", val role: String = "")

data class ExportFee(
    val discipline: String = "", val serviceType: String = "", val stage: String = "",
    val startDate: String = "", val endDate: String = "", val amount: String = "",
    val payStatus: String = "", val confirmStatus: String = "", val amountType: String = "",
    val description: String = ""
)

data class ExportReport(
    val rowNo: String = "", val reportType: String = "", val stage: String = "",
    val engineer: String = "", val discipline: String = "", val visitDate: String = "",
    val ceilingCount: String = "", val indicator: String = "", val hasFile: Boolean = false
)

data class ExportFollowUpEdit(
    val description: String = "", val caseNumberEdit: String = "",
    val ownerEdit: String = "", val addressEdit: String = "",
    val ownerMobileEdit: String = ""
)
