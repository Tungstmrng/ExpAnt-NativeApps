package com.aidoantonioc.expantnativeapps.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Everything is nullable: OCR output is messy, and Gson ignores Kotlin null-safety.
data class ReceiptScanResponse(
    @SerializedName("merchant_name") val merchantName: String?,
    val date: String?,
    @SerializedName("total_amount") val totalAmount: Double?,
    val items: List<ReceiptItemDto>?
)

data class ReceiptItemDto(
    val name: String?,
    val price: Double?,
    val qty: Int?,
    val category: String?,
    val discount: Double?
)

interface ReceiptApi {
    @Multipart
    @POST("api/scan")
    suspend fun scan(@Part image: MultipartBody.Part): ReceiptScanResponse
}