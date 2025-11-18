package com.cis_ac.cis_ac.ui.feature.patient.professionals.components

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
import com.cis_ac.cis_ac.core.model.label
import com.cis_ac.cis_ac.ui.feature.patient.professionals.PatientProfessionalItem
import com.cis_ac.cis_ac.ui.theme.Quaternary
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color

@Composable
fun ProfessionalCardForPatient(
    item: PatientProfessionalItem,
    onProfile: (String) -> Unit,
    onSchedule: (String) -> Unit,
    onChat: (PatientProfessionalItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val ratingText = item.rating?.let { String.format("%.1f", it) } ?: "0"

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(2.dp),
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(46.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Filled.Person, contentDescription = null) }

                    Spacer(Modifier.height(8.dp))

                    Surface(color = Quaternary, shape = RoundedCornerShape(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Black)
                            Spacer(Modifier.width(4.dp))
                            Text(ratingText, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        item.fullName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        item.discipline.label(),   // ← traducción desde tu extensión
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { onProfile(item.uid) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) { Text("Ver perfil") }

                        OutlinedButton(
                            onClick = { onChat(item) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) { Text("Chatear") }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { onSchedule(item.uid) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) { Text("Agendar cita") }
        }
    }
}
