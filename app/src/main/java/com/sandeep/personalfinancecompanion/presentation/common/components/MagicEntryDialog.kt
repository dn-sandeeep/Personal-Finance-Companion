package com.sandeep.personalfinancecompanion.presentation.common.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sandeep.personalfinancecompanion.presentation.common.MagicEntryViewModel
import androidx.compose.ui.res.stringResource
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.util.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MagicEntryDialog(
    viewModel: MagicEntryViewModel,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, type: String, notes: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.title_magic_entry),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Input Field
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.magic_entry_placeholder)) },
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Trigger Speech to Text */ }) {
                            Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.cd_voice_input))
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3
                )

                Spacer(Modifier.height(8.dp))

                // Suggestion Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.suggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { viewModel.onSuggestionClick(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Preview Section
                AnimatedVisibility(visible = uiState.showPreview) {
                    uiState.parsedTransaction?.let { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.magic_entry_predicted_details), style = MaterialTheme.typography.labelMedium)
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "₹${result.amount}",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black
                                        )
                                        Text(
                                            "${result.category.emoji} ${LocalizationUtils.getCategoryName(result.category)}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    
                                    Button(
                                        onClick = {
                                            if (result.amount != null) {
                                                onConfirm(
                                                    result.amount,
                                                    result.category.name,
                                                    result.type.name,
                                                    result.notes
                                                )
                                                onDismiss()
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(stringResource(R.string.btn_save))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Parse Button
                Button(
                    onClick = { viewModel.parseText(context) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.magic_entry_analyze))
                    }
                }
            }
        }
    }
}
