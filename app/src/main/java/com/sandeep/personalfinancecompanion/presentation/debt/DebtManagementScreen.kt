package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtManagementScreen(
    viewModel: DebtManagementViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showRepaymentDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<PeerDebtSummary?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Summary Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                title = "Owed to You",
                amount = uiState.totalLent,
                color = Color(0xFF2E7D32),
                icon = Icons.Default.TrendingUp,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "You Owe",
                amount = uiState.totalBorrowed,
                color = Color(0xFFC62828),
                icon = Icons.Default.TrendingDown,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            "People & Ledgers",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        if (uiState.peerSummaries.isEmpty()) {
            EmptyDebtState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.peerSummaries) { summary ->
                    PeerLedgerCard(
                        summary = summary,
                        onSettleAll = { viewModel.settleAllForPeer(summary.peerName) },
                        onRecordPayment = { showRepaymentDialog = summary }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showRepaymentDialog != null) {
        RepaymentDialog(
            peerSummary = showRepaymentDialog!!,
            onDismiss = { showRepaymentDialog = null },
            onConfirm = { amount, notes ->
                viewModel.recordRepayment(
                    peerName = showRepaymentDialog!!.peerName,
                    amount = amount,
                    isOwedToYou = showRepaymentDialog!!.isOwedToYou,
                    notes = notes
                )
                showRepaymentDialog = null
            }
        )
    }
}

@Composable
fun PeerLedgerCard(
    summary: PeerDebtSummary,
    onSettleAll: () -> Unit,
    onRecordPayment: () -> Unit
) {
    var isExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val statusColor = if (summary.isOwedToYou) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Summary Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        summary.peerName.take(1).uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(summary.peerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        if (summary.isOwedToYou) "Net Owed to You" else "Net You Owe",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "₹${String.format("%.0f", summary.netAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = statusColor
                )
            }

            Spacer(Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRecordPayment,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor.copy(alpha = 0.1f), contentColor = statusColor),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.AddCard, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Payment", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = onSettleAll,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Settle All", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.History,
                        null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded Ledger History
            if (isExpanded) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(Modifier.height(8.dp))
                
                summary.history.forEach { tx ->
                    LedgerEntryRow(tx)
                }
            }
        }
    }
}

@Composable
fun LedgerEntryRow(tx: Transaction) {
    val isIncoming = tx.type == TransactionType.BORROWED || tx.type == TransactionType.LENT_REPAYMENT
    val color = when(tx.type) {
        TransactionType.LENT -> Color(0xFF2E7D32)
        TransactionType.BORROWED -> Color(0xFFC62828)
        TransactionType.LENT_REPAYMENT -> Color(0xFF2E7D32)
        TransactionType.BORROWED_REPAYMENT -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    val dateFormatter = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
    val dateStr = dateFormatter.format(java.util.Date(tx.date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = when(tx.type) {
                    TransactionType.LENT -> "Loan Given"
                    TransactionType.BORROWED -> "Debt Taken"
                    TransactionType.LENT_REPAYMENT -> "Payment Received"
                    TransactionType.BORROWED_REPAYMENT -> "Payment Made"
                    else -> tx.notes
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = "${if (isIncoming) "+" else "-"}₹${tx.amount}",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun RepaymentDialog(
    peerSummary: PeerDebtSummary,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var notes by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("Repayment") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Repayment for ${peerSummary.peerName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    prefix = { Text("₹") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amtValue = amount.toDoubleOrNull() ?: 0.0
                    if (amtValue > 0) onConfirm(amtValue, notes)
                },
                enabled = amount.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(title, style = MaterialTheme.typography.labelMedium, color = color.copy(alpha = 0.7f))
            Text(
                "₹${String.format("%.0f", amount)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun EmptyDebtState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "No outstanding debts",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "Your slate is clean! 🤝",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
