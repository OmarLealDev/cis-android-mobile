package com.cis_ac.cis_ac.ui.feature.home.admin.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.home.admin.requests.components.RequestItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsRoute(
    vm: RequestsViewModel = viewModel(),
    onBack: () -> Unit,
    onOpenDetail: (String ) -> Unit
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Surface(tonalElevation = 0.dp, shadowElevation = 2.dp) {
                Column {
                    CenterAlignedTopAppBar(
                        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, null) } },
                        title = { Text("Solicitudes de registro") }
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
            }
        }
    ) { padding ->
        when {
            ui.loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error.isNotEmpty() -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error: ${ui.error}", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                ElevatedButton(onClick = { }) { Text("Reintentar") }
            }
            else -> LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                items(ui.pending, key = { it.uid }) { pro ->
                    RequestItemCard(
                        pro = pro,
                        onOpenDetail = { onOpenDetail(pro.uid) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}
