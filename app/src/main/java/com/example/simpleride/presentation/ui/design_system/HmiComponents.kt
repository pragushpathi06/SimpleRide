package com.example.simpleride.presentation.ui.design_system

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MonoText(
    text: String,
    color: Color,
    size: TextUnit,
    weight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start,
) {
    Text(
        text, modifier, color = color, fontSize = size, fontWeight = weight,
        fontFamily = Hmi.Mono, letterSpacing = 0.8.sp, textAlign = align
    )
}

@Composable
fun Dot(color: Color, size: Dp) {
    Box(Modifier.size(size).clip(CircleShape).background(color))
}

@Composable
fun Tag(text: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(5.dp))
            .background(color.copy(alpha = .12f))
            .border(1.dp, color.copy(alpha = .5f), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) { MonoText(text, color, 9.sp, FontWeight.Bold) }
}

@Composable
fun IconTile(glyph: String, tint: Color, container: Color = Hmi.PanelHi, border: Color = Hmi.Border) {
    Box(
        Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) { Text(glyph, color = tint, fontSize = 24.sp) }
}

@Composable
fun Panel(
    modifier: Modifier = Modifier,
    border: Color = Hmi.Border,
    container: Color = Hmi.Panel,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(container)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content
    )
}

const val GAUGE_MAX_KMH = 150f

@Composable
fun SpeedGauge(
    speedKmh: Int?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val target = if (speedKmh == null) 0f else (speedKmh / GAUGE_MAX_KMH).coerceIn(0f, 1f)
    val progress by animateFloatAsState(target, tween(600), label = "speed")

    val trackColor = Hmi.Track
    val deep = Hmi.AccentDeep
    val accent = Hmi.Accent
    val end = Hmi.Ok
    val tickColor = Hmi.Muted
    val ticks = listOf(0, 30, 60, 90, 120, 150)

    Box(modifier.aspectRatio(1f), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val pad = 26.dp.toPx()
            val arcSize = Size(size.width - pad * 2, size.height - pad * 2)
            val topLeft = Offset(pad, pad)
            val startAngle = 135f
            val totalSweep = 270f

            drawArc(trackColor, startAngle, totalSweep, false, topLeft, arcSize,
                style = Stroke(stroke, cap = StrokeCap.Round))

            val sweep = totalSweep * progress
            if (sweep > 0.5f) {
                val brush = Brush.sweepGradient(
                    0f to deep, 0.4f to accent, 0.75f to end, 1f to end,
                    center = center
                )
                rotate(startAngle, center) {
                    drawArc(brush, 0f, sweep, false, topLeft, arcSize,
                        alpha = .25f, style = Stroke(stroke * 2.2f, cap = StrokeCap.Round))
                    drawArc(brush, 0f, sweep, false, topLeft, arcSize,
                        style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }

            val radius = arcSize.width / 2f + stroke * 2.4f
            val paint = Paint().apply {
                isAntiAlias = true
                color = tickColor.toArgb()
                textSize = 11.sp.toPx()
                typeface = Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            ticks.forEach { t ->
                val a = Math.toRadians((startAngle + totalSweep * t / GAUGE_MAX_KMH).toDouble())
                val x = center.x + (radius * cos(a)).toFloat()
                val y = center.y + (radius * sin(a)).toFloat() + paint.textSize / 3f
                drawContext.canvas.nativeCanvas.drawText(t.toString(), x, y, paint)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}
