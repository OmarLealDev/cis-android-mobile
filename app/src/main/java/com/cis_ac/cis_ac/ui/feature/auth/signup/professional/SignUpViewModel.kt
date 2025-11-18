package com.cis_ac.cis_ac.ui.feature.auth.signup.professional

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Discipline
import com.cis_ac.cis_ac.core.model.Gender
import com.cis_ac.cis_ac.core.model.UserRole
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.cis_ac.cis_ac.data.storage.FirebaseStorageRepository
import com.cis_ac.cis_ac.data.storage.StorageRepository
import com.cis_ac.cis_ac.data.userprofile.FirestoreUserProfileRepository
import com.cis_ac.cis_ac.data.userprofile.UserProfileRepository
import com.cis_ac.cis_ac.domain.factory.UserProfileFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SignUpViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val firestoreRepository: UserProfileRepository = FirestoreUserProfileRepository(),
    private val storageRepository: StorageRepository = FirebaseStorageRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    /* -------- Inputs básicos -------- */
    fun onFullNameChange(v: String) = _uiState.update { it.copy(fullName = v, fullNameError = "") }
    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v.trim(), emailError = "") }
    fun onPhoneChange(v: String)    = _uiState.update { it.copy(phone = v, phoneError = "") }

    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v, passwordError = "") }
    fun onConfirmPasswordChange(v: String) =
        _uiState.update { it.copy(confirmPassword = v, confirmPasswordError = "") }

    fun onTogglePasswordVisibility() = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    fun onToggleConfirmVisibility()  = _uiState.update { it.copy(isConfirmVisible = !it.isConfirmVisible) }

    fun onLicenseChange(v: String) = _uiState.update { it.copy(licenseNumber = v.trim(), licenseError = "") }

    fun onGenderChange(v: Gender) = _uiState.update { it.copy(gender = v, genderError = "") }
    fun onDisciplineChange(v: Discipline) = _uiState.update {
        val cleanEnfoque = if (v == Discipline.PSYCHOLOGY) it.enfoque else ""
        it.copy(discipline = v, disciplineError = "", enfoque = cleanEnfoque, enfoqueError = "")
    }
    fun onEnfoqueChange(v: String) = _uiState.update { it.copy(enfoque = v, enfoqueError = "") }

    fun onSpecialityChange(v: String) = _uiState.update {it.copy(speciality = v, specialityError = "")}
    fun onTopicsChange(v: String)     = _uiState.update { it.copy(topics = v, topicsError = "")}
    fun onExpertizChange(v: String)   = _uiState.update { it.copy(expertiz = v, expertizError = "") }
    fun onSemblanceChange(v: String)  = _uiState.update { it.copy(semblance = v) }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onDobSelected(ms: Long) {
        val date = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
        _uiState.update { it.copy(dob = date.toString(), dobError = "") }
    }

    fun onCvPicked(uri: Uri)      = _uiState.update { it.copy(cvUri = uri, cvError = "") }
    fun onLicensePicked(uri: Uri) = _uiState.update { it.copy(licenseUri = uri, cedulaError = "") }

    fun onAcceptTermsChange(v: Boolean) = _uiState.update { it.copy(acceptedTerms = v, termsError = "") }

    /* -------- Múltiple selección (chips) -------- */
    fun toggleModality(v: Int) = _uiState.update { s ->
        s.copy(modalities = s.modalities.toMutableSet().apply { if (!add(v)) remove(v) })
    }
    fun toggleSessionType(v: Int) = _uiState.update { s ->
        s.copy(sessionTypes = s.sessionTypes.toMutableSet().apply { if (!add(v)) remove(v) })
    }
    fun togglePopulation(v: Int) = _uiState.update { s ->
        s.copy(populations = s.populations.toMutableSet().apply { if (!add(v)) remove(v) })
    }

    /* -------- Agenda (día Int -> horas) -------- */
    fun onScheduleChange(map: Map<Int, List<Int>>) = _uiState.update { it.copy(schedule = map) }

    /* ---------------------- SUBMIT ---------------------- */
    @RequiresApi(Build.VERSION_CODES.O)
    fun submit(
        context: android.content.Context,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        _uiState.update { it.copy(successMessage = "", errorMessage = "") }

        // Prevalidación
        _uiState.update { s ->
            s.copy(
                fullNameError = validateName(s.fullName),
                emailError = validateEmail(s.email),
                phoneError = validatePhone(s.phone),
                passwordError = validatePassword(s.password),
                confirmPasswordError = validateConfirm(s.password, s.confirmPassword),
                licenseError = validateLicense(s.licenseNumber),
                disciplineError = if (s.discipline == null) "Selecciona una especialidad" else "",
                enfoqueError = if (s.discipline == Discipline.PSYCHOLOGY) validateEnfoque(s.enfoque) else "",
                dobError = validateDob(s.dob),
                genderError = if (s.gender == Gender.Unspecified) "Selecciona un género" else "",
                cvError = if (s.cvUri == null) "Debes subir tu CV" else "",
                cedulaError = if (s.licenseUri == null) "Debes subir tu cédula profesional" else "",
                termsError = if (!s.acceptedTerms) "Debes aceptar los términos" else ""
            )
        }

        val current = _uiState.value
        if (!current.isFormValid) {
            val msg = "Revisa los campos marcados."
            _uiState.update { it.copy(errorMessage = msg) }
            onError(msg)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                when (val res = authRepository.signUp(current.email, current.password)) {
                    is Result.Success<String> -> {
                        val uid = res.data

                        // Subidas
                        var cvUrl: String? = null
                        var licenseUrl: String? = null

                        current.cvUri?.let { uri ->
                            when (val up = storageRepository.uploadFile(context, uri, uid, "cv", "documents")) {
                                is Result.Success -> cvUrl = up.data
                                is Result.Error   -> { fail("Error al subir CV: ${up.message}", onError); return@launch }
                                else -> Unit
                            }
                        }

                        current.licenseUri?.let { uri ->
                            when (val up = storageRepository.uploadFile(context, uri, uid, "license", "documents")) {
                                is Result.Success -> licenseUrl = up.data
                                is Result.Error   -> { fail("Error al subir cédula: ${up.message}", onError); return@launch }
                                else -> Unit
                            }
                        }

                        // Firestore: schedule necesita claves String
                        val scheduleForFs: Map<String, List<Int>> =
                            current.schedule.mapKeys { (k, _) -> k.toString() }

                        val detailsMap = mutableMapOf<String, Any?>(
                            "fullName" to current.fullName.trim(),
                            "phone" to current.phone.trim(),
                            "dob" to current.dob,
                            "gender" to current.gender,
                            "licenseNumber" to current.licenseNumber.trim(),
                            "mainDiscipline" to current.discipline,
                            "verified" to false,
                            "active" to true,
                            "cvUrl" to cvUrl,
                            "licenseUrl" to licenseUrl,
                            "createdAt" to System.currentTimeMillis(),

                            // Texto libre
                            "speciality" to current.speciality.trim(),
                            "approach" to current.enfoque.trim(),
                            "topics" to current.topics.trim(),
                            "expertiz" to current.expertiz.trim(),
                            "semblance" to current.semblance.trim(),

                            // Arrays de Int
                            "modalities" to current.modalities.toList(),
                            "sessionTypes" to current.sessionTypes.toList(),
                            "populations" to current.populations.toList(),

                            // Agenda
                            "schedule" to scheduleForFs
                        )

                        val user = UserProfileFactory.createUserProfile(
                            uid = uid,
                            email = current.email.trim(),
                            role = UserRole.PROFESSIONAL,
                            details = detailsMap
                        ) ?: run { fail("No se pudo construir el perfil de usuario", onError); return@launch }

                        when (val save = firestoreRepository.createUserProfile(user)) {
                            is Result.Success -> {
                                _uiState.update { it.copy(successMessage = "¡Cuenta creada exitosamente! Redirigiendo...") }
                                delay(1200)
                                onSuccess()
                            }
                            is Result.Error -> fail(save.message, onError)
                            else -> Unit
                        }
                    }
                    is Result.Error -> fail(res.message, onError)
                    else -> Unit
                }
            } catch (e: Exception) {
                fail(e.message ?: "Error inesperado", onError)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun fail(msg: String, onError: (String) -> Unit) {
        _uiState.update { it.copy(errorMessage = msg) }
        onError(msg)
    }

    /* ---------------- Validaciones ---------------- */
    private fun validateName(name: String): String = when {
        name.isBlank() -> "Ingresa tu nombre completo"
        name.length < 3 -> "Nombre demasiado corto"
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

    private fun validatePhone(phone: String): String = when {
        phone.isBlank() -> "Ingresa tu número de teléfono"
        phone.any { !it.isDigit() } -> "Solo se permiten números"
        phone.length != 10 -> "Debe tener exactamente 10 dígitos"
        else -> ""
    }

    private fun validatePassword(pw: String): String = when {
        pw.isBlank() -> "Ingresa una contraseña"
        pw.length < 8 -> "Mínimo 8 caracteres"
        !pw.any { it.isDigit() } || !pw.any { it.isLetter() } -> "Debe incluir letras y números"
        else -> ""
    }

    private fun validateConfirm(pw: String, confirm: String): String = when {
        confirm.isBlank() -> "Confirma la contraseña"
        pw != confirm -> "Las contraseñas no coinciden"
        else -> ""
    }

    private fun validateLicense(lic: String): String = when {
        lic.isBlank() -> "Ingresa tu cédula"
        lic.length < 5 -> "Cédula inválida"
        else -> ""
    }

    private fun validateEnfoque(enfoque: String): String = when {
        enfoque.isBlank() -> "Ingresa el enfoque psicológico"
        enfoque.length < 3 -> "Enfoque demasiado corto"
        else -> ""
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateDob(dob: String): String {
        if (dob.isBlank()) return "Ingresa tu fecha de nacimiento"
        return try {
            val d = LocalDate.parse(dob)
            if (d.isAfter(LocalDate.now())) "La fecha no puede ser futura" else ""
        } catch (_: Exception) { "Usa el formato AAAA-MM-DD" }
    }
}
