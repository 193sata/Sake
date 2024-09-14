package com.example.culturegram

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController

class MainScreen {

    @Composable
    fun Content(navController: NavHostController, startScreen: String? = null) {
        // 背景色を動的に変更するための状態
        val backgroundColor = remember { mutableStateOf(Color.White) }

        // メインコンテンツを動的に変更するための変数
        val contentState = remember {
            mutableStateOf<@Composable () -> Unit>({
                if (startScreen == "status") {
                    Status().Content(navController)
                } else {
                    Map().Content(navController)
                }
            })
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.value)  // 動的に背景色を設定
        ) {
            val iconColor = if (backgroundColor.value == Color.White) Color.Black else Color.White

            // メインコンテンツ
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                contentState.value()  // 動的に変更されるコンテンツを表示
            }

            // フッター
            Footer(navController, iconColor) { button ->
                when (button) {
                    "map" -> {
                        contentState.value = { Map().Content(navController) }
                        backgroundColor.value = Color.White
                    }
                    "shorts" -> {
                        contentState.value = { Shorts().Content() }
                        backgroundColor.value = Color.Black
                    }
                    "status" -> {
                        contentState.value = { Status().Content(navController) }
                        backgroundColor.value = Color.White
                    }
                }
            }
        }
    }

    @Composable
    fun Footer(navController: NavController, iconColor: Color, onButtonClick: (String) -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { onButtonClick("map") }) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Map Icon",
                    tint = iconColor
                )
            }

            IconButton(onClick = { onButtonClick("shorts") }) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Shorts Icon",
                    tint = iconColor
                )
            }

            IconButton(onClick = { onButtonClick("status") }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Status Icon",
                    tint = iconColor
                )
            }

            // 最後は消す
            IconButton(onClick = {
                navController.navigate("adviser")
            }) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Status Icon",
                    tint = iconColor
                )
            }
        }
    }
}