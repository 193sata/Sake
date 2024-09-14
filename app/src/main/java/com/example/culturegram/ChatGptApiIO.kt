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
import org.json.JSONArray

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

    /*public fun getAdviserResponse(sumList:List<Int>, callback: (String) -> Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.Main) // メインスレッドでコルーチンを起動
        var prompt = "次の数列は順番に[清酒，連続式蒸留焼酎，単式蒸留焼酎，ビール，果実酒，ウィスキー，スピリッツ，リキュール，その他]に分類されるお酒を購入できる製造所に立ち寄った回数を表しています．"
        prompt += sumList.toString() //回数のリストを渡す
        prompt += "以上をもとに，リストの基になった人物のお酒の好みを推定しなさい．ただし，20文字以内で説明し，語尾には「にゃん」をつけなさい．"

        coroutineScope.launch {
            // `getChatGptResponse` を非同期で呼び出し、その結果をcallbackに渡す
            val result = withContext(Dispatchers.IO) {
                getChatGptResponse(prompt)
            }
            callback(result) // 結果をcallbackに渡す
        }
    }*/

    public fun getAdviserResponse(syuzoDataStr: String, callback: (String) -> Unit) {
        val coroutineScope = CoroutineScope(Dispatchers.Main) // メインスレッドでコルーチンを起動
        var prompt = "以下は酒蔵の名前，代表酒，評価，visitid(１が訪れた，0が訪れていない)を示した情報である．"
        prompt += "visitedが1（すでに訪れた場所，飲んだお酒）以下の情報をもとに，visitedが0(訪れていない，飲んでいない)の中からお勧めする酒蔵名と代表酒を挙げてください．"
        prompt += "ただし，40文字以内で説明し，語尾には絶対に「にゃんをつけなさい」．口調はラフな感じで．加えて，全ての項目でvisitedが0の場合は，その点について指摘してください"
        prompt += syuzoDataStr
        //prompt += "以上をもとに，リストの基になった人物のお酒の好みを推定しなさい．ただし，20文字以内で説明し，語尾には「にゃん」をつけなさい．"
        //print(prompt)
        coroutineScope.launch {
            // `getChatGptResponse` を非同期で呼び出し、その結果をcallbackに渡す
            val result = withContext(Dispatchers.IO) {
                getChatGptResponse(prompt)
            }
            callback(result) // 結果をcallbackに渡す
        }
    }
    //            "model": "gpt-3.5-turbo",
    //プロンプトを受け取って，返す
    /*private suspend fun getChatGptResponse(prompt: String): String {
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
                            println(content)
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
    }*/
    private suspend fun getChatGptResponse(prompt: String): String {
        // JSONリクエストボディをJSONObjectを使用して構築
        val jsonBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        // contentフィールドのみを出力
        //val content = jsonBody.getJSONArray("messages").getJSONObject(0).getString("content")
        //println(content)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = RequestBody.create(mediaType, jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBodyStr = response.body?.string()

                if (response.isSuccessful && responseBodyStr != null) {
                    try {
                        val jsonResponse = JSONObject(responseBodyStr)
                        val choices = jsonResponse.getJSONArray("choices")
                        val choice = choices.getJSONObject(0)
                        val message = choice.getJSONObject("message")
                        val content = message.getString("content")
                        println(content)
                        return@withContext content
                    } catch (e: JSONException) {
                        Log.e("JSONError", "JSON parsing error", e)
                        return@withContext "エラー: レスポンスの解析に失敗しました"
                    }
                } else {
                    // エラーレスポンスの詳細をログに出力
                    val errorMsg = responseBodyStr ?: "不明なエラー"
                    Log.e("APIError", "エラーコード: ${response.code}, メッセージ: $errorMsg")
                    return@withContext "エラー: ${response.code} - $errorMsg"
                }
            } catch (e: IOException) {
                Log.e("NetworkError", "ネットワークエラー", e)
                return@withContext "ネットワークエラー: ${e.message}"
            }
        }
    }
}