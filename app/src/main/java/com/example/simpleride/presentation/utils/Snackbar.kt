package com.example.simpleride.presentation.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simpleride.presentation.ui.design_system.Hmi
import com.example.simpleride.presentation.ui.design_system.MonoText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class SnackBarData(
    val message: String,
    val action: SnackBarAction? = null,
    val type: SnackBarType = SnackBarType.INFO,
    val duration: Long = 4000L
)

data class SnackBarAction(val label: String, val onAction: suspend () -> Unit)

enum class SnackBarType {
    SUCCESS, ERROR, WARNING, INFO
}

data class SnackBarState(
    val isSnackBarVisible: Boolean = false,
    val snackBarHeight: Dp = 0.dp
)

val LocalSnackBarState = compositionLocalOf { SnackBarState() }

object SnackBarManager {

    private val eventChannel = Channel<SnackBarData>()
    val snackBarEvents = eventChannel.receiveAsFlow()

    suspend fun sendSnackBarEvent(message: String, action: SnackBarAction? = null) {
        eventChannel.send(SnackBarData(message, action, SnackBarType.INFO, 4000L))
    }

    suspend fun sendSnackBarEvent(data: SnackBarData) {
        eventChannel.send(data)
    }
}
@Composable
fun SnackBar(
    data: SnackBarData?,
    modifier: Modifier = Modifier,
    onSizeChange: (IntSize) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    AnimatedVisibility(
        visible = data != null && !isDragging,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 400,
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
            )
        ) + expandVertically(expandFrom = Alignment.Top),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(
                durationMillis = 300,
                easing = CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
            )
        ) + shrinkVertically(shrinkTowards = Alignment.Top),
    ) {
        data?.let { snackBarData ->
            LaunchedEffect(snackBarData) {
                if (snackBarData.action == null) {
                    delay(snackBarData.duration.milliseconds)
                    onDismiss()
                }
            }

            val accent = hmiAccentFor(snackBarData.type.name)
            val glyph = hmiGlyphFor(snackBarData.type.name)

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .clip(shape)
                    .background(accent.copy(alpha = 0.12f).compositeOver(Hmi.Panel))
                    .border(1.dp, accent.copy(alpha = 0.7f), shape)
                    .onSizeChanged(onSizeChange)
                    .pointerInput(snackBarData) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onHorizontalDrag = { _, dragAmount ->
                                if (dragAmount > 50 || dragAmount < -50) {
                                    isDragging = false
                                    onDismiss()
                                }
                            }
                        )
                    }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Hmi.PanelHi)
                        .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = glyph, color = accent, fontSize = 16.sp)
                }

                MonoText(
                    text = snackBarData.message,
                    color = Hmi.Text,
                    size = 13.sp,
                    weight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (snackBarData.action != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                coroutineScope.launch {
                                    snackBarData.action.onAction()
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        MonoText(
                            text = snackBarData.action.label.uppercase(),
                            color = accent,
                            size = 11.sp,
                            weight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✕", color = Hmi.Muted, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}


@Composable
private fun hmiAccentFor(typeName: String): Color = when (typeName.uppercase()) {
    "SUCCESS" -> Hmi.Ok
    "ERROR", "FAILURE", "DANGER" -> Hmi.Danger
    "WARNING", "WARN" -> Hmi.Warn
    else -> Hmi.Accent
}

private fun hmiGlyphFor(typeName: String): String = when (typeName.uppercase()) {
    "SUCCESS" -> "✓"
    "ERROR", "FAILURE", "DANGER" -> "⛔"
    "WARNING", "WARN" -> "▲"
    else -> "ℹ"
}

@Preview(
    name = "Success Snackbar",
    showBackground = true,
    backgroundColor = 0xFF101820
)
@Composable
private fun SuccessSnackBarPreview() {
    SnackBar(
        data = SnackBarData(
            message = "Vehicle connected successfully",
            type = SnackBarType.SUCCESS
        ),
        onDismiss = {},
        onSizeChange = {}
    )
}

@Preview(
    name = "Error Snackbar",
    showBackground = true,
    backgroundColor = 0xFF101820
)
@Composable
private fun ErrorSnackBarPreview() {
    SnackBar(
        data = SnackBarData(
            message = "Vehicle connection failed",
            type = SnackBarType.ERROR
        ),
        onDismiss = {},
        onSizeChange = {}
    )
}

@Preview(
    name = "Warning Snackbar",
    showBackground = true,
    backgroundColor = 0xFF101820
)
@Composable
private fun WarningSnackBarPreview() {
    SnackBar(
        data = SnackBarData(
            message = "Vehicle signal is unstable",
            type = SnackBarType.WARNING
        ),
        onDismiss = {},
        onSizeChange = {}
    )
}

@Preview(
    name = "Info Snackbar",
    showBackground = true,
    backgroundColor = 0xFF101820
)
@Composable
private fun InfoSnackBarPreview() {
    SnackBar(
        data = SnackBarData(
            message = "Vehicle dashboard is ready",
            type = SnackBarType.INFO
        ),
        onDismiss = {},
        onSizeChange = {}
    )
}