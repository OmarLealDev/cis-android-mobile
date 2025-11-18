package com.cis_ac.cis_ac.ui.feature.professional.networks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cis_ac.cis_ac.ui.feature.shared.PostDetailScreen

@Composable
fun ProfessionalPostDetailRoute(
    postId: String,
    onBack: () -> Unit,
    vm: ProfessionalPostDetailViewModel = viewModel(factory = ProfessionalPostDetailVMFactory(postId))
) {
    val ui = vm.ui.collectAsStateWithLifecycle().value

    LaunchedEffect(Unit) { vm.load() }

    PostDetailScreen(
        post = ui.post,
        comments = ui.comments,
        loading = ui.loading,
        error = ui.error,
        onBack = onBack,
        onToggleLike = vm::toggleLike,
        onSendComment = vm::addComment
    )
}
