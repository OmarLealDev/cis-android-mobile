package com.cis_ac.cis_ac.ui.feature.professional.appointments.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.ui.feature.professional.appointments.ProAppointmentItem
import java.time.format.DateTimeFormatter

@Composable
fun ProAppointmentCard(
    item: ProAppointmentItem,
    onOpen: (String) -> Unit,
    onApprove: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme

    val statusText: String
    val statusBg: Color
    val statusFg: Color
    if (item.confirmed) {
        statusText = "Confirmada"
        statusBg = Color(0xFF2E7D32)
        statusFg = Color.White
    } else {
        statusText = "Pendiente"
        statusBg = Color(0xFFFFD54F)
        statusFg = Color(0xFF3E2723)
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = cs.surface,
            contentColor   = cs.onSurface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.fillMaxWidth()) {

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                            "Consulta con " + item.patientName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Spacer(Modifier.height(10.dp))

                        val dateText = item.dateTime.format(DateTimeFormatter.ofPattern("dd 'de' MMMM"))
                        val timeText = item.dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text(dateText) },
                                leadingIcon = { Icon(Icons.Filled.CalendarMonth, null) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(timeText) },
                                leadingIcon = { Icon(Icons.Filled.AccessTime, null) }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (item.confirmed) {
                        Button(
                            onClick = { onOpen(item.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver cita")
                        }
                    } else {
                        Button(
                            onClick = { onApprove(item.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Revisar")
                        }
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
