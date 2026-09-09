package com.nezammonitor.app.ui.screens.engineers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nezammonitor.app.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineersScreen(viewModel: MainViewModel) {
    val engineers by viewModel.engineers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredEngineers = remember(engineers, searchQuery) {
        if (searchQuery.isBlank()) engineers
        else engineers.filter {
            it.name.contains(searchQuery, true) ||
            it.discipline.contains(searchQuery, true) ||
            it.caseNumber.contains(searchQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی مهندس...") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredEngineers, key = { "${it.caseNumber}_${it.name}_${it.discipline}" }) { eng ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(eng.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("رشته: ${eng.discipline}", fontSize = 14.sp)
                        Text("پرونده: ${eng.caseNumber}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (eng.role.isNotBlank()) {
                            Text("نقش: ${eng.role}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
