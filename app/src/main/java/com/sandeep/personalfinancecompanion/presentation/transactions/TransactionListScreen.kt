package com.sandeep.personalfinancecompanion.presentation.transactions

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.presentation.components.TransactionListItem
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionListScreen(
    snackbarHostState: SnackbarHostState,
    onAddTransaction: () -> Unit,
    onEditTransaction: (String) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        // ──── Headers ────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "CASH FLOW ARCHIVE",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ──── Search & Filters ────
        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = { 
                    Text("Search entries...", color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) 
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = colorScheme.surfaceVariant,
                    focusedContainerColor = colorScheme.surfaceVariant,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    cursorColor = colorScheme.primary
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ──── Activity Chips ────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Chip(
                    text = "All Activity",
                    isSelected = selectedFilter == null,
                    onClick = { viewModel.onFilterChanged(null) }
                )
                Chip(
                    text = "Income",
                    isSelected = selectedFilter == TransactionType.INCOME,
                    onClick = { viewModel.onFilterChanged(TransactionType.INCOME) }
                )
                Chip(
                    text = "Expenses",
                    isSelected = selectedFilter == TransactionType.EXPENSE,
                    onClick = { viewModel.onFilterChanged(TransactionType.EXPENSE) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ──── Transactions List ────
        when {
            listState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "⚠️", style = MaterialTheme.typography.displayLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = listState.error ?: "Failed to load transactions",
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { /* ViewModel logic to retry if needed */ }) {
                        Text("Retry")
                    }
                }
            }
            listState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorScheme.primary)
                }
            }
            listState.transactions.isEmpty() -> {
                EmptyState(
                    emoji = "🔍",
                    title = "No transactions found",
                    subtitle = "Adjust filters to see history"
                )
            }
            else -> {
                // Group by simple date format string to handle today, yesterday, and specific dates
                val groupedByDate = listState.transactions.groupBy {
                    formatDateHeader(it.date)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateContentSize()
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedByDate.forEach { (dateHeader, transactions) ->
                        val netAmount = transactions.sumOf { 
                            if (it.type == TransactionType.INCOME) it.amount else -it.amount 
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateHeader.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${CurrencyFormatter.formatAmount(kotlin.math.abs(netAmount), listState.selectedCurrency)} Net",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (netAmount >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }

                        items(items = transactions, key = { it.id }) { transaction ->
                            SwipeableTransactionItem(
                                transaction = transaction,
                                currency = listState.selectedCurrency,
                                onDelete = {
                                    viewModel.deleteTransaction(transaction.id)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Transaction deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.addTransaction(
                                                amount = transaction.amount,
                                                type = transaction.type,
                                                category = transaction.category,
                                                notes = transaction.notes,
                                                date = transaction.date
                                            )
                                        }
                                    }
                                },
                                onEdit = { onEditTransaction(transaction.id) }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) } // Bottom nav & FAB padding
                }
            }
        }
    }
}

@Composable
private fun Chip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val bgColor = if (isSelected) colorScheme.primary else colorScheme.primaryContainer
    val textColor = if (isSelected) colorScheme.onPrimary else colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableTransactionItem(
    transaction: Transaction,
    currency: Currency,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                label = "swipe_color"
            )

            val alignment = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.Center
            }

            val icon = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                else -> null
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                            MaterialTheme.colorScheme.onError 
                        else 
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) {
        TransactionListItem(
            transaction = transaction,
            currency = currency
        )
    }
}

private fun formatDateHeader(dateMillis: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = System.currentTimeMillis()
    val todayYear = cal.get(Calendar.YEAR)
    val todayDay = cal.get(Calendar.DAY_OF_YEAR)

    cal.timeInMillis = dateMillis
    val txYear = cal.get(Calendar.YEAR)
    val txDay = cal.get(Calendar.DAY_OF_YEAR)

    val format = SimpleDateFormat("MMM dd", Locale.getDefault())
    val dateString = format.format(Date(dateMillis)).uppercase()

    return if (todayYear == txYear) {
        when (todayDay - txDay) {
            0 -> "TODAY, $dateString"
            1 -> "YESTERDAY, $dateString"
            else -> dateString
        }
    } else {
        SimpleDateFormat("yyyy, MMM dd", Locale.getDefault()).format(Date(dateMillis)).uppercase()
    }
}
