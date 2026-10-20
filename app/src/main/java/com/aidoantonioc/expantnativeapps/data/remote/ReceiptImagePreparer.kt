package com.aidoantonioc.expantnativeapps.data.remote


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

class ReceiptImagePreparer(private val context: Context) {

    suspend fun prepare(uri: Uri): File = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver

        // 1. Read only the size, so we can decode a smaller bitmap and save memory.
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2

        // 2. Decode.
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalStateException("Could not read the selected image")

        // 3. Rotate according to EXIF and scale down to MAX_SIDE.
        val orientation = resolver.openInputStream(uri)?.use {
            ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        val scale = min(1f, MAX_SIDE.toFloat() / max(decoded.width, decoded.height))
        matrix.postScale(scale, scale)

        val result = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)

        // 4. Save as JPEG in the cache.
        val out = File(context.cacheDir, "scan_${System.currentTimeMillis()}.jpg")
        out.outputStream().use { result.compress(Bitmap.CompressFormat.JPEG, 85, it) }

        if (result !== decoded) decoded.recycle()
        result.recycle()
        out
    }

    private companion object {
        const val MAX_SIDE = 2000
    }
}