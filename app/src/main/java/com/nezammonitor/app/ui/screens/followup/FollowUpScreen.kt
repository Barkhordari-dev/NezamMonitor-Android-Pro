package com.nezammonitor.app.ui.screens.followup

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowUpScreen(viewModel: MainViewModel) {
    val cases by viewModel.cases.collectAsState()
    val edits by viewModel.followUpEdits.collectAsState()
    val reports by viewModel.reports.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val items = remember(cases, edits, reports, searchQuery) {
        val result = cases.map { case ->
            val edit = edits.find { it.caseNumber == case.caseNumber }
            val reportCount = reports.count { it.caseNumber == case.caseNumber }
            FollowUpDisplayItem(
                caseNumber = edit?.caseNumberEdit?.ifBlank { case.caseNumber } ?: case.caseNumber,
                originalCaseNumber = case.caseNumber,
                owner = edit?.ownerEdit?.ifBlank { case.owner } ?: case.owner,
                mobile = edit?.ownerMobileEdit?.ifBlank { case.ownerMobile } ?: case.ownerMobile,
                address = edit?.addressEdit?.ifBlank { "" } ?: "",
                description = edit?.description ?: "",
                reportCount = reportCount
            )
        }
        if (searchQuery.isBlank()) result
        else result.filter {
            it.caseNumber.contains(searchQuery, true) ||
            it.owner.contains(searchQuery, true) ||
            it.mobile.contains(searchQuery, true) ||
            it.description.contains(searchQuery, true)
        }.sortedBy { it.caseNumber }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی پرونده / مالک / توضیحات...") },
            singleLine = true
        )

        // Summary
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${items.size} پرونده قابل پیگیری", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                val withDesc = items.count { it.description.isNotBlank() }
                Text("با توضیحات: $withDesc", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items, key = { it.originalCaseNumber }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Header row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.caseNumber, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("مالک: ${item.owner}", fontSize = 13.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Report count badge
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Text(
                                        "${item.reportCount} گزارش",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (item.mobile.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        val cleaned = item.mobile.replace(Regex("[^0-9+]"), "")
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:$cleaned")
                                        context.startActivity(intent)
                                    }) {
                                        Icon(
                                            Icons.Default.Phone,
                                            contentDescription = "تماس",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Details
                        if (item.mobile.isNotBlank()) {
                            Text("همراه: ${item.mobile}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.address.isNotBlank()) {
                            Text("آدرس: ${item.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    "📝 ${item.description}",
                                    modifier = Modifier.padding(8.dp),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class FollowUpDisplayItem(
    val caseNumber: String,
    val originalCaseNumber: String,
    val owner: String,
    val mobile: String,
    val address: String,
    val description: String,
    val reportCount: Int
)
