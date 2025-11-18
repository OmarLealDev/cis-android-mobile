package com.cis_ac.cis_ac.ui.feature.auth.login.signup.patient

import com.cis_ac.cis_ac.core.model.Gender

import androidx.compose.runtime.Immutable

@Immutable
data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val dob: String = "",
    val gender: Gender = Gender.Unspecified,
    val password: String = "",
    val confirmPassword: String = "",
    val acceptedTerms: Boolean = false,

    val isPasswordVisible: Boolean = false,
    val isConfirmVisible: Boolean = false,
    val isLoading: Boolean = false,

    val successMessage: String = "",
    val errorMessage: String = "",

    val fullNameError: String = "",
    val emailError: String = "",
    val phoneError: String = "",
    val dobError: String = "",
    val genderError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val termsError: String = ""
) {
    val isFormValid: Boolean
        get() = fullNameError.isEmpty()
                && emailError.isEmpty()
                && phoneError.isEmpty()
                && dobError.isEmpty()
                && genderError.isEmpty()
                && passwordError.isEmpty()
                && confirmPasswordError.isEmpty()
                && termsError.isEmpty()
                && fullName.isNotBlank()
                && email.isNotBlank()
                && phone.length == 10
                && dob.isNotBlank()
                && gender != Gender.Unspecified
                && password.isNotBlank()
                && confirmPassword.isNotBlank()
                && acceptedTerms
}