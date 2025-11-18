package com.cis_ac.cis_ac.ui.feature.auth.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null,

    val triedLogin: Boolean = false,
)