package com.nezammonitor.app.ui.screens.cases

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
fun CasesScreen(viewModel: MainViewModel, onCaseClick: (String) -> Unit) {
    val cases by viewModel.cases.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchCases(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجو...") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cases, key = { it.caseNumber }) { case ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCaseClick(case.caseNumber) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(case.caseNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (case.ownerMobile.isNotBlank()) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "تماس",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:${case.ownerMobile}")
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مالک: ${case.owner}", fontSize = 14.sp)
                        Text("دفتر: ${case.office}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("مسئولیت: ${case.responsibility}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
