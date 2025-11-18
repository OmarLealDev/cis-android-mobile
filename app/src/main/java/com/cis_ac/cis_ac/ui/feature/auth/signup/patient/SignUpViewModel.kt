package com.cis_ac.cis_ac.ui.feature.auth.login.signup.patient

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.userprofile.FirestoreUserProfileRepository
import com.cis_ac.cis_ac.data.userprofile.UserProfileRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.domain.factory.UserProfileFactory

class SignUpViewModel (
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val firestoreRepository: UserProfileRepository = FirestoreUserProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun onFullNameChange(value: String) = _uiState.update {
        it.copy(fullName = value, fullNameError = validateName(value))
    }

    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value.trim(), emailError = validateEmail(value))
    }

    fun onPhoneChange(value: String) = _uiState.update {
        val onlyDigits = value.filter { ch -> ch.isDigit() }.take(10)
        it.copy(phone = onlyDigits, phoneError = validatePhone(onlyDigits))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDobSelected(selectedMillis: Long) = _uiState.update {
        val localDate = Instant.ofEpochMilli(selectedMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val formatted = localDate.toString()
        it.copy(dob = formatted, dobError = validateDob(formatted))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDobChange(value: String) = _uiState.update {
        it.copy(dob = value, dobError = validateDob(value))
    }

    fun onGenderChange(value: Gender) = _uiState.update {
        it.copy(gender = value, genderError = if (value == Gender.Unspecified) "Selecciona un género" else "")
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = validatePassword(value), confirmPasswordError = validateConfirm(value, it.confirmPassword))
    }

    fun onConfirmPasswordChange(value: String) = _uiState.update {
        it.copy(confirmPassword = value, confirmPasswordError = validateConfirm(it.password, value))
    }

    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onToggleConfirmVisibility() = _uiState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }

    fun onAcceptTermsChange(value: Boolean) = _uiState.update {
        it.copy(acceptedTerms = value, termsError = if (!value) "Debes aceptar los términos" else "")
    }

    fun clearMessages() = _uiState.update {
        it.copy(successMessage = "", errorMessage = "")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun submit(onSuccess: () -> Unit, onError: (String) -> Unit) {
        _uiState.update { it.copy(successMessage = "", errorMessage = "") }
        
        _uiState.update { s ->
            s.copy(
                fullNameError = validateName(s.fullName),
                emailError = validateEmail(s.email),
                phoneError = validatePhone(s.phone),
                dobError = validateDob(s.dob),
                genderError = if (s.gender == Gender.Unspecified) "Selecciona un género" else "",
                passwordError = validatePassword(s.password),
                confirmPasswordError = validateConfirm(s.password, s.confirmPassword),
                termsError = if (!s.acceptedTerms) "Debes aceptar los términos" else ""
            )
        }

        val current = _uiState.value
        if (!current.isFormValid) {
            _uiState.update { it.copy(errorMessage = "Revisa los campos marcados.") }
            onError("Revisa los campos marcados.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val current = _uiState.value
            try {
                when (val res = authRepository.signUp(current.email, current.password)) {
                    is Result.Success<String> -> {
                        val uid = res.data

                        val user = UserProfileFactory.createUserProfile(
                            uid = uid,
                            email = current.email.trim(),
                            role = UserRole.PATIENT,
                            details = mapOf(
                                "fullName" to current.fullName.trim(),
                                "phone" to current.phone,
                                "dob"    to current.dob,
                                "gender"         to current.gender
                            )
                        ) ?: run {
                            val errorMsg = "No se pudo construir el perfil de usuario"
                            _uiState.update { it.copy(errorMessage = errorMsg) }
                            onError(errorMsg)
                            return@launch
                        }

                        when (val save = firestoreRepository.createUserProfile(user)) {
                            is Result.Success<String> -> {
                                println("DEBUG: Firestore save successful, showing success message")
                                _uiState.update { it.copy(successMessage = "¡Cuenta creada exitosamente! Redirigiendo...") }
                                delay(1500)
                                println("DEBUG: About to call onSuccess")
                                onSuccess()
                            }
                            is Result.Error -> {
                                println("DEBUG: Firestore save error: ${save.message}")
                                _uiState.update { it.copy(errorMessage = save.message) }
                                onError(save.message)
                            }
                            is Result.Loading -> Unit
                            is Result.UserProfile<*> -> TODO()
                        }
                    }
                    is Result.Error -> {
                        _uiState.update { it.copy(errorMessage = res.message) }
                        onError(res.message)
                    }
                    is Result.Loading -> Unit
                    is Result.UserProfile<*> -> TODO()
                }
            } catch (e: Exception) {
                val errorMsg = e.message ?: "Error inesperado"
                _uiState.update { it.copy(errorMessage = errorMsg) }
                onError(errorMsg)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun validateName(name: String): String =
        when {
            name.isBlank() -> "Ingresa tu nombre completo"
            name.length < 3  -> "Nombre demasiado corto"
            else -> ""
        }

    private fun validateEmail(email: String): String {
        val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return when {
            email.isBlank() -> "Ingresa tu correo"
            !regex.matches(email) -> "Correo inválido"
            else -> ""
        }
    }

    private fun validatePhone(phone: String): String =
        when {
            phone.isBlank() -> "Ingresa tu teléfono"
            phone.any { !it.isDigit() } -> "Solo dígitos"
            phone.length != 10 -> "Debe tener exactamente 10 dígitos"
            else -> ""
        }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateDob(dob: String): String {
        if (dob.isBlank()) return "Ingresa tu fecha de nacimiento"
        return try {
            val date = LocalDate.parse(dob)
            val today = LocalDate.now()
            if (date.isAfter(today)) "La fecha no puede ser futura" else ""
        } catch (e: Exception) {
            "Usa el formato AAAA-MM-DD"
        }
    }

    private fun validatePassword(pw: String): String =
        when {
            pw.isBlank() -> "Ingresa una contraseña"
            pw.length < 8 -> "Mínimo 8 caracteres"
            !pw.any { it.isDigit() } || !pw.any { it.isLetter() } -> "Debe incluir letras y números"
            else -> ""
        }

    private fun validateConfirm(pw: String, confirm: String): String =
        when {
            confirm.isBlank() -> "Confirma la contraseña"
            pw != confirm -> "Las contraseñas no coinciden"
            else -> ""
        }
}