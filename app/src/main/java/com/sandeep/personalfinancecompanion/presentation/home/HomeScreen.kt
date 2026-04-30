package com.sandeep.personalfinancecompanion.presentation.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.presentation.components.*
import com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.util.LocalizationUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToDebt: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        is HomeUiState.Error -> {
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
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.retry() }) {
                    Text(stringResource(R.string.btn_retry))
                }
            }
        }

        is HomeUiState.Success -> {
            HomeContent(
                state = state,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToDebt = onNavigateToDebt,
                onAddIncome = onAddIncome,
                onAddExpense = onAddExpense,
                onDaySelected = { dayOfWeek ->
                    viewModel.selectDay(dayOfWeek)
                },
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                },
                onUpdateBudget = { newLimit ->
                    viewModel.updateBudgetLimit(newLimit)
                }
            )

            if (state.selectedDayTransactions != null || state.selectedCategoryTransactions != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.clearSelectedDay() },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                        )
                    }
                ) {
                    val label = state.selectedDayLabel ?: state.selectedCategoryLabel ?: ""
                    val txs = state.selectedDayTransactions ?: state.selectedCategoryTransactions
                    ?: emptyList()
                    DayDetailsContent(
                        dayLabel = label,
                        transactions = txs,
                        currency = state.selectedCurrency
                    )
                }
            }
        }
    }
}

