package com.jna.tictactoe.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines a set of dimension values that change based on screen size.
 */
@Immutable
data class AppDimensions(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val cardSpacing: Dp,
    val cornerRadiusLarge: Dp,
    val cornerRadiusMedium: Dp,
    val gridPadding: Dp,
    val gridGap: Dp,
    val iconSizeLarge: Dp,
    val iconSizeMedium: Dp
)

/**
 * Default dimensions for compact screens (phones).
 */
val CompactDimensions = AppDimensions(
    horizontalPadding = 24.dp,
    verticalPadding = 16.dp,
    cardSpacing = 16.dp,
    cornerRadiusLarge = 24.dp,
    cornerRadiusMedium = 16.dp,
    gridPadding = 12.dp,
    gridGap = 12.dp,
    iconSizeLarge = 48.dp,
    iconSizeMedium = 24.dp
)

/**
 * Dimensions for expanded screens (tablets/desktops).
 */
val ExpandedDimensions = AppDimensions(
    horizontalPadding = 64.dp,
    verticalPadding = 32.dp,
    cardSpacing = 32.dp,
    cornerRadiusLarge = 32.dp,
    cornerRadiusMedium = 20.dp,
    gridPadding = 24.dp,
    gridGap = 20.dp,
    iconSizeLarge = 64.dp,
    iconSizeMedium = 32.dp
)

/**
 * CompositionLocal to provide current dimensions down the tree.
 */
val LocalAppDimensions = staticCompositionLocalOf { CompactDimensions }
