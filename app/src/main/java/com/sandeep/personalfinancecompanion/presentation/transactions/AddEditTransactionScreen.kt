package com.sandeep.personalfinancecompanion.presentation.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.util.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: String? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onSave: (Transaction) -> Unit,
    onBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(initialType) }
    var selectedCategory by remember {
        mutableStateOf(
            when (initialType) {
                TransactionType.INCOME -> Category.SALARY
                TransactionType.BORROWED, TransactionType.LENT, 
                TransactionType.BORROWED_REPAYMENT, TransactionType.LENT_REPAYMENT -> Category.UDHAAR
                else -> Category.FOOD
            }
        )
    }
    var notes by remember { mutableStateOf("") }
    var peerName by remember { mutableStateOf("") }
    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var expanded by remember { mutableStateOf(false) }

    // Validation
    var amountError by remember { mutableStateOf<String?>(null) }

    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val currency = listState.selectedCurrency

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            val transaction = viewModel.getTransactionById(transactionId)
            if (transaction != null) {
                amount = transaction.amount.toString()
                type = transaction.type
                selectedCategory = transaction.category
                notes = transaction.notes
                date = transaction.date
                peerName = transaction.peerName ?: ""
            }
        }
    }

    val categories = if (type == TransactionType.INCOME) {
        Category.incomeCategories()
    } else {
        Category.expenseCategories()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (transactionId == null) stringResource(R.string.label_new_entry) else stringResource(R.string.label_edit_entry),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector (Segmented Button)
        Text(
            text = stringResource(R.string.label_tx_type),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = type == TransactionType.INCOME,
                onClick = {
                    type = TransactionType.INCOME
                    selectedCategory = Category.SALARY
                },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text(stringResource(R.string.label_income), style = MaterialTheme.typography.labelSmall)
            }
            SegmentedButton(
                selected = type == TransactionType.EXPENSE,
                onClick = {
                    type = TransactionType.EXPENSE
                    selectedCategory = Category.FOOD
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text(stringResource(R.string.label_expense), style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount
        Text(
            text = stringResource(R.string.label_amount, currency.symbol),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = { input ->
                val filtered = input.filter { char -> char.isDigit() || char == '.' }
                // Basic length check to prevent massive strings
                if (filtered.length <= 13) {
                    val parts = filtered.split(".")
                    if (parts.size <= 2 && (parts.size < 2 || parts[1].length <= 2)) {
                        amount = filtered
                        amountError = null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.placeholder_enter_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        Text(
            text = stringResource(R.string.label_category),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${selectedCategory.emoji} ${LocalizationUtils.getCategoryName(selectedCategory)}",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(14.dp)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(14.dp)
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text("${category.emoji} ${LocalizationUtils.getCategoryName(category)}") },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes
        Text(
            text = stringResource(R.string.label_notes_optional),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.placeholder_add_note)) },
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 4
        )

        // Person Name (Only for Udhaar types)
        if (type == TransactionType.BORROWED || type == TransactionType.LENT) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (type == TransactionType.BORROWED) stringResource(R.string.label_borrowed_from) else stringResource(R.string.label_lent_to),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = peerName,
                onValueChange = { peerName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.hint_debtor_name)) },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        val errorInvalidAmount = stringResource(R.string.error_invalid_amount)
        val errorAmountTooLarge = stringResource(R.string.error_amount_too_large)
        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                when {
                    parsedAmount == null || parsedAmount <= 0 -> {
                        amountError = errorInvalidAmount
                    }
                    parsedAmount > 1000000000.0 -> {
                        amountError = errorAmountTooLarge
                    }
                    else -> {
                        val transaction = Transaction(
                            id = transactionId ?: java.util.UUID.randomUUID().toString(),
                            amount = parsedAmount,
                            type = type,
                            category = selectedCategory,
                            notes = notes,
                            date = date,
                            peerName = if (type == TransactionType.BORROWED || type == TransactionType.LENT) peerName else null,
                            isSettled = false
                        )
                        onSave(transaction)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (transactionId == null) stringResource(R.string.btn_save_tx) else stringResource(R.string.btn_update_tx),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
