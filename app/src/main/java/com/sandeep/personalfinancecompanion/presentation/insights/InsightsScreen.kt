package com.sandeep.personalfinancecompanion.presentation.insights

import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sandeep.personalfinancecompanion.presentation.components.EmptyState
import com.sandeep.personalfinancecompanion.presentation.components.PieChart
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import androidx.compose.ui.res.stringResource
import com.sandeep.personalfinancecompanion.R
import com.sandeep.personalfinancecompanion.util.LocalizationUtils

@Composable
fun InsightsScreen(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val colorScheme = MaterialTheme.colorScheme

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        }

        state.error != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "🏥", style = MaterialTheme.typography.displayLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = state.error ?: stringResource(R.string.error_analytics_load),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }

        state.categoryBreakdown.isEmpty() -> {
            EmptyState(
                emoji = "📊",
                title = stringResource(R.string.title_no_insights),
                subtitle = stringResource(R.string.subtitle_no_insights)
            )
        }

        else -> {
            InsightsContent(innerPadding = innerPadding, state = state, currency = currency)
        }
    }
}

@Composable
private fun InsightsContent(
    innerPadding: androidx.compose.foundation.layout.PaddingValues,
    state: InsightsState,
    currency: Currency
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 10.dp)
    ) {
        Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding() + 10.dp))

        // Quick Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val topCatName = state.topCategory?.let { 
                "${it.emoji} ${LocalizationUtils.getCategoryName(it)}"
            } ?: "N/A"
            
            StatCard(
                title = stringResource(R.string.label_top_category),
                value = topCatName,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.label_transactions),
                value = state.totalTransactions.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Income vs Expense Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = IncomeGreen.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(R.string.label_total_income_insights),
                        style = MaterialTheme.typography.labelMedium,
                        color = IncomeGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Crossfade(targetState = currency, label = "income_anim") { curr ->
                        Text(
                            text = CurrencyFormatter.formatAmount(state.balanceSummary.totalIncome, curr),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = ExpenseRed.copy(alpha = 0.1f)
                )
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = stringResource(R.string.label_total_expense_insights),
                        style = MaterialTheme.typography.labelMedium,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Crossfade(targetState = currency, label = "expense_anim") { curr ->
                        Text(
                            text = CurrencyFormatter.formatAmount(state.balanceSummary.totalExpense, curr),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pie Chart Section
        Text(
            text = stringResource(R.string.label_spending_by_category),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            PieChart(
                entries = state.categoryBreakdown,
                modifier = Modifier.padding(25.dp),
                currency = currency
            )
        }

        Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding() + 100.dp))
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface
            )
        }
    }
}
