package com.example.culturegram

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ChatGptApiIO {
    private val apiKey = BuildConfig.CHATGPT_API_KEY // ここにOpenAIのAPIキーを入力
    private val url = "https://api.openai.com/v1/chat/completions"
    private val client = OkHttpClient()// OkHttpClientを使用してネットワーク通信を行う

    //プロンプトを受け取って，返す
    suspend fun getChatGptResponse(prompt: String): String {
        // JSONリクエストボディ
        val jsonBody = """
        {
            "model": "gpt-3.5-turbo",
            "messages": [{"role": "user", "content": "$prompt"}]
        }
    """.trimIndent()

        val requestBody = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),  // 修正点: toMediaType() を使用
            jsonBody
        )

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val choices = jsonResponse.getJSONArray("choices")
                            val choice = choices.getJSONObject(0)
                            val message = choice.getJSONObject("message")
                            val content = message.getString("content")
                            return@withContext content
                        } catch (e: JSONException) {
                            Log.e("JSONError", "JSON parsing error", e)
                            return@withContext "エラー: レスポンスの解析に失敗しました"
                        }
                    } else {
                        return@withContext "エラー: レスポンスが空です"
                    }
                } else {
                    return@withContext "エラー: ${response.message}"
                }
            } catch (e: IOException) {
                Log.e("NetworkError", "ネットワークエラー", e)
                return@withContext "ネットワークエラー: ${e.message}"
            }
        }
    }
}