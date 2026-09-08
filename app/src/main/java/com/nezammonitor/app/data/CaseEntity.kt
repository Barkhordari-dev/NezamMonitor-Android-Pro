package com.nezammonitor.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String = "",
    val serial: String = "",
    val owner: String = "",
    val ownerMobile: String = "",
    val responsibility: String = "",
    val capacityDate: String = "",
    val office: String = "",
    val reportDate1: String = "",
    val reportDate2: String = "",
    val reportDate3: String = ""
)

@Entity(tableName = "specifications")
data class SpecificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String = "",
    val address: String = "",
    val buildingGroup: String = "",
    val renovationCode: String = "",
    val planInstructionNo: String = "",
    val planInstructionType: String = "",
    val planInstructionDate: String = "",
    val landArea: String = "",
    val parafArea: String = "",
    val capacityArea: String = "",
    val structureType: String = "",
    val blockTitle: String = "",
    val blockCount: String = "",
    val floors: String = "",
    val units: String = "",
    val issuer: String = "",
    val permitNumber: String = "",
    val permitDate: String = "",
    val releaseDate: String = "",
    val planZone: String = "",
    val usageType: String = ""
)

@Entity(tableName = "engineers")
data class EngineerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String = "",
    val discipline: String = "",
    val name: String = "",
    val role: String = ""
)

@Entity(tableName = "fees")
data class FeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String = "",
    val discipline: String = "",
    val serviceType: String = "",
    val stage: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val amount: String = "",
    val payStatus: String = "",
    val confirmStatus: String = "",
    val amountType: String = "",
    val description: String = ""
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseNumber: String = "",
    val rowNo: String = "",
    val reportType: String = "",
    val stage: String = "",
    val engineer: String = "",
    val discipline: String = "",
    val visitDate: String = "",
    val ceilingCount: String = "",
    val indicator: String = "",
    val hasFile: Boolean = false
)

@Entity(tableName = "followup_edits")
data class FollowUpEditEntity(
    @PrimaryKey val caseNumber: String = "",
    val description: String = "",
    val caseNumberEdit: String = "",
    val ownerEdit: String = "",
    val addressEdit: String = "",
    val ownerMobileEdit: String = ""
)
