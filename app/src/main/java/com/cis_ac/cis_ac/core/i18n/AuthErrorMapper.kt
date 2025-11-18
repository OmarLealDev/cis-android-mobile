package com.cis_ac.cis_ac.core.i18n

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

fun authErrorToSpanish(t: Throwable?, fallback: String? = null): String {
    return when (t) {
        is FirebaseAuthInvalidCredentialsException -> "Las credenciales son incorrectas. Verifica tu correo y contraseña."
        is FirebaseAuthInvalidUserException -> when (t.errorCode) {
            "ERROR_USER_DISABLED" -> "Tu cuenta está deshabilitada."
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con ese correo."
            else -> "No pudimos encontrar tu cuenta."
        }
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con este correo."
        is FirebaseTooManyRequestsException -> "Demasiados intentos. Inténtalo de nuevo más tarde."
        is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo."
        is FirebaseAuthRecentLoginRequiredException -> "Por seguridad, vuelve a iniciar sesión para continuar."
        is FirebaseAuthEmailException -> "El correo electrónico no es válido."
        is FirebaseAuthException -> when (t.errorCode) {
            "ERROR_INVALID_EMAIL"   -> "El correo electrónico no es válido."
            "ERROR_WRONG_PASSWORD"  -> "La contraseña es incorrecta."
            else -> fallback ?: "Ocurrió un error al iniciar sesión."
        }
        else -> {
            // Heurística por si solo llega el mensaje en inglés
            val msg = (fallback ?: t?.message).orEmpty()
            when {
                msg.contains("network", true)      -> "Sin conexión. Revisa tu internet."
                msg.contains("expired", true)
                        || msg.contains("invalid credential", true)
                        || msg.contains("malformed", true)
                        || msg.contains("wrong password", true)
                    -> "Las credenciales son incorrectas. Verifica tu correo y contraseña."
                else -> "Ocurrió un error al iniciar sesión."
            }
        }
    }
}
