package com.cis_ac.cis_ac.ui.feature.chat.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.chat.utils.getUserDisplayName
import com.cis_ac.cis_ac.data.userprofile.FirestoreUserProfileRepository

@Composable
fun StartChatScreen(
    otherUserId: String,
    onNavigateToChat: (String, String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfileRepository = remember { FirestoreUserProfileRepository() }
    var isLoading by remember { mutableStateOf(true) }
    var userName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(otherUserId) {
        try {
            userName = getUserDisplayName(otherUserId, userProfileRepository)
            onNavigateToChat(otherUserId, userName)
        } catch (e: Exception) {
            error = e.message ?: "Error obteniendo información del usuario"
            isLoading = false
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (error != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = error ?: "Error desconocido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBackClick) {
                    Text("Volver")
                }
            }
        }
    }
}
