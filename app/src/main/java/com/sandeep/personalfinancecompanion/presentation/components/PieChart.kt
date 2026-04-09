package com.sandeep.personalfinancecompanion.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.sandeep.personalfinancecompanion.domain.model.Currency
import com.sandeep.personalfinancecompanion.util.CurrencyFormatter.formatAmount

data class PieChartEntry(
    val label: String,
    val value: Double,
    val color: Color
)

@Composable
fun PieChart(
    entries: List<PieChartEntry>,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) return

    val total = entries.sumOf { it.value }
    if (total == 0.0) return

    var animationTarget by remember { mutableFloatStateOf(0f) }
    val animatedSweep by animateFloatAsState(
        targetValue = animationTarget,
        animationSpec = tween(durationMillis = 1000),
        label = "pie_chart_animation"
    )

    LaunchedEffect(entries) {
        animationTarget = 1f
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // The Chart
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                var startAngle = -90f
                entries.forEach { entry ->
                    val sweepAngle = ((entry.value / total) * 360f * animatedSweep).toFloat()
                    drawArc(
                        color = entry.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }

            // Center text
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatAmount(total, currency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Legend
        entries.forEach { entry ->
            val percentage = ((entry.value / total) * 100).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = entry.color
                ) {}

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatAmount(entry.value, currency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
