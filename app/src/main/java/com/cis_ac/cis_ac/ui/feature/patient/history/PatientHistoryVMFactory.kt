package com.cis_ac.cis_ac.ui.feature.patient.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.cis_ac.cis_ac.data.patient.history.FirestorePatientHistoryRepository

@Suppress("UNCHECKED_CAST")
class PatientHistoryVMFactory(
    private val patientUid: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val repo = FirestorePatientHistoryRepository(FirebaseFirestore.getInstance())
        val auth = FirebaseAuth.getInstance()
        return PatientHistoryViewModel(
            repo = repo,
            auth = auth,
            db = FirebaseFirestore.getInstance(),
            targetPatientId = patientUid
        ) as T
    }
}
