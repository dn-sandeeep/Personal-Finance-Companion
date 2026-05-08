package com.sandeep.personalfinancecompanion.voiceagent.presentation

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.sandeep.personalfinancecompanion.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAgentDialog(
    viewModel: VoiceAgentViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startListening()
            }
        }
    )

    // Initialize voice manager
    LaunchedEffect(Unit) {
        viewModel.initVoiceManager(context)
    }

    // Close on success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onDismiss()
        }
    }

    var showExitConfirmation by remember { mutableStateOf(false) }

    // Logic to handle intent to close
    val handleDismiss = {
        if (uiState.inputText.isNotBlank() || uiState.parsedResults.isNotEmpty()) {
            showExitConfirmation = true
        } else {
            onDismiss()
        }
    }

    if (showExitConfirmation) {
        AlertDialog(
            onDismissRequest = { showExitConfirmation = false },
            title = { Text(stringResource(R.string.title_discard_entry)) },
            text = { Text(stringResource(R.string.msg_discard_entry_confirm)) },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_discard), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmation = false }) { Text(stringResource(R.string.btn_keep)) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Dialog(
        onDismissRequest = handleDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .imePadding(),
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
                        stringResource(R.string.title_voice_agent),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Voice Pulse Animation
                if (uiState.isListening) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        VoicePulseAnimation(onStop = viewModel::stopListening)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.label_tap_to_stop),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    IconButton(
                        onClick = { 
                            val permission = android.Manifest.permission.RECORD_AUDIO
                            val isGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                                context, permission
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            
                            if (isGranted) {
                                viewModel.startListening()
                            } else {
                                permissionLauncher.launch(permission)
                            }
                        },
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = stringResource(R.string.cd_start_listening),
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (uiState.isListening) stringResource(R.string.status_listening) else if (uiState.isLoading) stringResource(R.string.status_analyzing_speech) else stringResource(R.string.msg_tap_mic_to_speak),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // Input Preview (Only show when not showing results)
                AnimatedVisibility(
                    visible = uiState.parsedResults.isEmpty() && !uiState.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = viewModel::onTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.magic_entry_placeholder)) },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text(stringResource(R.string.label_your_entry)) },
                        maxLines = 3
                    )
                }

                // AI Structured Preview (Multi-Entry Support)
                AnimatedVisibility(
                    visible = uiState.parsedResults.isNotEmpty() && !uiState.isLoading,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.msg_tx_detected, uiState.parsedResults.size),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        uiState.parsedResults.forEach { result ->
                            ParsedResultPreview(result = result)
                        }
                    }
                }

                if (uiState.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isPreview = uiState.parsedResults.isNotEmpty()
                    
                    OutlinedButton(
                        onClick = if (isPreview) viewModel::backToEdit else handleDismiss,
                        modifier = Modifier.weight(1.0f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(if (isPreview) Icons.Default.Edit else Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isPreview) stringResource(R.string.btn_edit_text) else stringResource(R.string.btn_cancel))
                    }

                    Button(
                        onClick = { 
                            if (isPreview) {
                                viewModel.saveTransactions(uiState.parsedResults)
                            } else {
                                viewModel.parseAndProcess(context)
                            }
                        },
                        modifier = Modifier.weight(1.0f),
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (isPreview) Icons.Default.Check else Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (isPreview) stringResource(R.string.btn_confirm_all) else stringResource(R.string.btn_process))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePulseAnimation(onStop: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
        IconButton(
            onClick = onStop,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = stringResource(R.string.cd_stop_listening),
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun ParsedResultPreview(result: com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.label_amount_simple),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "₹${result.amount ?: 0.0}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.label_category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = { },
                    label = { Text(result.category.name) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Label,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.label_type),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                val typeColor = when (result.type) {
                    TransactionType.EXPENSE -> Color(0xFFC62828)
                    TransactionType.INCOME -> Color(0xFF2E7D32)
                    TransactionType.LENT -> Color(0xFFE65100) // Orange
                    TransactionType.BORROWED -> Color(0xFF4A148C) // Purple
                    TransactionType.BORROWED_REPAYMENT -> Color(0xFF4A148C)
                    TransactionType.LENT_REPAYMENT -> Color(0xFFE65100)
                }
                val bgColor = when (result.type) {
                    TransactionType.EXPENSE -> Color(0xFFFFEBEE)
                    TransactionType.INCOME -> Color(0xFFE8F5E9)
                    TransactionType.LENT -> Color(0xFFFFF3E0)
                    TransactionType.BORROWED -> Color(0xFFF3E5F5)
                    TransactionType.BORROWED_REPAYMENT -> Color(0xFFF3E5F5)
                    TransactionType.LENT_REPAYMENT -> Color(0xFFFFF3E0)
                }

                Surface(
                    color = bgColor,
                    contentColor = typeColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.type.name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (result.peerName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.label_person),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = result.peerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
