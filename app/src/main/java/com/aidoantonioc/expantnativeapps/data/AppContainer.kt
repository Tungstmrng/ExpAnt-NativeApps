package com.aidoantonioc.expantnativeapps.data

import android.content.Context
import com.aidoantonioc.expantnativeapps.data.local.AppDatabase
import com.aidoantonioc.expantnativeapps.data.remote.ApiConfig
import com.aidoantonioc.expantnativeapps.data.remote.ReceiptApi
import com.aidoantonioc.expantnativeapps.data.remote.ReceiptImagePreparer
import com.aidoantonioc.expantnativeapps.data.repository.ReceiptScanRepository
import com.aidoantonioc.expantnativeapps.data.repository.ReceiptScanRepositoryImpl
import com.aidoantonioc.expantnativeapps.data.repository.TransactionRepository
import com.aidoantonioc.expantnativeapps.data.repository.TransactionRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    // --- Local ---
    private val database: AppDatabase by lazy { AppDatabase.build(appContext) }

    val transactionRepository: TransactionRepository by lazy {
        TransactionRepositoryImpl(database.transactionDao())
    }

    // --- Remote ---
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS) // OCR can take a while
            // BASIC logs only the request line and status. BODY would dump the whole image into Logcat.
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val receiptScanRepository: ReceiptScanRepository by lazy {
        ReceiptScanRepositoryImpl(
            api = retrofit.create(ReceiptApi::class.java),
            imagePreparer = ReceiptImagePreparer(appContext)
        )
    }
}