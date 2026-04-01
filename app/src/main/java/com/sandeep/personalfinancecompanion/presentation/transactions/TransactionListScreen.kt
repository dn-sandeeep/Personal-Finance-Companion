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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.presentation.components.TransactionListItem
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionListScreen(
    snackbarHostState: SnackbarHostState,
    onAddTransaction: () -> Unit,
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
        // ──── Top Bar ────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = colorScheme.surface,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "The Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { }) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

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
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                fontSize = 34.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ──── Search & Filters ────
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
                shape = RoundedCornerShape(8.dp),
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

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Filters Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.secondaryContainer)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filters", fontWeight = FontWeight.SemiBold, color = colorScheme.onSecondaryContainer)
                }

                // Calendar Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colorScheme.secondaryContainer)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                    text = "${if (netAmount >= 0) "+" else "-"}\u20B9${String.format("%,.2f", kotlin.math.abs(netAmount))} Net",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        items(items = transactions, key = { it.id }) { transaction ->
                            SwipeableTransactionItem(
                                transaction = transaction,
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
                                }
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
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error
                    else -> Color.Transparent
                },
                label = "swipe_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        TransactionListItem(transaction = transaction)
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
