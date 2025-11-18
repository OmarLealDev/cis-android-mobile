package com.cis_ac.cis_ac.data.storage

import android.content.Context
import android.net.Uri
import com.cis_ac.cis_ac.core.Result
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) : StorageRepository {

    override suspend fun uploadFile(
        context: Context,
        uri: Uri,
        userId: String,
        fileName: String,
        folder: String
    ): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.Error("No se pudo leer el archivo")

            val fileExtension = getFileExtension(context, uri)
            val uniqueFileName = "${fileName}_${UUID.randomUUID()}.$fileExtension"
            val storageRef = storage.reference
                .child("users")
                .child(userId)
                .child(folder)
                .child(uniqueFileName)

            val uploadTask = storageRef.putStream(inputStream)
            uploadTask.await()

            val downloadUrl = storageRef.downloadUrl.await()
            Result.Success(downloadUrl.toString())

        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al subir el archivo")
        }
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Boolean> {
        return try {
            val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl)
            storageRef.delete().await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al eliminar el archivo")
        }
    }

    private fun getFileExtension(context: Context, uri: Uri): String {
        return context.contentResolver.getType(uri)?.let { mimeType ->
            when (mimeType) {
                "application/pdf" -> "pdf"
                else -> "pdf"
            }
        } ?: "pdf"
    }
}
