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
                return@withContext ImportResult(false, "Missing manifest.json")
            }
            
            val manifest = gson.fromJson(manifestFile.readText(), ManifestData::class.java)
            if (manifest.format != "NezamMonitor.AndroidData") {
                return@withContext ImportResult(false, "Invalid package format")
            }
            
            val dataFile = File(tempDir, "data.json")
            if (!dataFile.exists()) {
                return@withContext ImportResult(false, "Missing data.json")
            }
            
            val exportData = gson.fromJson(dataFile.readText(), ExportData::class.java)
            
            // Transactional import
            db.withTransaction {
                dao.deleteAllCases()
                dao.deleteAllSpecifications()
                dao.deleteAllEngineers()
                dao.deleteAllFees()
                dao.deleteAllReports()
                dao.deleteAllFollowUpEdits()
                
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
                    SpecificationEntity(
                        caseNumber = ec.caseNumber,
                        address = ec.specification?.address ?: "",
                        buildingGroup = ec.specification?.buildingGroup ?: "",
                        renovationCode = ec.specification?.renovationCode ?: "",
                        planInstructionNo = ec.specification?.planInstructionNo ?: "",
                        planInstructionType = ec.specification?.planInstructionType ?: "",
                        planInstructionDate = ec.specification?.planInstructionDate ?: "",
                        landArea = ec.specification?.landArea ?: "",
                        parafArea = ec.specification?.parafArea ?: "",
                        capacityArea = ec.specification?.capacityArea ?: "",
                        structureType = ec.specification?.structureType ?: "",
                        blockTitle = ec.specification?.blockTitle ?: "",
                        blockCount = ec.specification?.blockCount ?: "",
                        floors = ec.specification?.floors ?: "",
                        units = ec.specification?.units ?: "",
                        issuer = ec.specification?.issuer ?: "",
                        permitNumber = ec.specification?.permitNumber ?: "",
                        permitDate = ec.specification?.permitDate ?: "",
                        releaseDate = ec.specification?.releaseDate ?: "",
                        planZone = ec.specification?.planZone ?: "",
                        usageType = ec.specification?.usageType ?: ""
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
                        FeeEntity(caseNumber = ec.caseNumber, discipline = ef.discipline, serviceType = ef.serviceType,
                            stage = ef.stage, startDate = ef.startDate, endDate = ef.endDate, amount = ef.amount,
                            payStatus = ef.payStatus, confirmStatus = ef.confirmStatus, amountType = ef.amountType, description = ef.description)
                    }
                }
                dao.insertFees(fees)
                
                val reports = exportData.cases.flatMap { ec ->
                    ec.reports.map { er ->
                        ReportEntity(caseNumber = ec.caseNumber, rowNo = er.rowNo, reportType = er.reportType,
                            stage = er.stage, engineer = er.engineer, discipline = er.discipline,
                            visitDate = er.visitDate, ceilingCount = er.ceilingCount, indicator = er.indicator, hasFile = er.hasFile)
                    }
                }
                dao.insertReports(reports)
                
                val edits = exportData.cases.filter { it.followUpEdit != null }.map { ec ->
                    FollowUpEditEntity(caseNumber = ec.caseNumber, description = ec.followUpEdit?.description ?: "",
                        caseNumberEdit = ec.followUpEdit?.caseNumberEdit ?: "", ownerEdit = ec.followUpEdit?.ownerEdit ?: "",
                        addressEdit = ec.followUpEdit?.addressEdit ?: "", ownerMobileEdit = ec.followUpEdit?.ownerMobileEdit ?: "")
                }
                edits.forEach { dao.insertFollowUpEdit(it) }
            }
            
            val casesCount = dao.getCasesCount()
            val engineersCount = dao.getEngineersCount()
            val feesCount = dao.getFeesCount()
            val reportsCount = dao.getReportsCount()
            
            tempFile.delete()
            tempDir.deleteRecursively()
            
            ImportResult(true, "Import completed successfully", casesCount, engineersCount, feesCount, reportsCount)
        } catch (e: Exception) {
            ImportResult(false, "Import failed: ${e.message}")
        }
    }
}

data class ManifestData(val format: String = "", val version: String = "", val formatVersion: String = "", val createdAt: String = "",
    val sourceApplication: String = "", val sourceApplicationVersion: String = "", val recordCount: Int = 0,
    val schemaVersion: String = "", val checksum: String = "", val exportedEntities: List<String> = emptyList())

data class ExportData(val cases: List<ExportCase> = emptyList())

data class ExportCase(val caseNumber: String = "", val serial: String = "", val owner: String = "", val ownerMobile: String = "",
    val responsibility: String = "", val capacityDate: String = "", val office: String = "",
    val reportDate1: String = "", val reportDate2: String = "", val reportDate3: String = "",
    val specification: ExportSpecification? = null, val engineers: List<ExportEngineer> = emptyList(),
    val fees: List<ExportFee> = emptyList(), val reports: List<ExportReport> = emptyList(), val followUpEdit: ExportFollowUpEdit? = null)

data class ExportSpecification(val address: String = "", val buildingGroup: String = "", val renovationCode: String = "",
    val planInstructionNo: String = "", val planInstructionType: String = "", val planInstructionDate: String = "",
    val landArea: String = "", val parafArea: String = "", val capacityArea: String = "", val structureType: String = "",
    val blockTitle: String = "", val blockCount: String = "", val floors: String = "", val units: String = "",
    val issuer: String = "", val permitNumber: String = "", val permitDate: String = "", val releaseDate: String = "",
    val planZone: String = "", val usageType: String = "")

data class ExportEngineer(val discipline: String = "", val name: String = "", val role: String = "")

data class ExportFee(val discipline: String = "", val serviceType: String = "", val stage: String = "",
    val startDate: String = "", val endDate: String = "", val amount: String = "",
    val payStatus: String = "", val confirmStatus: String = "", val amountType: String = "", val description: String = "")

data class ExportReport(val rowNo: String = "", val reportType: String = "", val stage: String = "",
    val engineer: String = "", val discipline: String = "", val visitDate: String = "",
    val ceilingCount: String = "", val indicator: String = "", val hasFile: Boolean = false)

data class ExportFollowUpEdit(val description: String = "", val caseNumberEdit: String = "",
    val ownerEdit: String = "", val addressEdit: String = "", val ownerMobileEdit: String = "")
