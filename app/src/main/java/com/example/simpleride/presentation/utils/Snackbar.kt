package com.example.simpleride.presentation.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

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

    AnimatedVisibility(
        visible = data != null && !isDragging,
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
            // Auto-dismiss after duration (only if no action)
            LaunchedEffect(snackBarData) {
                if (snackBarData.action == null) {
                    delay(snackBarData.duration)
                    onDismiss()
                }
            }

            val colors = getSnackBarColors(snackBarData.type)

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                colors.background,
                                colors.background.copy(alpha = 0.85f)
                            )
                        )
                    )
                    .onSizeChanged(onSizeChange)
                    .pointerInput(snackBarData) {
                        detectHorizontalDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onHorizontalDrag = { _, dragAmount ->
                                // 50dp threshold - intentional gesture, not accidental touch
                                if (dragAmount > 50 || dragAmount < -50) {
                                    isDragging = false
                                    onDismiss()
                                }
                            }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon with semantic color
                Icon(
                    imageVector = colors.icon,
                    contentDescription = snackBarData.type.name,
                    tint = colors.iconColor,
                    modifier = Modifier.size(22.dp)
                )

                // Message text (flexible width)
                Text(
                    text = snackBarData.message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.content,
                    modifier = Modifier.weight(1f)
                )

                // Action button or close icon
                if (snackBarData.action != null) {
                    Text(
                        text = snackBarData.action.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = colors.iconColor,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable {
                                coroutineScope.launch {
                                    snackBarData.action.onAction()
                                    onDismiss()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = colors.content.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(20.dp)
                            .clip(MaterialTheme.shapes.small)
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

private data class SnackBarColorScheme(
    val background: Color,
    val content: Color,
    val iconColor: Color,
    val icon: ImageVector
)


private fun getSnackBarColors(type: SnackBarType): SnackBarColorScheme {
    return when (type) {
        SnackBarType.SUCCESS -> SnackBarColorScheme(
            background = Color(0xFF10B981).copy(alpha = 0.95f),
            content = Color.White,
            iconColor = Color(0xFFECFDF5),
            icon = Icons.Default.CheckCircle
        )
        SnackBarType.ERROR -> SnackBarColorScheme(
            background = Color(0xFFEF4444).copy(alpha = 0.95f),
            content = Color.White,
            iconColor = Color(0xFFFEE2E2),
            icon = Icons.Default.Error
        )
        SnackBarType.WARNING -> SnackBarColorScheme(
            background = Color(0xFFF59E0B).copy(alpha = 0.95f),
            content = Color.White,
            iconColor = Color(0xFFFEF3C7),
            icon = Icons.Default.Warning
        )
        SnackBarType.INFO -> SnackBarColorScheme(
            background = Color(0xFF3B82F6).copy(alpha = 0.95f),
            content = Color.White,
            iconColor = Color(0xFFDEF2FF),
            icon = Icons.Default.Info
        )
    }
}