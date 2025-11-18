package com.cis_ac.cis_ac.ui.feature.patient.professionalprofile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cis_ac.cis_ac.core.model.label
import java.util.Locale

@Composable
fun ProfessionalProfileScreen(
    data: ProfessionalProfileUiState.Content,
    contentPadding: PaddingValues = PaddingValues(),
    onChat: (() -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier.size(88.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Person, contentDescription = null)
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                data.fullName,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                data.discipline.label(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            RatingBadge(rating = data.rating ?: 0.0)
        }

        if (onChat != null) {
            Button(
                onClick = onChat,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Enviar mensaje")
            }
        }

        SectionTitle("Información profesional")
        InfoCard {
            InfoRow("Cédula", data.licenseNumber ?: "—")
            InfoRow("Formación", data.expertiz ?: "—")
            if (!data.approach.isNullOrBlank()) {
                InfoRow("Enfoque", data.approach!!)
            }
        }

        SectionTitle("Temas de especialización")
        BulletsCard(items = data.topics)

        SectionTitle("Tipos de sesión")
        BulletsCard(items = data.sessionTypes)

        SectionTitle("Modalidad")
        BulletsCard(items = data.modalities)

        SectionTitle("Semblanza")
        InfoCard {
            Text(
                text = data.summary ?: "—",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        SectionTitle("Testimonios")
        if (data.testimonials.isEmpty()) {
            Text("Aún no hay testimonios", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                data.testimonials.forEach { t -> TestimonialCard(t) }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}


@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BulletsCard(items: List<String>) {
    InfoCard {
        if (items.isEmpty()) {
            Text("—", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                items.forEach { it ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text("•", modifier = Modifier.width(14.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBadge(rating: Double) {
    Surface(color = com.cis_ac.cis_ac.ui.theme.Quaternary, shape = RoundedCornerShape(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.Black)
            Spacer(Modifier.width(6.dp))
            Text(String.format(Locale.getDefault(), "%.1f", rating), style = MaterialTheme.typography.labelMedium, color = Color.Black)
        }
    }
}

@Composable
private fun TestimonialCard(t: Testimonial) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Person, null) } }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(t.author, style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Hace ${t.monthsAgo} mes${if (t.monthsAgo == 1) "" else "es"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { idx ->
                        val filled = idx < t.stars
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (filled) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
            Text(t.text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
