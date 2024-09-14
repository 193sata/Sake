package com.example.culturegram

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import org.jetbrains.annotations.ApiStatus.NonExtendable
import java.io.File

class Adviser {

    @Composable
    fun Content() {
        // 表示するテキストを一文字ずつ描画するためのステート
        val chatGptAccess = ChatGptApiIO()
        var displayedText by remember { mutableStateOf("") }
        var fullText by remember { mutableStateOf("") } // ここに返答を返す

        val syuzoDataStr: String = loadOrCreateCsv()

        if (syuzoDataStr == "None") {
            fullText = "僕が君の旅のお供を務めるスパにゃんだにゃん。よろしくにゃん〜"
        }
        else {
            //syuzoListの中身をStringに変換
            // 非同期でChatGPTのレスポンスを取得し、状態を更新
            LaunchedEffect(Unit) {
                //val dSumList =  listOf(1,0,5,3,0,1,2,2,1)
                chatGptAccess.getAdviserResponse(syuzoDataStr) { responseText ->
                    // ChatGPTの返答を受け取ったときに`fullText`を更新
                    println(responseText)
                    fullText = responseText // 動的に変更
                }
            }
        }





        // テキストを一文字ずつ表示する
        LaunchedEffect(fullText) {
            displayedText = ""
            for (i in fullText.indices) {
                displayedText += fullText[i]
                delay(60) // 各文字の表示間隔（60ミリ秒ごとに次の文字を表示）
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White) // Set the background color to white
        ) {
            // Image section (70% of the screen height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(7f) // Takes up 70% of the space
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sakespanyan2),
                    contentDescription = "Spanyan Image",
                    modifier = Modifier.fillMaxSize(), // Adjust the size as needed
                    contentScale = ContentScale.Crop    // Adjust content scaling as needed
                )
            }

            // Text section (30% of the screen height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(3f) // Takes up 30% of the space
                    .padding(16.dp) // Add some padding
                    .clip(RoundedCornerShape(16.dp)) // Use RoundedCornerShape for rounded corners
                    .border(3.dp, Color.Black, RoundedCornerShape(16.dp)) // Black border with rounded corners
                    .background(Color.White) // White background
                    .padding(24.dp) // Increase padding inside the bubble for more text space
            ) {
                AnimatedText(displayedText)
            }
        }
    }

    // AnimatedText関数は変更なし
    @Composable
    fun AnimatedText(text: String) {
        // テキストのフェードイン用のアニメーション
        var isVisible by remember { mutableStateOf(false) }
        val alpha: Float by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = tween(durationMillis = 1000) // フェードインの持続時間
        )

        // フェードインアニメーションを開始
        LaunchedEffect(text) {
            isVisible = true
        }

        Text(
            text = text,
            color = Color.Black,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            modifier = Modifier.alpha(alpha) // アルファ値を設定してフェードイン
        )
    }

    fun makePrompt() {

    }

    // CSVを読み込むか、なければ作成する関数
    private fun loadOrCreateCsv(): String {
        val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo.csv"
        val csvFile = File(filePath)

        // ファイルが存在しない場合、"None"を返す
        if (!csvFile.exists()) {
            return "None"
        }

        // CSVファイルを読み込み、SyuzoMiniのリストに変換
        val syuzoList = mutableListOf<SyuzoMini>()
        csvFile.forEachLine { line ->
            val parts = line.split(",")
            if (parts.size >= 7) {
                val name = parts[1]
                val representativeSake = parts[2]
                val visited = parts[5].trim().toIntOrNull() == 1
                val evaluation = parts[6].trim().toIntOrNull() ?: 3

                syuzoList.add(
                    SyuzoMini(name, representativeSake, visited, evaluation)
                )
            }
        }

        // リストの全ての要素を一つの文字列に整形して結合
        val syuzoDataStr = syuzoList.joinToString(separator = "\n") { syuzo ->
            // 各要素をフォーマット
            val visitedStr = if (syuzo.visited) "Visited" else "Not Visited"
            "Name: ${syuzo.name}, Sake: ${syuzo.representativeSake}, Status: $visitedStr, Evaluation: ${syuzo.evaluation}"
        }

        return syuzoDataStr
    }
}

// Syuzoデータクラス
data class SyuzoMini(
    val name: String,
    val representativeSake: String,
    var visited: Boolean,
    var evaluation: Int,
)