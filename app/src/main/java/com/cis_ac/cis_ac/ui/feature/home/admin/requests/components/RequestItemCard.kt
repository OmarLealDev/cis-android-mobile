package com.cis_ac.cis_ac.ui.feature.home.admin.requests.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.core.model.Professional
import com.cis_ac.cis_ac.ui.theme.BackgroundDark
import com.cis_ac.cis_ac.ui.theme.Quaternary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RequestItemCard(
    pro: Professional,
    onOpenDetail: (Professional) -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = { onOpenDetail(pro) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(3.dp),
        modifier = modifier
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Person, contentDescription = null) }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    pro.fullName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                pro.createdAt?.let { millis ->
                    Text(
                        "Solicitado: ${formatDate(millis)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AssistChip(
                onClick = { onOpenDetail(pro) },
                label = { Text("Pendiente") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Quaternary,
                    labelColor = BackgroundDark
                )
            )
        }
    }
}

private fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}
