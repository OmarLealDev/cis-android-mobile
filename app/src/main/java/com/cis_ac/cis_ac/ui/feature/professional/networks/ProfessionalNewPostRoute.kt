package com.cis_ac.cis_ac.ui.feature.professional.networks

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfessionalNewPostRoute(
    onBack: () -> Unit,
    onPosted: (String) -> Unit,
    vm: ProfessionalNewPostViewModel = viewModel()
) {
    val ui = vm.ui.collectAsStateWithLifecycle().value
    val snackbar = remember { SnackbarHostState() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        vm.onImagePicked(uri)
    }

    LaunchedEffect(ui.success) {
        if (ui.success) {
            val msg = "¡Publicación creada!"
            snackbar.showSnackbar(msg)
            vm.consumeSuccess()
            onPosted(msg)
        }
    }

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 6.dp) {
                CenterAlignedTopAppBar(
                    navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                    title = { Text("Nueva publicación") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { if (ui.loading) Text("Publicando...") else Text("Publicar") },
                icon = {
                    if (ui.loading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                onClick = { if (!ui.loading) vm.submit() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                ) {}
                Spacer(Modifier.width(10.dp))
            }

            OutlinedTextField(
                value = ui.text,
                onValueChange = vm::onTextChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Escribe algo...") },
                minLines = 5,
                shape = RoundedCornerShape(14.dp)
            )

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Image, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar imagen (opcional)")
            }

            ui.imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (ui.error.isNotEmpty()) {
                Text(ui.error, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(72.dp))
        }
    }
}
