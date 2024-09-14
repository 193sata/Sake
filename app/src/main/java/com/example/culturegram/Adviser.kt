package com.example.culturegram

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale

class Adviser {
    //var text: String = "どらくえふうのきょじがあらわれた！\nきょじのこうしん！"

    //text = this.makeText
    @Composable
    fun Content() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Set the background color to black to match Dragon Quest style
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.spanyan1t),
                contentDescription = "Spanyan Image",
                modifier = Modifier.fillMaxSize(),  // Adjust the size as needed
                contentScale = ContentScale.Crop    // Adjust content scaling as needed
            )

            // Speech bubble (Dragon Quest style)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Position at the bottom center
                    .padding(16.dp) // Add some padding
                    .fillMaxWidth(0.9f) // Make the box a bit narrower than the screen
                    .clip(RectangleShape) // Dragon Quest style usually has rectangular borders
                    .border(2.dp, Color.White) // White border to match the style
                    .background(Color.Black) // Black background
                    .padding(16.dp) // Padding inside the bubble
            ) {
                Text(
                    text = makeText(), // Sample Japanese text
                    color = Color.White, // Text color
                    fontSize = 18.sp, // Text size to match retro style
                    lineHeight = 22.sp // Adjust line height for better spacing
                )
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

        return "${maxIndex}ばかり飲んでるにゃ〜" // Return the generated text as a String
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
}
