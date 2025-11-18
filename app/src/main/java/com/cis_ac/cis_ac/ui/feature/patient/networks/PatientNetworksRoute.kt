package com.cis_ac.cis_ac.ui.feature.patient.networks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cis_ac.cis_ac.core.model.social.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientNetworksRoute(
    vm: PatientNetworksViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit
) {
    val state by vm.ui.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { vm.refreshOnce() }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 6.dp) {
                CenterAlignedTopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                    title = { Text("Redes") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when {
            state.loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.error.isNotEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                ElevatedButton(onClick = vm::refreshOnce) { Text("Reintentar") }
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        onOpen = { onOpenDetail(post.id) },
                        onLike = { vm.toggleLike(post.id) }
                    )
                }
                item { Spacer(Modifier.height(64.dp)) }
            }
        }
    }
}

@Composable
private fun PostCard(
    post: Post,
    onOpen: () -> Unit,
    onLike: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                post.authorName.ifBlank { "Profesional" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            // Texto
            if (post.text.isNotBlank()) {
                Text(post.text, style = MaterialTheme.typography.bodyMedium)
            }

            post.imageUrl?.let { url ->
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 140.dp, max = 220.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = onLike,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    val icon = if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                    Icon(icon, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${post.likeCount}")
                }

                OutlinedButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.ChatBubbleOutline, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("${post.commentCount}")
                }
            }
        }
    }
}
