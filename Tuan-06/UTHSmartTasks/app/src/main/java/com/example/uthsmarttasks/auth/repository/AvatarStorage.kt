package com.example.uthsmarttasks.auth.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.example.uthsmarttasks.BuildConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

internal object AvatarStorage {
    private const val MAX_BYTES = 1024 * 1024L
    private const val MAX_DIMENSION = 512

    val isAvailable: Boolean = BuildConfig.DEBUG &&
        (Build.HARDWARE.contains("ranchu") || Build.HARDWARE.contains("goldfish"))

    private val emulator by lazy {
        check(isAvailable)
        FirebaseStorage.getInstance().apply {
            useEmulator("10.0.2.2", 9199)
            maxDownloadRetryTimeMillis = 5_000
            maxUploadRetryTimeMillis = 8_000
        }
    }

    suspend fun load(uid: String): Bitmap? {
        val bytes = try {
            avatar(uid).getBytes(MAX_BYTES).await()
        } catch (error: StorageException) {
            if (error.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) return null
            throw error
        }
        return withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    }

    suspend fun upload(context: Context, uid: String, uri: Uri): Bitmap {
        val (bitmap, jpeg) = withContext(Dispatchers.IO) {
            val image = decode(context, uri)
            val buffer = ByteArrayOutputStream()
            check(image.compress(Bitmap.CompressFormat.JPEG, 88, buffer))
            image to buffer.toByteArray()
        }
        check(jpeg.size <= MAX_BYTES)
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
        avatar(uid).putBytes(jpeg, metadata).await()
        return bitmap
    }

    private fun avatar(uid: String) = emulator.reference.child("users/$uid/avatar.jpg")

    private fun decode(context: Context, uri: Uri): Bitmap {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= 28) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, uri)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > MAX_DIMENSION) {
                    val ratio = MAX_DIMENSION.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * ratio).roundToInt().coerceAtLeast(1),
                        (info.size.height * ratio).roundToInt().coerceAtLeast(1)
                    )
                }
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0)
        val longest = maxOf(bounds.outWidth, bounds.outHeight)
        var sample = 1
        while (longest / sample > MAX_DIMENSION) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return checkNotNull(resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) })
    }
}
