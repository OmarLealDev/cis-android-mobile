package com.cis_ac.cis_ac.ui.feature.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cis_ac.cis_ac.core.model.social.Comment
import com.cis_ac.cis_ac.core.model.social.Post

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    post: Post?,
    comments: List<Comment>,
    loading: Boolean,
    error: String,
    onBack: () -> Unit,
    onToggleLike: () -> Unit,
    onSendComment: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 6.dp) {
                CenterAlignedTopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                    title = { Text("Publicación") }
                )
            }
        },
        bottomBar = {
            Surface(shadowElevation = 12.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escribe un comentario") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            val txt = comment.trim()
                            if (txt.isNotEmpty()) {
                                onSendComment(txt)
                                comment = ""
                            }
                        },
                        enabled = comment.isNotBlank(),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Enviar") }
                }
            }
        }
    ) { padding ->
        when {
            loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }

            error.isNotEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }

            post == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Post no disponible")
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.elevatedCardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(post.authorName.ifBlank { "Usuario" }, style = MaterialTheme.typography.titleSmall)
                                if (post.text.isNotBlank()) Text(post.text)

                                post.imageUrl?.let { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 160.dp, max = 280.dp)
                                            .clip(RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    FilledTonalButton(onClick = onToggleLike, shape = RoundedCornerShape(10.dp)) {
                                        val icon = if (post.likedByMe) Icons.Filled.Favorite else androidx.compose.material.icons.Icons.Outlined.FavoriteBorder
                                        Icon(icon, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("${post.likeCount}")
                                    }
                                }
                            }
                        }
                    }

                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "Sin comentarios",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(comments, key = { it.id }) { c ->
                            ElevatedCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.elevatedCardElevation(1.dp)
                            ) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(c.authorName.ifBlank { "Usuario" }, style = MaterialTheme.typography.labelLarge)
                                    Text(c.text)
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(64.dp)) }
                }
            }
        }
    }
}
