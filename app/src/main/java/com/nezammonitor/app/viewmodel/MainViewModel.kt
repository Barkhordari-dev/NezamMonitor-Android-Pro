package com.nezammonitor.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nezammonitor.app.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = NezamDatabase.getDatabase(application)
    private val dao = db.nezamDao()

    // Cases
    private val _cases = MutableStateFlow<List<CaseEntity>>(emptyList())
    val cases: StateFlow<List<CaseEntity>> = _cases.asStateFlow()

    private val _caseDetail = MutableStateFlow<CaseEntity?>(null)
    val caseDetail: StateFlow<CaseEntity?> = _caseDetail.asStateFlow()

    private val _caseSpec = MutableStateFlow<SpecificationEntity?>(null)
    val caseSpec: StateFlow<SpecificationEntity?> = _caseSpec.asStateFlow()

    private val _caseEngineers = MutableStateFlow<List<EngineerEntity>>(emptyList())
    val caseEngineers: StateFlow<List<EngineerEntity>> = _caseEngineers.asStateFlow()

    private val _caseFees = MutableStateFlow<List<FeeEntity>>(emptyList())
    val caseFees: StateFlow<List<FeeEntity>> = _caseFees.asStateFlow()

    private val _caseReports = MutableStateFlow<List<ReportEntity>>(emptyList())
    val caseReports: StateFlow<List<ReportEntity>> = _caseReports.asStateFlow()

    // Engineers
    private val _engineers = MutableStateFlow<List<EngineerEntity>>(emptyList())
    val engineers: StateFlow<List<EngineerEntity>> = _engineers.asStateFlow()

    // Fees
    private val _fees = MutableStateFlow<List<FeeEntity>>(emptyList())
    val fees: StateFlow<List<FeeEntity>> = _fees.asStateFlow()

    // Reports
    private val _reports = MutableStateFlow<List<ReportEntity>>(emptyList())
    val reports: StateFlow<List<ReportEntity>> = _reports.asStateFlow()

    // FollowUp
    private val _followUpEdits = MutableStateFlow<List<FollowUpEditEntity>>(emptyList())
    val followUpEdits: StateFlow<List<FollowUpEditEntity>> = _followUpEdits.asStateFlow()

    // Import
    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    init {
        loadAllData()
    }

    private fun loadAllData() {
        viewModelScope.launch {
            dao.getAllCases().collect { _cases.value = it }
        }
        viewModelScope.launch {
            dao.getAllEngineers().collect { _engineers.value = it }
        }
        viewModelScope.launch {
            dao.getAllFees().collect { _fees.value = it }
        }
        viewModelScope.launch {
            dao.getAllReports().collect { _reports.value = it }
        }
        viewModelScope.launch {
            dao.getAllFollowUpEdits().collect { _followUpEdits.value = it }
        }
    }

    fun loadCaseDetail(caseNumber: String) {
        viewModelScope.launch {
            _caseDetail.value = cases.value.find { it.caseNumber == caseNumber }
            _caseSpec.value = dao.getSpecification(caseNumber)
            _caseEngineers.value = dao.getEngineersForCase(caseNumber)
            _caseFees.value = dao.getFeesForCase(caseNumber)
            _caseReports.value = dao.getReportsForCase(caseNumber)
        }
    }

    fun searchCases(query: String) {
        viewModelScope.launch {
            dao.searchCases(query).collect { _cases.value = it }
        }
    }

    fun importData(result: ImportResult) {
        _importResult.value = result
        loadAllData()
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun saveFollowUpEdit(edit: FollowUpEditEntity) {
        viewModelScope.launch {
            dao.insertFollowUpEdit(edit)
        }
    }
}
