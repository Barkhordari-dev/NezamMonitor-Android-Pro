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
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val followUpItems = remember(cases, edits, searchQuery) {
        val items = cases.map { case ->
            val edit = edits.find { it.caseNumber == case.caseNumber }
            FollowUpItem(
                caseNumber = edit?.caseNumberEdit?.ifBlank { case.caseNumber } ?: case.caseNumber,
                owner = edit?.ownerEdit?.ifBlank { case.owner } ?: case.owner,
                mobile = edit?.ownerMobileEdit?.ifBlank { case.ownerMobile } ?: case.ownerMobile,
                address = edit?.addressEdit?.ifBlank { "" } ?: "",
                description = edit?.description ?: ""
            )
        }
        if (searchQuery.isBlank()) items
        else items.filter {
            it.caseNumber.contains(searchQuery, true) ||
            it.owner.contains(searchQuery, true) ||
            it.description.contains(searchQuery, true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("جستجوی پیگیری...") },
            singleLine = true
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(followUpItems, key = { it.caseNumber }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.caseNumber, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (item.mobile.isNotBlank()) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "تماس",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val intent = Intent(Intent.ACTION_DIAL)
                                        intent.data = Uri.parse("tel:${item.mobile}")
                                        context.startActivity(intent)
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مالک: ${item.owner}", fontSize = 14.sp)
                        if (item.address.isNotBlank()) {
                            Text("آدرس: ${item.address}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("توضیحات: ${item.description}", fontSize = 13.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
}

data class FollowUpItem(
    val caseNumber: String,
    val owner: String,
    val mobile: String,
    val address: String,
    val description: String
)
