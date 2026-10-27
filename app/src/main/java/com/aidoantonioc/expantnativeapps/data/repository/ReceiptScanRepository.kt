package com.aidoantonioc.expantnativeapps.data.repository

import android.net.Uri
import com.aidoantonioc.expantnativeapps.data.remote.ReceiptApi
import com.aidoantonioc.expantnativeapps.data.remote.ReceiptImagePreparer
import com.aidoantonioc.expantnativeapps.data.remote.toTransactionDetails
import com.aidoantonioc.expantnativeapps.domain.model.TransactionDetails
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

interface ReceiptScanRepository {
    suspend fun scan(imageUri: Uri): Result<TransactionDetails>
}

class ReceiptScanRepositoryImpl(
    private val api: ReceiptApi,
    private val imagePreparer: ReceiptImagePreparer
) : ReceiptScanRepository {

    override suspend fun scan(imageUri: Uri): Result<TransactionDetails> {
        var file: File? = null
        return try {
            file = imagePreparer.prepare(imageUri)
            val part = MultipartBody.Part.createFormData(
                name = "receipt", // must match upload.single('receipt') in server.js
                filename = file.name,
                body = file.asRequestBody("image/jpeg".toMediaType())
            )
            Result.success(api.scan(part).toTransactionDetails())
        } catch (e: CancellationException) {
            throw e // never swallow coroutine cancellation
        } catch (e: HttpException) {
            Result.failure(Exception("Server returned an error (${e.code()}).", e))
        } catch (e: JsonParseException) {
            Result.failure(Exception("The server's response wasn't in the expected format.", e))
        } catch (e: IOException) {
            Result.failure(Exception("Can't reach the server. Is the backend running, and is the URL correct?", e))
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            file?.delete()
        }
    }
}