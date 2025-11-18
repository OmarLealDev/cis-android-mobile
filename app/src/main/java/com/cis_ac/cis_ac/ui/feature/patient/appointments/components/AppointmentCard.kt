package com.cis_ac.cis_ac.ui.feature.patient.appointments.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.ui.feature.patient.appointments.AppointmentItem
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.Color

@Composable
fun AppointmentCard(
    item: AppointmentItem,
    onClick: () -> Unit,
    isPast: Boolean = false
) {
    val cs = MaterialTheme.colorScheme

    val dateContainer = if (isPast) cs.surfaceVariant else cs.primaryContainer
    val dateContent   = if (isPast) cs.onSurfaceVariant else cs.onPrimaryContainer
    val timeContainer = if (isPast) cs.surfaceVariant else cs.primaryContainer
    val timeContent   = if (isPast) cs.onSurfaceVariant else cs.onPrimaryContainer


    val confirmedBg = Color(0xFF2E7D32)
    val pendingBg   = Color(0xFFFFA000)

    val statusBg = if (item.confirmed) confirmedBg else pendingBg
    val statusFg = if (item.confirmed) Color.White else contentColorFor(backgroundColor = pendingBg)

    val statusText = if (item.confirmed) "Confirmada" else "Pendiente"

    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cs.surface,
            contentColor   = cs.onSurface
        )
    ) {
        Box(Modifier.fillMaxWidth()) {

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(44.dp).clip(CircleShape),
                    shape = CircleShape,
                    color = cs.surfaceVariant
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = cs.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        item.professionalName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        item.disciplineLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    val dateText = item.dateTime.format(DateTimeFormatter.ofPattern("dd 'de' MMMM"))
                    val timeText = item.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AssistChip(
                            onClick = {},
                            label = { Text(dateText) },
                            leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) },
                            enabled = true,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = dateContainer,
                                labelColor = dateContent,
                                leadingIconContentColor = dateContent
                            )
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(timeText) },
                            leadingIcon = { Icon(Icons.Filled.AccessTime, null) },
                            enabled = true,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = timeContainer,
                                labelColor = timeContent,
                                leadingIconContentColor = timeContent
                            )
                        )
                    }
                }
            }

            Surface(
                color = statusBg,
                contentColor = statusFg,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
            ) {
                Text(
                    statusText,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
