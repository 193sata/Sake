package com.example.culturegram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.culturegram.ui.theme.CultureGramTheme
import java.io.File
// test_sata
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CultureGramTheme {
                val navController = rememberNavController()
                var selectedImage by remember { mutableStateOf<String?>(null) }

                val chatGptAccess = ChatGptApiIO()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("main") {
                            MainScreen().Content(navController)
                        }
                        composable(
                            route = "camera/{heritageName}",
                            arguments = listOf(navArgument("heritageName") { defaultValue = "Unknown" })
                        ) { backStackEntry ->
                            val heritageName = backStackEntry.arguments?.getString("heritageName")
                            CameraScreen().Content(heritageName ?: "Unknown", navController)
                        }
                        composable("status") {
                            Status().Content(navController)
                        }
                        /*composable("adviser") {
                            val adviser = Adviser()
                            adviser.Content()
                        }*/
                        composable(
                            "edit/{heritageName}",
                            arguments = listOf(navArgument("heritageName") { defaultValue = "Unknown" })
                        ) { backStackEntry ->
                            val heritageName = backStackEntry.arguments?.getString("heritageName") ?: ""
                            val imageList = getImageListForHeritage(heritageName)
                            EditScreen().Content(
                                navController = navController,
                                heritageName = heritageName,
                                imageList = imageList
                            ) { selectedImageFromEdit ->
                                selectedImage = selectedImageFromEdit
                                saveImagePath(applicationContext, heritageName, selectedImageFromEdit)
                            }
                        }
                        composable("main?screen={screen}", arguments = listOf(navArgument("screen") { nullable = true })) { backStackEntry ->
                            val startScreen = backStackEntry.arguments?.getString("screen")
                            val mainScreen = MainScreen()
                            mainScreen.Content(navController, startScreen)  // MainScreenに画面状態を渡す
                        }
                    }
                }
            }
        }
    }

    private fun getImageListForHeritage(heritageName: String): List<String> {
        val basePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures"
        val imageDir = File(basePath)
        val imageFiles = imageDir.listFiles { file -> file.name.startsWith(heritageName) && file.name.endsWith(".jpg") }

        return if (imageFiles != null && imageFiles.isNotEmpty()) {
            imageFiles.map { it.absolutePath }
        } else {
            listOf("$basePath/no_image.jpg")
        }
    }
}