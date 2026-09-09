package com.nezammonitor.app.ui.screens.engineers

import android.content.Intent
import android.net.Uri
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
fun EngineersScreen(viewModel: MainViewModel) {
    val engineers by viewModel.engineers.collectAsState()
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val filtered = remember(engineers, cases, searchQuery) {
        if (searchQuery.isBlank()) engineers
        else engineers.filter { eng ->
            eng.name.contains(searchQuery, true) ||
            eng.discipline.contains(searchQuery, true) ||
            eng.caseNumber.contains(searchQuery, true) ||
            eng.role.contains(searchQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی مهندس / رشته / پرونده...") },
            singleLine = true
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Text(
                "${filtered.size} مهندس",
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
            items(filtered, key = { "${it.caseNumber}_${it.name}_${it.discipline}" }) { eng ->
                val case = cases.find { it.caseNumber == eng.caseNumber }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(eng.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("رشته: ${eng.discipline}", fontSize = 13.sp)
                            }
                            if (case?.ownerMobile?.isNotBlank() == true) {
                                IconButton(onClick = {
                                    val cleaned = case.ownerMobile.replace(Regex("[^0-9+]"), "")
                                    val intent = Intent(Intent.ACTION_DIAL)
                                    intent.data = Uri.parse("tel:$cleaned")
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.Default.Phone, contentDescription = "تماس", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                        if (eng.role.isNotBlank()) {
                            Text("نقش: ${eng.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("پرونده: ${eng.caseNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (case != null) {
                            Text("مالک: ${case.owner}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
