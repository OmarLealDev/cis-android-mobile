package com.cis_ac.cis_ac.ui.feature.shared

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun UserBottomBar(
    items: List<BottomBarItem>,
    selectedId: String,
    onSelect: (BottomBarItem) -> Unit,
    alwaysShowLabel: Boolean = true,
    label: @Composable (String) -> Unit = { text ->
        Text(text, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, softWrap = false)
    }
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.id == selectedId,
                onClick = { if (item.enabled) onSelect(item) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { label(item.label) },
                enabled = item.enabled,
                alwaysShowLabel = alwaysShowLabel
            )
        }
    }
}
