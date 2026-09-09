package com.nezammonitor.app.ui.screens.engineers

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
fun EngineerDetailScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val case by viewModel.caseDetail.collectAsState()
    val engineers by viewModel.caseEngineers.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("مهندسین پرونده", fontSize = 16.sp)
                        Text(case?.caseNumber ?: "", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Case header
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مالک: ${case?.owner ?: ""}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("پرونده: ${case?.caseNumber ?: ""}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        if (case?.ownerMobile?.isNotBlank() == true) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("همراه: ${case?.ownerMobile}", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "تماس",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val cleaned = case?.ownerMobile?.replace(Regex("[^0-9+]"), "")
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:$cleaned")
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Engineers
            if (engineers.isNotEmpty()) {
                item {
                    Text("مهندسین (${engineers.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(vertical = 4.dp))
                }
                items(engineers) { eng ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(eng.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("رشته: ${eng.discipline}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (eng.role.isNotBlank()) {
                                    Text("نقش: ${eng.role}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Text("مهندسی یافت نشد", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
