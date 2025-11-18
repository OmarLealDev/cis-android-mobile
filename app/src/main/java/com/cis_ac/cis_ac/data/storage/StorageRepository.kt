package com.cis_ac.cis_ac.data.storage

import android.content.Context
import android.net.Uri
import com.cis_ac.cis_ac.core.Result

interface StorageRepository {
    suspend fun uploadFile(
        context: Context,
        uri: Uri,
        userId: String,
        fileName: String,
        folder: String = "documents"
    ): Result<String>

    suspend fun deleteFile(downloadUrl: String): Result<Boolean>
}