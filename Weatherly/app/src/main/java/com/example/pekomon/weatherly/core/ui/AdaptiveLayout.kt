package com.example.pekomon.weatherly.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private val mediumWidthBreakpoint = 720.dp
private val expandedWidthBreakpoint = 960.dp

@Composable
fun isMediumWidth(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp >= mediumWidthBreakpoint
}

@Composable
fun isExpandedWidth(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp.dp >= expandedWidthBreakpoint
}
