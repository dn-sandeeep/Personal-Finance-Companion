package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import com.sandeep.personalfinancecompanion.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtManagementScreen(
    //onBack: () -> Unit,
    viewModel: DebtManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    var showRepaymentDialog by remember { mutableStateOf<PeerDebtSummary?>(null) }

    if (showRepaymentDialog != null) {
        RecordRepaymentDialog(
            peerName = showRepaymentDialog!!.peerName,
            currency = uiState.selectedCurrency,
            onDismiss = { showRepaymentDialog = null },
            onConfirm = { amount, note ->
                viewModel.recordRepayment(showRepaymentDialog!!.peerName, amount, showRepaymentDialog!!.isOwedToYou, note)
                showRepaymentDialog = null
            }
        )
    }

    Scaffold(
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ──── Summary Cards ────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DebtSummaryCard(
                    title = stringResource(R.string.label_owed_to_you),
                    amount = uiState.totalLent,
                    currency = uiState.selectedCurrency,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f)
                )
                DebtSummaryCard(
                    title = stringResource(R.string.label_you_owe),
                    amount = uiState.totalBorrowed,
                    currency = uiState.selectedCurrency,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.label_people_ledgers),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.peerSummaries.isEmpty()) {
                DebtEmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.peerSummaries) { summary ->
                        PeerDebtCard(
                            summary = summary,
                            currency = uiState.selectedCurrency,
                            onRecordRepayment = { showRepaymentDialog = summary },
                            onSettleAll = { viewModel.settleAllForPeer(summary.peerName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DebtSummaryCard(
    title: String,
    amount: Double,
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.formatAmount(amount, currency),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun PeerDebtCard(
    summary: PeerDebtSummary,
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency,
    onRecordRepayment: () -> Unit,
    onSettleAll: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        summary.peerName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        summary.peerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (summary.isOwedToYou) stringResource(R.string.label_net_owed_to_you) else stringResource(R.string.label_net_you_owe),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = CurrencyFormatter.formatAmount(summary.netAmount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (summary.isOwedToYou) IncomeGreen else ExpenseRed
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(12.dp))

                summary.history.forEach { tx ->
                    TransactionMiniItem(tx, currency)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRecordRepayment,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.label_payment), fontSize = 12.sp)
                    }
                    Button(
                        onClick = onSettleAll,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_settle_all), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionMiniItem(
    tx: Transaction,
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency
) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = when (tx.type) {
                    TransactionType.LENT -> stringResource(R.string.tx_loan_given)
                    TransactionType.BORROWED -> stringResource(R.string.tx_debt_taken)
                    TransactionType.BORROWED_REPAYMENT -> stringResource(R.string.tx_payment_received)
                    TransactionType.LENT_REPAYMENT -> stringResource(R.string.tx_payment_made)
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                dateFormatter.format(Date(tx.date)),
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = CurrencyFormatter.formatAmount(tx.amount, currency),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = when (tx.type) {
                TransactionType.LENT, TransactionType.BORROWED_REPAYMENT -> IncomeGreen
                else -> ExpenseRed
            }
        )
    }
}

@Composable
fun RecordRepaymentDialog(
    peerName: String,
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.title_record_repayment, peerName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.label_amount, currency.symbol)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.label_note)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                amount.toDoubleOrNull()?.let { onConfirm(it, note) }
            }) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}

@Composable
fun DebtEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Handshake,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.msg_no_debts_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            stringResource(R.string.msg_no_debts_subtitle),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}
