package com.cis_ac.cis_ac.data.patient

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.Patient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import com.cis_ac.cis_ac.data.patient.dto.PatientFS

class FirestorePatientProfileRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : PatientProfileRepository {

    private val col = db.collection("patients")


    override suspend fun getCurrent(): Result<Patient> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.Error("No hay sesión")
            val snap = db.collection("patients").document(uid).get().await()
            val dto  = snap.toObject(PatientFS::class.java)
            val patient = dto?.toDomain() ?: return Result.Error("No se encontró el perfil")
            Result.Success(patient)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al obtener paciente", e)
        }
    }

    override suspend fun update(fullName: String, email: String, phone: String): Result<Unit> = try {
        val uid = auth.currentUser?.uid ?: return Result.Error("No session")
        col.document(uid)
            .update(
                mapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone
                )
            ).await()
        if (auth.currentUser?.email != email) {
            auth.currentUser?.updateEmail(email)?.await()
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(e.message ?: "Error al guardar cambios", e)
    }
}
