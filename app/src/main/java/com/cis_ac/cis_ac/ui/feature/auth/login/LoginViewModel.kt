package com.cis_ac.cis_ac.ui.feature.auth.login

import com.cis_ac.cis_ac.core.Result
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cis_ac.cis_ac.data.auth.AuthRepository
import com.cis_ac.cis_ac.data.auth.FirebaseAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private var _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.authState().collect { logged ->
                _uiState.update { it.copy(isLoggedIn = logged, isLoading = false) }
            }
        }
    }

    fun onEmailChange(v: String)    = _uiState.update { it.copy(email = v,     error = null) }
    fun onPasswordChange(v: String) = _uiState.update { it.copy(password = v,  error = null) }
    fun togglePasswordVisibility()  = _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }


    fun signIn(onSuccessNavigate: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val res = repo.signIn(_uiState.value.email, _uiState.value.password)) {
                is Result.Success -> {
                    val ok = ensureProfessionalIsActiveAndVerified()
                    if (ok) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                triedLogin = true,
                                error = null
                            )
                        }
                        onSuccessNavigate()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                triedLogin = true
                            )
                        }
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = res.message, triedLogin = true)
                    }
                }
                Result.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Result.UserProfile<*> -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun signUp(onUid: (String) -> Unit) =
        runAuth(block = { repo.signUp(_uiState.value.email, _uiState.value.password) }, onSuccess = onUid)

    fun signOut() = runAuth(block = { repo.signOut() })

    private fun <T> runAuth(
        block: suspend () -> Result<T>,
        onSuccess: (T) -> Unit = {}
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val res = block()) {
                is Result.Success -> {
                    onSuccess(res.data)
                    _uiState.update { it.copy(isLoading = false, error = null) }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = res.message, triedLogin = true) }
                Result.Loading   -> _uiState.update { it.copy(isLoading = true) }
                is Result.UserProfile<*> -> {  }
            }
        }
    }

    private suspend fun ensureProfessionalIsActiveAndVerified(): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        val snap = db.collection("professionals").document(uid).get().await()
        if (!snap.exists()) {
            return true
        }

        val active   = snap.getBoolean("active")   ?: false
        val verified = snap.getBoolean("verified") ?: false

        return if (active && verified) {
            true
        } else {
            repo.signOut()
            val msg = when {
                !active && !verified -> "Tu cuenta profesional está inactiva y pendiente de verificación."
                !active              -> "Tu cuenta profesional está inactiva. Contacta al administrador."
                else                 -> "Tu cuenta profesional está en revisión. Te avisaremos cuando sea verificada."
            }
            _uiState.update { it.copy(
                error = msg,
                isLoading = false,
                triedLogin = true
            ) }
            false
        }
    }
}
