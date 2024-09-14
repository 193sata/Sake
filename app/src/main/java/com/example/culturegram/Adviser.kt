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
    val text: List<String> = List(9) { "生成されたテキスト${it + 1}" }

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
                    text = "どらくえふうのきょじがあらわれた！\nきょじのこうしん！", // Sample Japanese text
                    color = Color.White, // Text color
                    fontSize = 18.sp, // Text size to match retro style
                    lineHeight = 22.sp // Adjust line height for better spacing
                )
            }
        }
    }

    fun makeText() {

    }

    fun getAttr(): List<List<Int>> {
        return listOf(
            listOf(0, 0, 0, 1, 1, 0, 1, 1, 1),
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
