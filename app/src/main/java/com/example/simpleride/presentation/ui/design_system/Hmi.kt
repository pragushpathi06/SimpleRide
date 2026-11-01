package com.example.simpleride.presentation.ui.design_system

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily


object Hmi {
    val Mono = FontFamily.Monospace

    val Bg: Color @Composable get() = MaterialTheme.colorScheme.background
    val FrameTop: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
    val FrameBottom: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerLowest
    val Panel: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val PanelHi: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val Track: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHighest
    val Border: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant

    val Accent: Color @Composable get() = MaterialTheme.colorScheme.primary
    val AccentDeep: Color @Composable get() = MaterialTheme.colorScheme.inversePrimary

    val Ok: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val Warn: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val Danger: Color @Composable get() = MaterialTheme.colorScheme.error

    val Text: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val Muted: Color @Composable get() = MaterialTheme.colorScheme.outline
}
