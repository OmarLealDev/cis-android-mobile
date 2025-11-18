package com.cis_ac.cis_ac.ui.feature.home.patient.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun ExploreProfessionalsCard(onClick: () -> Unit) {
    ElevatedCard(onClick = onClick, shape = RoundedCornerShape(16.dp), elevation = CardDefaults.elevatedCardElevation(2.dp)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiaryContainer))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Lista de profesionales", style = MaterialTheme.typography.titleSmall)
                Text("Encuentra al profesional adecuado para ti", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForwardIos, contentDescription = null)
        }
    }
}
