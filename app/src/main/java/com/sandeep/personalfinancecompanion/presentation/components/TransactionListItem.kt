package com.sandeep.personalfinancecompanion.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed
import com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter

@Composable
fun TransactionListItem(
    transaction: Transaction,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val isIncome = transaction.type == TransactionType.INCOME

    // Assigning specific badge colors based on category
    val badgeColor = when (transaction.category) {
        Category.FOOD, Category.BILLS -> colorScheme.errorContainer.copy(alpha = 0.5f)
        Category.TRANSPORT -> colorScheme.secondaryContainer
        Category.SALARY, Category.INVESTMENT, Category.FREELANCE -> colorScheme.primaryContainer
        else -> colorScheme.surfaceVariant
    }

    //val amountColor = if (isIncome) colorScheme.primary else colorScheme.onSurface

    // Derive Status logic based on type/category for visual matching
    val statusText = if (isIncome) "EARNED" else "SPEND"
    val statusColor = if (isIncome) IncomeGreen else ExpenseRed

    // Title mapping (Fallback to Category if Notes are empty)
    val title =
        if (transaction.notes.isNotBlank()) transaction.notes else transaction.category.displayName
    val subtitle = transaction.category.displayName

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge (Rounded Rectangle)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.category.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Title and Category
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount and Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = CurrencyFormatter.formatAmount(transaction.amount, currency),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
