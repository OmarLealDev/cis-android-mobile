package com.cis_ac.cis_ac.ui.feature.home.patient.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ReviewPromptDialog(
    professionalName: String?,
    saving: Boolean,
    onSubmit: (rating: Int, comment: String) -> Unit,
    onMissed: (comment: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onSubmit(rating, comment.trim()) },
                enabled = rating in 1..5 && !saving
            ) { if (saving) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(18.dp)) else Text("Enviar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, enabled = !saving) { Text("Cancelar") }
                TextButton(onClick = { onMissed(comment.ifBlank { null }) }, enabled = !saving) { Text("No se tuvo la cita") }
            }
        },
        title = { Text("Califica al profesional", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Tu opinión es muy importante para nosotros")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    (1..5).forEach { i ->
                        IconButton(onClick = { rating = i }) {
                            if (i <= rating) Icon(Icons.Filled.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            else Icon(Icons.Outlined.Star, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    minLines = 4,
                    maxLines = 6,
                    placeholder = { Text("Escribe tus comentarios aquí…") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (!professionalName.isNullOrBlank()) {
                    Text("Profesional: $professionalName", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
