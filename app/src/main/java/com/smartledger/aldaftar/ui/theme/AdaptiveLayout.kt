package com.smartledger.aldaftar.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Window-aware layout policy shared by the whole application. */
enum class MizanWindowSizeClass { Compact, Medium, Expanded }

@Composable
fun rememberMizanWindowSizeClass(): MizanWindowSizeClass {
    val configuration = LocalConfiguration.current
    return mizanWindowSizeClassForWidth(configuration.screenWidthDp)
}

fun mizanWindowSizeClassForWidth(widthDp: Int): MizanWindowSizeClass = when {
    widthDp < 600 -> MizanWindowSizeClass.Compact
    widthDp < 840 -> MizanWindowSizeClass.Medium
    else -> MizanWindowSizeClass.Expanded
}

fun MizanWindowSizeClass.isExpanded(): Boolean = this == MizanWindowSizeClass.Expanded

fun MizanWindowSizeClass.contentMaxWidth(): Dp = when (this) {
    MizanWindowSizeClass.Compact -> 600.dp
    MizanWindowSizeClass.Medium -> 960.dp
    MizanWindowSizeClass.Expanded -> 1200.dp
}
