package com.cis_ac.cis_ac.ui.feature.shared

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomBarItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true
)