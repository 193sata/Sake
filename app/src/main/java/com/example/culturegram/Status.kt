package com.example.culturegram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavHostController
import java.io.File
import java.io.IOException

class Status {

    @Composable
    fun Content(navController: NavHostController) {
        val context = LocalContext.current
        AchievementPage(navController, context)
    }

    @Composable
    fun AchievementPage(navController: NavHostController, context: Context) {
        val sakeBrewList = loadOrCreateCsv(context)  // CSVを読み込むか、存在しなければ作成
        updateAchievementStatus(sakeBrewList, context)

        val visitedCount = sakeBrewList.count { it.visited }
        val totalCount = sakeBrewList.size
        val achievementRatio = if (totalCount > 0) visitedCount.toFloat() / totalCount else 0f

        var selectedSakeBrew by remember { mutableStateOf<SakeBrew?>(null) }

        if (selectedSakeBrew == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // 達成度のドーナツ型グラフ
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(150.dp)
                ) {
                    DonutChart(achievementRatio, visitedCount, totalCount)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 画像一覧
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sakeBrewList.size) { index ->
                        val sakeBrew = sakeBrewList[index]
                        val imagePath = getSavedImagePath(context, sakeBrew.name)
                            ?: "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures/${sakeBrew.name}-0.jpg"
                        val imageFile = File(imagePath)

                        Box(
                            modifier = Modifier
                                .border(0.5.dp, Color.Black)
                                .aspectRatio(1f)
                                .clickable {
                                    selectedSakeBrew = sakeBrew
                                }
                        ) {
                            if (imageFile.exists()) {
                                val bitmap = loadRotatedBitmap(imageFile)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = sakeBrew.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.no_image),
                                    contentDescription = "No Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        } else {
            EnlargedImageScreen(
                heritage = selectedSakeBrew!!,
                navController = navController,
                onBackClick = { selectedSakeBrew = null },
                onVisitStatusChanged = { newVisitedStatus ->
                    selectedSakeBrew!!.visited = newVisitedStatus
                    saveCsv(context, sakeBrewList)  // 訪問ステータスをCSVに保存
                }
            )
        }
    }

    @Composable
    fun DonutChart(percentage: Float, visitedCount: Int, totalCount: Int) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(120.dp)
        ) {
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(
                    color = Color.LightGray,
                    style = Stroke(width = 15f)
                )
                drawArc(
                    color = Color.Green,
                    startAngle = -90f,
                    sweepAngle = 360 * percentage,
                    useCenter = false,
                    style = Stroke(width = 15f, cap = StrokeCap.Round)
                )
            }
            Text(
                text = "$visitedCount / $totalCount",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EnlargedImageScreen(
        heritage: SakeBrew,
        navController: NavHostController,
        onBackClick: () -> Unit,
        onVisitStatusChanged: (Boolean) -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = heritage.name, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    }
                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // 酒造の情報を表示
                    Text(text = "代表酒: ${heritage.representativeSake}")
                    Text(text = "緯度: ${heritage.latitude}")
                    Text(text = "経度: ${heritage.longitude}")

                    // 訪問ステータスを編集できるチェックボックス
                    Row(modifier = Modifier.padding(16.dp)) {
                        Text(text = "訪れたか: ")
                        Checkbox(
                            checked = heritage.visited,
                            onCheckedChange = { isChecked ->
                                onVisitStatusChanged(isChecked)
                            }
                        )
                    }
                }
            }
        )
    }

    // EXIFデータに基づいて画像を回転させる関数
    private fun loadRotatedBitmap(imageFile: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imageFile.path)
        var rotatedBitmap = bitmap

        try {
            val exif = androidx.exifinterface.media.ExifInterface(imageFile.path)
            val orientation = exif.getAttributeInt(androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION, androidx.exifinterface.media.ExifInterface.ORIENTATION_NORMAL)

            rotatedBitmap = when (orientation) {
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rotatedBitmap
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // CSVを読み込むか、存在しない場合は作成する関数
    private fun loadOrCreateCsv(context: Context): List<SakeBrew> {
        val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo0.csv"
        val csvFile = File(filePath)

        // ファイルが存在しない場合、作成する
        if (!csvFile.exists()) {
            createCsv(context, csvFile)
        }

        val sakeBrewList = mutableListOf<SakeBrew>()
        if (csvFile.exists()) {
            csvFile.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 6) {
                    val id = parts[0].toIntOrNull() ?: 0
                    val name = parts[1]
                    val representativeSake = parts[2]
                    val latitude = parts[3].toDoubleOrNull() ?: 0.0
                    val longitude = parts[4].toDoubleOrNull() ?: 0.0
                    val visited = parts[5].toIntOrNull() == 1
                    sakeBrewList.add(SakeBrew(id, name, representativeSake, latitude, longitude, visited))
                }
            }
        }

        return sakeBrewList
    }

    // CSVファイルを保存する関数
    private fun saveCsv(context: Context, sakeBrewList: List<SakeBrew>) {
        val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo0.csv"
        val csvFile = File(filePath)
        csvFile.bufferedWriter().use { writer ->
            sakeBrewList.forEach { sakeBrew ->
                writer.write("${sakeBrew.id},${sakeBrew.name},${sakeBrew.representativeSake},${sakeBrew.latitude},${sakeBrew.longitude},${if (sakeBrew.visited) 1 else 0}\n")
           }
        }
    }
    // CSVファイルが存在しない場合に作成する関数
    private fun createCsv(context: Context, file: File) {
        file.parentFile?.mkdirs()
        file.writeText(
            """
        1,瑞鷹（株）　川尻本蔵,瑞鷹,32.73743225,130.6823003,0,None
        2,瑞鷹（株）　東肥蔵,瑞鷹,32.74092219,130.6813227,0,None
        """.trimIndent()
        )
    }

    // 訪問した酒造かどうかのステータスを更新
    private fun updateAchievementStatus(sakeBrewList: List<SakeBrew>, context: Context) {
        sakeBrewList.forEach { sakeBrew ->
            val imagePath = getSavedImagePath(context, sakeBrew.name)
            sakeBrew.visited = imagePath != null && File(imagePath).exists()
        }
    }

    // 保存された画像パスを取得する関数
    private fun getSavedImagePath(context: Context, sakeBrewName: String): String? {
        val basePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures"
        val imageFile = File("$basePath/$sakeBrewName-0.jpg")
        return if (imageFile.exists()) imageFile.absolutePath else null
    }
}

// 酒造のデータクラス
data class SakeBrew(
    val id: Int,
    val name: String,
    val representativeSake: String,
    val latitude: Double,
    val longitude: Double,
    var visited: Boolean
)