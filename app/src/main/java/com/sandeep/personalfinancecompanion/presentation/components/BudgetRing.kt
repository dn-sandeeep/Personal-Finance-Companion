package com.sandeep.personalfinancecompanion.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.ui.theme.BudgetCaution
import com.sandeep.personalfinancecompanion.ui.theme.BudgetDanger
import com.sandeep.personalfinancecompanion.ui.theme.BudgetSafe
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter.formatAmount

@Composable
fun BudgetRing(
    spent: Double,
    limit: Double,
    currency: Currency,
    targetAmount: Double,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 14.dp
) {
    val percentage = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1.2f) else 0f
    val displayPercentage = (percentage * 100).coerceAtMost(100f)

    val ringColor = when {
        percentage < 0.5f -> BudgetSafe
        percentage < 0.8f -> BudgetCaution
        else -> BudgetDanger
    }

    var animationTarget by remember { mutableFloatStateOf(0f) }
    val animatedPercentage by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1200),
        label = "budget_ring_animation"
    )

    LaunchedEffect(percentage) {
        animationTarget = percentage.coerceAtMost(1f)
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val remaining = (limit - spent).coerceAtLeast(0.0)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round
            )

            // Track (background ring)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Progress ring
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedPercentage * 360f,
                useCenter = false,
                style = stroke
            )
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatAmount(remaining, currency),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ringColor
            )
            Text(
                //text = "left of ₹${String.format("%,.0f", limit)}",
                text = "left of ${formatAmount(limit, currency)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${displayPercentage.toInt()}% used",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
