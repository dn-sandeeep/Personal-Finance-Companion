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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType

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
            if (initialType == TransactionType.INCOME) Category.SALARY else Category.FOOD
        )
    }
    var notes by remember { mutableStateOf("") }
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
                text = if (transactionId == null) "NEW ENTRY" else "EDIT ENTRY",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Type Selector (Segmented Button)
        Text(
            text = "Transaction Type",
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
                Text("💰 Income")
            }
            SegmentedButton(
                selected = type == TransactionType.EXPENSE,
                onClick = {
                    type = TransactionType.EXPENSE
                    selectedCategory = Category.FOOD
                },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("💸 Expense")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Amount
        Text(
            text = "Amount (${currency.symbol})",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = amount,
            onValueChange = {
                amount = it.filter { char -> char.isDigit() || char == '.' }
                amountError = null
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Enter amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it) } }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = "${selectedCategory.emoji} ${selectedCategory.displayName}",
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
                        text = { Text("${category.emoji} ${category.displayName}") },
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
            text = "Notes (optional)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Add a note...") },
            shape = RoundedCornerShape(14.dp),
            minLines = 2,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = {
                val parsedAmount = amount.toDoubleOrNull()
                when {
                    parsedAmount == null || parsedAmount <= 0 -> {
                        amountError = "Please enter a valid amount greater than 0"
                    }
                    else -> {
                        val transaction = Transaction(
                            id = transactionId ?: java.util.UUID.randomUUID().toString(),
                            amount = parsedAmount,
                            type = type,
                            category = selectedCategory,
                            notes = notes,
                            date = date
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
                text = if (transactionId == null) "Save Transaction" else "Update Transaction",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
