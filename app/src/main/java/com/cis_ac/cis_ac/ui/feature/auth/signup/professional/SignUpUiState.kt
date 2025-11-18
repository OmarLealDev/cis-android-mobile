package com.cis_ac.cis_ac.ui.feature.auth.signup.professional

import androidx.compose.runtime.Immutable
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.Discipline
import android.net.Uri

@Immutable
data class SignUpUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",

    val licenseNumber: String = "",
    val discipline: Discipline? = null,
    val speciality: String = "",
    val enfoque: String = "",
    val topics: String = "",
    val expertiz: String = "",

    val modalities: Set<Int> = emptySet(),
    val sessionTypes: Set<Int> = emptySet(),
    val populations: Set<Int> = emptySet(),

    val schedule: Map<Int, List<Int>> = emptyMap(),

    val semblance: String = "",

    val dob: String = "",
    val gender: Gender = Gender.Unspecified,

    val cvUri: Uri? = null,
    val licenseUri: Uri? = null,

    val isPasswordVisible: Boolean = false,
    val isConfirmVisible: Boolean = false,
    val isLoading: Boolean = false,
    val acceptedTerms: Boolean = false,

    val fullNameError: String = "",
    val emailError: String = "",
    val passwordError: String = "",
    val confirmPasswordError: String = "",
    val phoneError: String = "",
    val licenseError: String = "",
    val disciplineError: String = "",
    val specialityError: String = "",
    val enfoqueError: String = "",
    val topicsError: String = "",
    val expertizError: String = "",
    val dobError: String = "",
    val genderError: String = "",
    val termsError: String = "",
    val cvError: String = "",
    val cedulaError: String = "",

    // Mensajes
    val successMessage: String = "",
    val errorMessage: String = ""
) {
    val isFormValid: Boolean
        get() {
            val baseOk =
                fullName.isNotBlank() &&
                        email.isNotBlank() &&
                        password.isNotBlank() &&
                        confirmPassword.isNotBlank() &&
                        phone.isNotBlank() &&
                        licenseNumber.isNotBlank() &&
                        discipline != null &&
                        dob.isNotBlank() &&
                        gender != Gender.Unspecified &&
                        acceptedTerms &&
                        cvUri != null &&
                        licenseUri != null &&
                        modalities.isNotEmpty() &&
                        sessionTypes.isNotEmpty() &&
                        populations.isNotEmpty() &&
                        listOf(
                            fullNameError, emailError, passwordError, confirmPasswordError, phoneError,
                            licenseError, disciplineError, dobError, genderError, termsError, cvError, cedulaError
                        ).all { it.isEmpty() }

            val enfoqueOk =
                if (discipline == Discipline.PSYCHOLOGY) enfoque.isNotBlank() && enfoqueError.isEmpty()
                else true

            return baseOk && enfoqueOk
        }
}
