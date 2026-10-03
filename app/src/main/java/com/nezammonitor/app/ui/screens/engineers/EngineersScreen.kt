package com.nezammonitor.app.ui.screens.engineers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.data.SinaEngineerStore
import com.nezammonitor.app.data.formatSinaLevels
import com.nezammonitor.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineersScreen(viewModel: MainViewModel, onCaseClick: (String) -> Unit) {
    val cases by viewModel.cases.collectAsState()
    val engineers by viewModel.engineers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Case-centric: group engineers by case
    val caseGroups = remember(cases, engineers, searchQuery) {
        cases.map { case ->
            val caseEngineers = engineers.filter { it.caseNumber == case.caseNumber }
            Pair(case, caseEngineers)
        }.filter { (_, engs) -> engs.isNotEmpty() }
         .filter { (case, _) ->
            searchQuery.isBlank() ||
            case.caseNumber.contains(searchQuery, true) ||
            case.owner.contains(searchQuery, true)
        }.sortedBy { it.first.caseNumber }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("جستجوی پرونده / مالک...") },
            singleLine = true
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                "${caseGroups.size} پرونده دارای مهندس",
                modifier = Modifier.padding(12.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(caseGroups, key = { it.first.caseNumber }) { (case, caseEngineers) ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onCaseClick(case.caseNumber) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(case.owner, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("پرونده: ${case.caseNumber}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Text(
                                    "${caseEngineers.size} مهندس",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        caseEngineers.take(4).forEach { eng ->
                            val sina = remember(eng.name) { SinaEngineerStore.findByName(context, eng.name) }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("• ${eng.name} (${eng.discipline})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (sina != null) Text("  ${formatSinaLevels(sina)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                val phone = sina?.phone?.ifBlank { null } ?: eng.phone.ifBlank { null }
                                if (!phone.isNullOrBlank()) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = "تماس",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp).clickable {
                                            val c = phone.replace(Regex("[^0-9+]"), "")
                                            try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$c"))) } catch (_: Exception) {}
                                        }
                                    )
                                }
                            }
                        }
                        if (caseEngineers.size > 4) {
                            Text("+ ${caseEngineers.size - 4} مهندس دیگر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
