package com.cis_ac.cis_ac.data.patient.history

import com.cis_ac.cis_ac.core.Result
import com.cis_ac.cis_ac.core.model.history.GeneralInfo
import com.cis_ac.cis_ac.core.model.history.HistoryEntry
import com.cis_ac.cis_ac.core.model.history.UserRef

interface PatientHistoryRepository {
    suspend fun loadGeneral(patientId: String): Result<GeneralInfo>
    suspend fun saveGeneral(patientId: String, info: GeneralInfo, actor: UserRef): Result<Unit>

    suspend fun listNotes(patientId: String, section: String): Result<List<HistoryEntry>>
    suspend fun addNote(patientId: String, section: String, date: String, text: String, actor: UserRef): Result<String>
    suspend fun updateNote(patientId: String, noteId: String, date: String, text: String, actor: UserRef): Result<Unit>
}
