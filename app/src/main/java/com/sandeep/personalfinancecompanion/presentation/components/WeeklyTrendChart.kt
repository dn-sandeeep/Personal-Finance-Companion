package com.sandeep.personalfinancecompanion.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sandeep.personalfinancecompanion.ui.theme.BudgetCaution
import com.sandeep.personalfinancecompanion.ui.theme.BudgetDanger
import com.sandeep.personalfinancecompanion.ui.theme.BudgetSafe


data class BarEntry(
    val label: String,
    val value: Float,
    val isHighlighted: Boolean = false,
    val dayOfWeek: Int = -1
)
@Composable
fun WeeklyTrendChart(
    entries: List<BarEntry>,
    modifier: Modifier = Modifier,
    onBarClick: (BarEntry) -> Unit = {},
    budgetLimit: Double = 50000.0,
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency = com.sandeep.personalfinancecompanion.domain.model.Currency.INR,
    barColor: Color = Color(0xFF0D6B58),
    barColorLight: Color = Color(0xFFB2DFDB),
    highlightColor: Color = Color(0xFF0D6B58)
) {
    if (entries.isEmpty()) return

    val maxEntryValue = entries.maxOfOrNull { it.value } ?: 0f
    
    // Dynamic Scale: Default max is ₹1000 converted to current currency
    val minDefaultMax = com.sandeep.personalfinancecompanion.domain.model.Currency.convert(
        1000.0, 
        com.sandeep.personalfinancecompanion.domain.model.Currency.INR, 
        currency
    ).toFloat()

    // Dynamic Thresholds Based on Daily Budget (Monthly Budget / 30)
    val dailyBudget = (budgetLimit / 30.0).toFloat()
    val safeThreshold = dailyBudget * 0.5f  // 50% of daily budget
    val cautionThreshold = dailyBudget      // 100% of daily budget

    // Calculate a "nice" max value for the chart scale
    val maxValue = when {
        maxEntryValue <= 0f -> minDefaultMax
        maxEntryValue <= minDefaultMax -> minDefaultMax
        maxEntryValue <= minDefaultMax * 5 -> minDefaultMax * 5
        maxEntryValue <= minDefaultMax * 10 -> minDefaultMax * 10
        else -> {
            val magnitude = Math.pow(10.0, Math.floor(Math.log10(maxEntryValue.toDouble()))).toFloat()
            (Math.ceil((maxEntryValue / magnitude).toDouble()) * magnitude).toFloat()
        }
    }

    val yAxisLabels = listOf(
        maxValue,
        maxValue * 0.75f,
        maxValue * 0.5f,
        maxValue * 0.25f
    ).map { it.toInt() }

    var animTarget by remember { mutableFloatStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 800),
        label = "bar_animation"
    )

    LaunchedEffect(entries) {
        animTarget = 1f
    }

    val labelWidth = 55.dp // Increased width for symbol

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            // Y-Axis Labels
            Column(
                modifier = Modifier
                    .width(labelWidth)
                    .height(130.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                yAxisLabels.forEach { label ->
                    Text(
                        text = formatLabel(label, currency),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text(
                    text = "${currency.symbol}0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Chart Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .pointerInput(entries) {
                            detectTapGestures { offset ->
                                val barCount = entries.size
                                val totalWidth = size.width
                                val barWidth = totalWidth / (barCount * 1.8f)
                                val gap = (totalWidth - (barWidth * barCount)) / (barCount + 1)

                                entries.forEachIndexed { index, entry ->
                                     val x = gap + index * (barWidth + gap)
                                     if (offset.x >= x && offset.x <= x + barWidth) {
                                         onBarClick(entry)
                                     }
                                }
                            }
                        }
                ) {
                    val barCount = entries.size
                    val totalWidth = size.width
                    val barWidth = totalWidth / (barCount * 1.2f)
                    val gap = (totalWidth - (barWidth * barCount)) / (barCount + 1)

                    // Draw Y-axis line
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Draw horizontal grid lines
                    yAxisLabels.forEach { label ->
                        val y = size.height - (label.toFloat() / maxValue) * size.height
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.2f),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Draw bars
                    entries.forEachIndexed { index, entry ->
                        val barHeight = (entry.value / maxValue) * size.height * animProgress
                        val x = gap + index * (barWidth + gap)

                        // Dynamic Color Logic based on budget-derived thresholds
                        val dynamicBarColor = when {
                            entry.value == 0f -> Color.LightGray.copy(alpha = 0.3f)
                            entry.value < safeThreshold -> BudgetSafe
                            entry.value < cautionThreshold -> BudgetCaution
                            else -> BudgetDanger
                        }

                        // Use dynamicBarColor as the base. If highlighted (current day), use full opacity.
                        // Otherwise, use slightly reduced opacity to make the current day stand out.
                        val color = if (entry.isHighlighted) {
                            dynamicBarColor
                        } else {
                            dynamicBarColor.copy(alpha = 0.6f)
                        }

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight.coerceAtLeast(2.dp.toPx())), // Show tiny bar even for small values
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-Axis Labels (Days)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = labelWidth),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            entries.forEach { entry ->
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (entry.isHighlighted)
                        highlightColor
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }
    }
}

private fun formatLabel(
    value: Int, 
    currency: com.sandeep.personalfinancecompanion.domain.model.Currency
): String {
    val symbol = currency.symbol
    return when {
        value >= 1000000 -> "$symbol${value / 1000000}M"
        value >= 1000 -> "$symbol${value / 1000}K"
        else -> "$symbol$value"
    }
}