@Composable
private fun DayDetailsContent(
    dayLabel: String,
    transactions: List<Transaction>,
    currency: Currency
) {
    val timeFormatter = remember { SimpleDateFormat("hh:mm a, MMM dd, yyyy", Locale.getDefault()) }
    val translatedLabel = LocalizationUtils.translateDayName(dayLabel)
        .let { LocalizationUtils.translateCategoryName(it) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.msg_transactions_for, translatedLabel),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (transactions.isEmpty()) {
            EmptyState(
                emoji = "📅",
                title = stringResource(R.string.msg_no_tx_title),
                subtitle = stringResource(R.string.msg_no_transactions_day)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                transactions.forEach { transaction ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Emoji
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = transaction.category.emoji)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = LocalizationUtils.getCategoryName(transaction.category),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = timeFormatter.format(Date(transaction.date)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (transaction.notes.isNotEmpty()) {
                                    Text(
                                        text = transaction.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = if (transaction.type == TransactionType.INCOME) {
                                    "+${CurrencyFormatter.formatAmount(transaction.amount, currency)}"
                                } else {
                                    "-${CurrencyFormatter.formatAmount(transaction.amount, currency)}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (transaction.type == TransactionType.INCOME) Color(
                                    0xFF4ADE80
                                ) else Color(0xFFFB7185)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onNavigateToTransactions: () -> Unit,
    onNavigateToDebt: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onDaySelected: (Int) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onUpdateBudget: (Double) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    var showBudgetDialog by remember { mutableStateOf(false) }

    if (showBudgetDialog) {
        EditBudgetDialog(
            currentLimit = state.budgetLimit,
            currency = state.selectedCurrency,
            onDismiss = { showBudgetDialog = false },
            onConfirm = { newLimit ->
                showBudgetDialog = false
                onUpdateBudget(newLimit)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {

        // ──── Balance Card ────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colorScheme.primary,
                            colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.label_total_balance),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Crossfade(targetState = state.selectedCurrency, label = "balance_anim") { currency ->
                    Text(
                        text = CurrencyFormatter.formatAmount(state.balance.currentBalance, currency),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontSize = 36.sp
                        ),
                        color = colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Income & Expense pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Monthly Income
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.onPrimary.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFF4ADE80), // Keep positive green for visibility
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.label_monthly_income),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Crossfade(targetState = state.selectedCurrency, label = "income_anim") { currency ->
                                    Text(
                                        text = CurrencyFormatter.formatAmount(state.balance.totalIncome, currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    // Monthly Expenses
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colorScheme.onPrimary.copy(alpha = 0.15f))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFFFB7185), // Keep negative red for visibility
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.label_monthly_expenses),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onPrimary.copy(alpha = 0.7f),
                                    lineHeight = 14.sp,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Crossfade(targetState = state.selectedCurrency, label = "expense_anim") { currency ->
                                    Text(
                                        text = CurrencyFormatter.formatAmount(state.balance.totalExpense, currency),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ──── Weekly Trend Card ────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_weekly_trend),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                WeeklyTrendChart(
                    entries = state.weeklyTrend,
                    budgetLimit = state.budgetLimit,
                    currency = state.selectedCurrency,
                    onBarClick = { entry ->
                        onDaySelected(entry.dayOfWeek)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ──── Savings Goal Card ────
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier.padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.label_savings_goal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.label_budget_tracker),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showBudgetDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.cd_edit_budget),
                            tint = colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (state.budgetLimit == 0.0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.msg_no_budget_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.msg_no_budget_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showBudgetDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.btn_set_monthly_budget))
                        }
                    }
                } else {
                    BudgetRing(
                        spent = state.totalExpense,
                        limit = state.budgetLimit,
                        currency = state.selectedCurrency,
                        targetAmount = state.goalTargetAmount,
                        size = 140.dp,
                        strokeWidth = 12.dp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ──── Category Breakdown ────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.label_category_breakdown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
            TextButton(onClick = onNavigateToTransactions) {
                Text(
                    text = stringResource(R.string.label_view_all),
                    style = MaterialTheme.typography.labelSmall,
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.categoryExpenses.isEmpty()) {
            EmptyState(
                emoji = "💸",
                title = stringResource(R.string.msg_no_tx_title),
                subtitle = stringResource(R.string.msg_no_tx_subtitle)
            )
        } else {
            // Premium Segmented Bar Summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    state.categoryExpenses.forEach { stats ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(stats.percentage.coerceAtLeast(0.01f))
                                .background(getCategoryColor(stats.category))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            state.categoryExpenses.forEach { stats ->
                CategoryBreakdownItem(
                    stats = stats,
                    currency = state.selectedCurrency,
                    onClick = { onCategorySelected(stats.category) }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
        }
    }

    Spacer(modifier = Modifier.height(100.dp)) // Bottom nav padding
}


@Composable
private fun CategoryBreakdownItem(
    stats: CategoryStats,
    currency: Currency,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val categoryColor = getCategoryColor(stats.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji Badge with custom background
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stats.category.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LocalizationUtils.getCategoryName(stats.category),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.formatAmount(stats.amount, currency),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.label_num_transactions, stats.transactionCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(stats.percentage * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stats.percentage)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        categoryColor.copy(alpha = 0.7f),
                                        categoryColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

private fun getCategoryColor(category: Category): Color {
    return when (category) {
        Category.FOOD -> Color(0xFFFF9F1C)
        Category.TRANSPORT -> Color(0xFF2EC4B6)
        Category.SHOPPING -> Color(0xFFE71D36)
        Category.ENTERTAINMENT -> Color(0xFF9B5DE5)
        Category.BILLS -> Color(0xFF00B4D8)
        Category.HEALTH -> Color(0xFFF15BB5)
        Category.EDUCATION -> Color(0xFFFEE440)
        Category.OTHER -> Color(0xFFADB5BD)
        else -> Color(0xFF6C757D)
    }
}

@Composable
fun EditBudgetDialog(
    currentLimit: Double,
    currency: Currency,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetInput by remember { mutableStateOf(currentLimit.toInt().toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val errorInvalidAmount = stringResource(R.string.error_invalid_amount)
    val errorAmountTooLarge = stringResource(R.string.error_amount_too_large)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.btn_edit_monthly_budget))
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.msg_edit_budget_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { newValue: String -> 
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        if (filtered.length <= 13) {
                            budgetInput = filtered
                            errorText = null
                        }
                    },
                    label = { Text(stringResource(R.string.label_amount, currency.symbol)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorText != null,
                    supportingText = errorText?.let { { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newLimit = budgetInput.toDoubleOrNull()
                    when {
                        newLimit == null || newLimit <= 0 -> {
                            errorText = errorInvalidAmount
                        }
                        newLimit > 1000000000.0 -> {
                            errorText = errorAmountTooLarge
                        }
                        else -> {
                            onConfirm(newLimit)
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer
    )
}
