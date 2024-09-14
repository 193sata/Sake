package com.example.culturegram

import android.util.Log
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.*//APIとの非同期通信に必要

class ChatGptApiIO {
    private val apiKey = BuildConfig.CHATGPT_API_KEY // ここにOpenAIのAPIキーを入力
    private val url = "https://api.openai.com/v1/chat/completions"
    private val client = OkHttpClient()// OkHttpClientを使用してネットワーク通信を行う

    /*
    //使い方
    val chatGptAccess = ChatGptApiIO()
    chatGptAccess.getResponse("Hello!") { responseText ->
        // ここでChatGPTの返答を受け取る
        println(responseText)
    }
    */

    public fun getResponse(prompt: String, callback: (String) -> Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.Main) // メインスレッドでコルーチンを起動
        coroutineScope.launch {
            // `getChatGptResponse` を非同期で呼び出し、その結果をcallbackに渡す
            val result = withContext(Dispatchers.IO) {
                getChatGptResponse(prompt)
            }
            callback(result) // 結果をcallbackに渡す
        }
    }

    public fun getAdviserResponse(sumList:List<Int>, callback: (String) -> Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.Main) // メインスレッドでコルーチンを起動
        var prompt = "次の数列は順番に[清酒，連続式蒸留焼酎，単式蒸留焼酎，ビール，果実酒，ウィスキー，スピリッツ，リキュール，その他]に分類されるお酒を購入できる製造所に立ち寄った回数を表しています．"
        prompt += sumList.toString() //回数のリストを渡す
        prompt += "以上をもとに，リストの基になった人物のお酒の好みを推定しなさい．ただし，20文字以内で説明し，語尾には「にゅん」をつけなさい．"

        coroutineScope.launch {
            // `getChatGptResponse` を非同期で呼び出し、その結果をcallbackに渡す
            val result = withContext(Dispatchers.IO) {
                getChatGptResponse(prompt)
            }
            callback(result) // 結果をcallbackに渡す
        }
    }

    //プロンプトを受け取って，返す
    private suspend fun getChatGptResponse(prompt: String): String {
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