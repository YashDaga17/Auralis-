package com.brandforge.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brandforge.app.core.model.RiskLevel
import com.brandforge.app.ui.theme.ForgeColor

@Composable
fun GridBackdrop(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .background(ForgeColor.Black),
    ) {
        val step = 18.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawLine(
                color = ForgeColor.Grid.copy(alpha = 0.42f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f,
            )
            x += step
        }
        var y = 0f
        while (y < size.height) {
            drawLine(
                color = ForgeColor.Grid.copy(alpha = 0.42f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
            )
            y += step
        }
    }
}

@Composable
fun ForgePanel(
    modifier: Modifier = Modifier,
    borderColor: Color = ForgeColor.White.copy(alpha = 0.72f),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .background(ForgeColor.Panel.copy(alpha = 0.96f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        content = content,
    )
}

@Composable
fun SectionHeader(
    title: String,
    trailing: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "[$title]",
            style = MaterialTheme.typography.titleMedium,
            color = ForgeColor.Yellow,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = ForgeColor.Green,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun StatusPill(
    label: String,
    riskLevel: RiskLevel = RiskLevel.Low,
    modifier: Modifier = Modifier,
) {
    val color = when (riskLevel) {
        RiskLevel.Low -> ForgeColor.Green
        RiskLevel.Medium -> ForgeColor.Yellow
        RiskLevel.High -> ForgeColor.Red
    }
    Box(
        modifier = modifier
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
fun PixelButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
) {
    val color = if (active) ForgeColor.Yellow else ForgeColor.White
    Box(
        modifier = modifier
            .height(42.dp)
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .background(if (active) ForgeColor.Yellow.copy(alpha = 0.14f) else ForgeColor.PanelRaised)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SignalBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = ForgeColor.Green,
    height: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(1.dp, ForgeColor.White.copy(alpha = 0.42f), RoundedCornerShape(2.dp))
            .background(ForgeColor.Black.copy(alpha = 0.8f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(color),
        )
    }
}

@Composable
fun PixelTwinAvatar(
    modifier: Modifier = Modifier,
) {
    val pixel = 6.dp
    Column(
        modifier = modifier
            .border(1.dp, ForgeColor.Yellow)
            .background(ForgeColor.Black)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        repeat(7) { row ->
            Row {
                repeat(7) { col ->
                    val filled = when (row) {
                        0 -> col in 2..4
                        1 -> col in 1..5
                        2 -> col in 0..6
                        3 -> col == 1 || col == 5 || col == 3
                        4 -> col in 1..5
                        5 -> col == 2 || col == 4
                        else -> col in 1..5
                    }
                    Box(
                        modifier = Modifier
                            .size(pixel)
                            .background(if (filled) ForgeColor.Yellow else Color.Transparent),
                    )
                }
            }
        }
    }
}

@Composable
fun TerminalDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp),
    ) {
        drawLine(
            color = ForgeColor.White.copy(alpha = 0.65f),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 1f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
        )
    }
}

@Composable
fun KeyValueLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = ForgeColor.White,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = ForgeColor.Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.82f),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.18f),
        )
    }
}
