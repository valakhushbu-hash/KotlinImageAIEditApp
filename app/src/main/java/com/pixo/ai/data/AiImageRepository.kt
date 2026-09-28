package com.pixo.ai.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.pixo.ai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface AiImageRepository {
    suspend fun edit(bitmap: Bitmap, prompt: String): Bitmap
}

/**
 * Direct Android -> OpenAI implementation.
 *
 * IMPORTANT: this is intended for testing/personal use. An API key shipped in an APK
 * can ultimately be extracted. A production app should put OpenAI behind a backend.
 */
class OpenAiImageRepository @Inject constructor() : AiImageRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(180, TimeUnit.SECONDS)
        .callTimeout(240, TimeUnit.SECONDS)
        .build()

    override suspend fun edit(bitmap: Bitmap, prompt: String): Bitmap = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.OPENAI_API_KEY.trim()
        require(apiKey.isNotEmpty()) {
            "OpenAI API key is missing. Add OPENAI_API_KEY to local.properties and sync Gradle."
        }

        val imageBytes = bitmapToPng(bitmap)
        val imageBody = imageBytes.toRequestBody("image/png".toMediaType())

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", "gpt-image-2.5-sunburst")
            .addFormDataPart("prompt", prompt)
            .addFormDataPart("size", "auto")
            .addFormDataPart("quality", "medium")
            .addFormDataPart("output_format", "png")
            .addFormDataPart("image", "pixo_input.png", imageBody)
            .build()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/images/edits")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .post(multipart)
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException(parseOpenAiError(body, response.code))
            }

            val json = JSONObject(body)
            val data = json.optJSONArray("data")
                ?: throw IllegalStateException("OpenAI returned no image data.")
            if (data.length() == 0) {
                throw IllegalStateException("OpenAI returned an empty image result.")
            }

            val b64 = data.getJSONObject(0).optString("b64_json")
            if (b64.isBlank()) {
                throw IllegalStateException("OpenAI returned no image bytes.")
            }

            val bytes = android.util.Base64.decode(b64, android.util.Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: throw IllegalStateException("The edited image could not be decoded.")
        }
    }

    private fun bitmapToPng(bitmap: Bitmap): ByteArray {
        // PNG is lossless and is accepted by the Images edit endpoint.
        // Cap extremely large phone photos to keep memory/request size reasonable.
        val normalized = scaleDown(bitmap, maxDimension = 2048)
        val stream = ByteArrayOutputStream()
        normalized.compress(Bitmap.CompressFormat.PNG, 100, stream)
        if (normalized !== bitmap) normalized.recycle()
        return stream.toByteArray()
    }

    private fun scaleDown(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val maxSide = maxOf(bitmap.width, bitmap.height)
        if (maxSide <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxSide.toFloat()
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    private fun parseOpenAiError(body: String, code: Int): String {
        return try {
            val json = JSONObject(body)
            val error = json.optJSONObject("error")
            val message = error?.optString("message").orEmpty()
            if (message.isNotBlank()) "OpenAI error ($code): $message"
            else "OpenAI request failed ($code)."
        } catch (_: Exception) {
            "OpenAI request failed ($code). ${body.take(300)}"
        }
    }
}
