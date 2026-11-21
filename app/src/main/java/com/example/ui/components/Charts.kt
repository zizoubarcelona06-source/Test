package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.util.Formatters
import com.example.ui.theme.AmberBright
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyElevated
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SteelTertiary
import com.example.viewmodel.CategoryAmount
import com.example.viewmodel.ProjectBudgetStat

@Composable
fun DonutBreakdownChart(
    items: List<CategoryAmount>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val palette = listOf(
        NavyPrimary,
        AmberSecondary,
        SteelTertiary,
        AmberBright,
        NavyElevated
    )
    val total = items.sumOf { it.amount }.coerceAtLeast(1.0)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(134.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(126.dp)) {
                val strokeWidth = 22.dp.toPx()
                var startAngle = -90f
                if (items.isEmpty()) {
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                } else {
                    items.forEachIndexed { idx, cat ->
                        val sweep = ((cat.amount / total) * 360.0).toFloat()
                        drawArc(
                            color = palette[idx % palette.size],
                            startAngle = startAngle,
                            sweepAngle = (sweep - 3f).coerceAtLeast(2f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += sweep
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = strings.total,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatMoney(items.sumOf { it.amount }, currency),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { idx, cat ->
                val pct = ((cat.amount / total) * 100).toInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(palette[idx % palette.size])
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.translateTerm(cat.label),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectBudgetBarsChart(
    stats: List<ProjectBudgetStat>,
    currency: String,
    modifier: Modifier = Modifier
) {
    val budgetColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val spentColor = AmberSecondary
    val overBudgetColor = StatusDanger

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { item ->
            val maxVal = item.budget.coerceAtLeast(1.0)
            val spentRatio = (item.spent / maxVal).toFloat().coerceIn(0f, 1f)
            val isOver = item.spent > item.budget

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${Formatters.formatMoney(item.spent, currency)} / ${Formatters.formatMoney(item.budget, currency)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOver) overBudgetColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    drawRoundRect(
                        color = budgetColor,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    drawRoundRect(
                        color = if (isOver) overBudgetColor else spentColor,
                        topLeft = Offset.Zero,
                        size = Size(size.width * spentRatio, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    progressPercent: Int,
    color: Color = AmberSecondary,
    modifier: Modifier = Modifier
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(48.dp)) {
            val stroke = 5.dp.toPx()
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = (progressPercent.coerceIn(0, 100) / 100f) * 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Text(
            text = "$progressPercent%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
