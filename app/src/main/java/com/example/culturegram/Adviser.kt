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

class Adviser {
    @Composable
    fun Content() {
        // 表示するテキストを一文字ずつ描画するためのステート
        var displayedText by remember { mutableStateOf("") }
        val fullText = makeText() // すべてのテキストを取得

        // テキストを一文字ずつ表示する
        LaunchedEffect(fullText) {
            displayedText = ""
            for (i in fullText.indices) {
                displayedText += fullText[i]
                delay(60) // 各文字の表示間隔（150ミリ秒ごとに次の文字を表示）
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
                    painter = painterResource(id = R.drawable.sakespanyan),
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

                /*Text(
                    text = displayedText, // 一文字ずつ表示するテキスト
                    color = Color.Black, // Text color
                    fontSize = 20.sp, // Slightly larger text size
                    lineHeight = 26.sp // Adjust line height for better spacing
                )*/
            }
        }
    }

    // 生成されたテキストを作成
    fun makeText(): String {
        val attributes = getAttr() // getAttrを呼び出す
        val sumList = calculateSumAttr(attributes) // 各列の合計を計算
        val maxIndex = calculateMaxIndex(sumList) // 最大値のインデックスを取得
        println("各列の合計値: $sumList") // デバッグ用に出力
        println("最大値のインデックス: $maxIndex") // 最大値のインデックスを出力

        return "${maxIndex}ばかり飲んでるにゃ〜，あああああああああああああああ" // Return the generated text as a String
    }

    // 各列の合計値を計算
    fun calculateSumAttr(attributes: List<List<Int>>): List<Int> {
        val numColumns = attributes[0].size // 列の数
        val sums = MutableList(numColumns) { 0 } // 合計値を保持するリスト

        for (row in attributes) {
            for (i in 0 until numColumns) {
                sums[i] += row[i] // 各列の値を合計
            }
        }

        return sums
    }

    // 最大値のインデックスを取得
    fun calculateMaxIndex(sumList: List<Int>): Int {
        var maxIndex = 0
        var maxValue = sumList[0]

        for (i in sumList.indices) {
            if (sumList[i] > maxValue) {
                maxValue = sumList[i]
                maxIndex = i
            }
        }

        return maxIndex
    }

    // サンプルデータを取得
    fun getAttr(): List<List<Int>> {
        return listOf(
            listOf(0, 0, 0, 1, 0, 0, 0, 0, 0),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1)
        )
    }

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
}
