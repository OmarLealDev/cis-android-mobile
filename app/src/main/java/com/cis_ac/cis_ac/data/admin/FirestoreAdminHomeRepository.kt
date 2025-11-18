package com.cis_ac.cis_ac.data.admin

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreAdminHomeRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : AdminHomeRepository {

    private val pros = db.collection("professionals")

    override suspend fun getActiveProfessionalsCount(): Int {
        // Activos = verified == true && active == true
        val q = pros.whereEqualTo("verified", true)
            .whereEqualTo("active", true)
        return try {
            q.count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Throwable) {
            // fallback si no está disponible count()
            q.get().await().size()
        }
    }

    override suspend fun getPendingRequestsCount(): Int {
        // Pendientes = verified == false
        val q = pros.whereEqualTo("verified", false)
            .whereEqualTo("active", true)
        return try {
            q.count().get(AggregateSource.SERVER).await().count.toInt()
        } catch (_: Throwable) {
            q.get().await().size()
        }
    }

    override suspend fun getAdminDisplayName(): String {
        return auth.currentUser?.displayName ?: "Administrador"
    }
}
