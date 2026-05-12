package com.sandeep.personalfinancecompanion.presentation.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntry
import com.sandeep.personalfinancecompanion.domain.model.UdhaarEntryType
import com.sandeep.personalfinancecompanion.domain.model.UdhaarPersonSummary
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassbookScreen(
    innerPadding: PaddingValues,
    onBack: () -> Unit,
    viewModel: PassbookViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddEntryDialog by remember { mutableStateOf<Boolean?>(null) } // true for Gave, false for Got

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.summary?.person?.name ?: stringResource(R.string.title_passbook)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(bottom = innerPadding.calculateBottomPadding()),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showAddEntryDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_you_gave), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showAddEntryDialog = false },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.btn_you_got), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val summary = uiState.summary
                if (summary == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.error_person_not_found))
                    }
                } else {
                    // Header with net balance
                    NetBalanceHeader(summary = summary, currency = uiState.selectedCurrency)

                    HorizontalDivider()

                    // Ledger table headers
                    PassbookTableHeader()

                    // Ledger entries
                    val passbookEntries = remember(summary.history) {
                        summary.history
                            .sortedBy { it.date }
                            .runningFold(0.0) { balance, entry -> balance + entry.signedBalanceAmount() }
                            .drop(1)
                            .let { balances ->
                                summary.history
                                    .sortedBy { it.date }
                                    .zip(balances)
                                    .sortedByDescending { it.first.date }
                            }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(passbookEntries) { (entry, balanceAfter) ->
                            PassbookEntryRow(
                                entry = entry,
                                balanceAfter = balanceAfter,
                                currency = uiState.selectedCurrency
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }

    showAddEntryDialog?.let { isGiven ->
        QuickEntryDialog(
            isGiven = isGiven,
            currency = uiState.selectedCurrency,
            onDismiss = { showAddEntryDialog = null },
            onSave = { amount, note, date ->
                viewModel.saveEntry(amount, isGiven, note, date)
                showAddEntryDialog = null
            }
        )
    }
}

@Composable
fun NetBalanceHeader(summary: UdhaarPersonSummary, currency: Currency) {
    val color = when {
        summary.netAmount == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        summary.isOwedToYou -> IncomeGreen
        else -> ExpenseRed
    }
    val label = when {
        summary.netAmount == 0.0 -> stringResource(R.string.label_settled)
        summary.isOwedToYou -> stringResource(R.string.label_net_owed_to_you)
        else -> stringResource(R.string.label_net_you_owe)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            CurrencyFormatter.formatAmount(summary.netAmount, currency),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PassbookTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.label_date_entries), modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.label_gave), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ExpenseRed)
        Text(stringResource(R.string.label_got), modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
    }
}

@Composable
fun PassbookEntryRow(
    entry: UdhaarEntry,
    balanceAfter: Double,
    currency: Currency
) {
    val dateFormatter = remember { SimpleDateFormat("dd MMM yy\nhh:mm a", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date and Note
        Column(modifier = Modifier.weight(1.5f)) {
            Text(dateFormatter.format(Date(entry.date)), style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, lineHeight = 14.sp)
            if (entry.note.isNotBlank()) {
                Text(entry.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Gave Column (Red)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (entry.type == UdhaarEntryType.GIVEN || entry.type == UdhaarEntryType.PAID_BACK) {
                Text(
                    CurrencyFormatter.formatAmount(entry.amount, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }

        // Got Column (Green)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (entry.type == UdhaarEntryType.TAKEN || entry.type == UdhaarEntryType.RECEIVED_BACK) {
                Text(
                    CurrencyFormatter.formatAmount(entry.amount, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
            }
        }
    }
    
    // Running Balance line below the row
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        val balLabel = when {
            balanceAfter > 0.0 -> stringResource(R.string.label_bal_you_will_get, CurrencyFormatter.formatAmount(abs(balanceAfter), currency))
            balanceAfter < 0.0 -> stringResource(R.string.label_bal_you_owe, CurrencyFormatter.formatAmount(abs(balanceAfter), currency))
            else -> stringResource(R.string.label_balance_settled)
        }
        Text(balLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), fontSize = 10.sp)
    }
}

@Composable
fun QuickEntryDialog(
    isGiven: Boolean,
    currency: Currency,
    onDismiss: () -> Unit,
    onSave: (Double, String, Long) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val date by remember { mutableStateOf(System.currentTimeMillis()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isGiven) stringResource(R.string.title_you_gave) else stringResource(R.string.title_you_got)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(stringResource(R.string.label_amount, currency.symbol)) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.label_note_optional)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val parsed = amount.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    onSave(parsed, note, date)
                } else {
                    error = "Invalid amount"
                }
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

private fun UdhaarEntry.signedBalanceAmount(): Double {
    return when (type) {
        UdhaarEntryType.GIVEN -> amount
        UdhaarEntryType.PAID_BACK -> amount
        UdhaarEntryType.TAKEN -> -amount
        UdhaarEntryType.RECEIVED_BACK -> -amount
    }
}
