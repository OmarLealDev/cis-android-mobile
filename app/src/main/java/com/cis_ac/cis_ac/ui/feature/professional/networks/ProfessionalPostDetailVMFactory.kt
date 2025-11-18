package com.cis_ac.cis_ac.ui.feature.professional.networks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Suppress("UNCHECKED_CAST")
class ProfessionalPostDetailVMFactory(
    private val postId: String) : ViewModelProvider.Factory
{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfessionalPostDetailViewModel(postId) as T
    }
}

