package com.sandeep.personalfinancecompanion.voiceagent.presentation

import androidx.activity.compose.rememberLauncherForActivityResult

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAgentDialog(
    viewModel: VoiceAgentViewModel,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, category: String, type: String, notes: String) -> Unit
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
                        "Voice Agent",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Voice Pulse Animation
                if (uiState.isListening) {
                    VoicePulseAnimation()
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
                            contentDescription = "Start Listening",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (uiState.isListening) "Listening..." else "Tap the mic to speak",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.isListening) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                // Input Preview (Updated by voice results)
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("What did you spend today?") },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Your Entry") },
                    maxLines = 3
                )

                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.0f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { 
                            if (uiState.parsedResult?.amount != null) {
                                val res = uiState.parsedResult!!
                                onConfirm(res.amount!!, res.category.name, res.type.name, res.notes)
                                onDismiss()
                            } else {
                                viewModel.parseAndProcess(context)
                            }
                        },
                        modifier = Modifier.weight(1.0f),
                        enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Process")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoicePulseAnimation() {
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
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
        IconButton(
            onClick = { /* Stop handled by manager/UI */ },
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                Icons.Default.Stop,
                contentDescription = "Stop",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
    }
}
