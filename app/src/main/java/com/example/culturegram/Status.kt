package com.example.culturegram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
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
        val syuzoList = loadOrCreateCsv()
        updateAchievementStatus(syuzoList, context)

        val visitedCount = syuzoList.count { it.visited }
        val totalCount = syuzoList.size
        val achievementRatio = if (totalCount > 0) visitedCount.toFloat() / totalCount else 0f

        var selectedSyuzo by remember { mutableStateOf<Syuzo?>(null) }

        if (selectedSyuzo == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {

                // 達成度のドーナツ型グラフ
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
//                            .fillMaxWidth()
                        .padding(16.dp)
                        .height(250.dp)
                ) {
                    Row(){
                        DonutChart(achievementRatio, visitedCount, totalCount)
                        Adviser().Content()
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 画像一覧
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(syuzoList.size) { index ->
                        val syuzo = syuzoList[index]

                        val latitudeStr = createLatitudeStr(syuzo.latitude)
                        println("latitudeStr")
                        println(latitudeStr)
                        // 経度の処理
                        val longitudeStr = createLongitudeStr(syuzo.longitude)

                        val imagePath = getSavedImagePath(context, syuzo.name)
                            ?: "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures/${syuzo.name}-0.jpg"
                        val imageFile = File(imagePath)
                        // 緯度経度を文字列に変換し、小数点を除いて結合
                        val latLongStr = "${latitudeStr.replace(".", "")}${longitudeStr.replace(".", "")}"
                        val imageName = "syuzo_$latLongStr"
                        println("syuzoname")
                        println(syuzo.name)
                        println("緯度")
                        println(syuzo.latitude)
                        println("経度")
                        println(syuzo.longitude)
                        println("imageName")
                        println(imageName)
                        Box(
                            modifier = Modifier
                                .border(0.5.dp, Color.Black)
                                .aspectRatio(1f)
                                .clickable {
                                    selectedSyuzo = syuzo
                                }
                        ) {
                            if (imageFile.exists()) {
                                val bitmap = loadRotatedBitmap(imageFile)
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = syuzo.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val context = LocalContext.current
                                val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                                println("resId")
                                println(resId)
                                if (resId != 0) {
                                    // drawableに指定の名前の画像が存在する場合
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = imageName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // 指定の名前の画像が存在しない場合はNo Imageを表示
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
            }
        } else {
            EnlargedImageScreen(
                syuzo = selectedSyuzo!!,
                navController = navController,
                onBackClick = { selectedSyuzo = null }
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
        syuzo: Syuzo,
        navController: NavHostController,
        onBackClick: () -> Unit
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = syuzo.name, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("edit/${syuzo.name}")
                        }) {
                            Icon(imageVector = Icons.Filled.Edit, contentDescription = "編集")
                        }
                    }
                )
            },
            content = { padding ->
                val latitudeStr = createLatitudeStr(syuzo.latitude)
                val longitudeStr = createLongitudeStr(syuzo.longitude)
                // 緯度経度を文字列に変換し、小数点を除いて結合
                val latLongStr = "${latitudeStr.replace(".", "")}${longitudeStr.replace(".", "")}"
                val imageName = "syuzo_$latLongStr"

                // 保存された画像パスを取得
                val imagePath = getSavedImagePath(LocalContext.current, syuzo.name)
                    ?: "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures/${syuzo.name}-0.jpg"
                val imageFile = File(imagePath)
                println("imagename")
                println(imageName)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (imageFile.exists()) {
                        val bitmap = loadRotatedBitmap(imageFile)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = syuzo.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val context = LocalContext.current
                        val resId = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        println("resId")
                        println(resId)
                        if (resId != 0) {
                            // drawableに指定の名前の画像が存在する場合
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = imageName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // 指定の名前の画像が存在しない場合はNo Imageを表示
                            Image(
                                painter = painterResource(id = R.drawable.no_image),
                                contentDescription = "No Image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Text(
                        text = syuzo.representativeSake,  // 画像名
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)  // テキストの周りにパディングを追加
                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))  // 半透明の黒い背景と角丸
                            .padding(8.dp),  // 背景内のテキストに追加パディング
                        color = Color.White,  // 白い文字色
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }

    // EXIFデータに基づいて画像を回転させる関数
    private fun loadRotatedBitmap(imageFile: File): Bitmap {
        val bitmap = BitmapFactory.decodeFile(imageFile.path)
        var rotatedBitmap = bitmap

        try {
            val exif = ExifInterface(imageFile.path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

            rotatedBitmap = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                else -> bitmap
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return rotatedBitmap
    }

    // 画像を指定した角度で回転させる関数
    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = android.graphics.Matrix().apply {
            postRotate(degrees)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // CSVを読み込むか、なければ作成する関数
    private fun loadOrCreateCsv(): List<Syuzo> {
        val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo.csv"
        val csvFile = File(filePath)

        // ファイルが存在しない場合、作成する
        if (!csvFile.exists()) {
            createCsv(csvFile)
        }

        val syuzoList = mutableListOf<Syuzo>()
        if (csvFile.exists()) {
            csvFile.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size == 15) {  // ID, name, representativeSake, latitude, longitude, visited, clear, link, single, beer, fruit, wine, spirits, liquor, other
                    val id = parts[0].toIntOrNull() ?: 0
                    val name = parts[1]
                    val representativeSake = parts[2]
                    val latitude = parts[3].toDoubleOrNull() ?: 0.0
                    val longitude = parts[4].toDoubleOrNull() ?: 0.0
                    val visited = parts[5].toIntOrNull() == 1
                    val clear = parts[6].toIntOrNull() ?: 0
                    val link = parts[7].toIntOrNull() ?: 0
                    val single = parts[8].toIntOrNull() ?: 0
                    val beer = parts[9].toIntOrNull() ?: 0
                    val fruit = parts[10].toIntOrNull() ?: 0
                    val wine = parts[11].toIntOrNull() ?: 0
                    val spirits = parts[12].toIntOrNull() ?: 0
                    val liquor = parts[13].toIntOrNull() ?: 0
                    val other = parts[14].toIntOrNull() ?: 0

                    syuzoList.add(
                        Syuzo(id, name, representativeSake, latitude, longitude, visited, clear, link, single, beer, fruit, wine, spirits, liquor, other)
                    )
                }
            }
        }

        return syuzoList
    }

    // 達成度を更新する関数
    private fun updateAchievementStatus(syuzoList: List<Syuzo>, context: Context) {
        syuzoList.forEach { syuzo ->
//            val imagePath = getSavedImagePath(context, syuzo.name)
            val imagePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/Pictures/${syuzo.name}-0.jpg"
            if (File(imagePath).exists()) {  // 画像が存在する場合にのみカウント
                syuzo.visited = true
            } else {
                syuzo.visited = false  // 画像がない場合は未達成にする
            }
        }

        // CSVファイルに更新された visited 状態を書き込む
        saveCsv(syuzoList, context)
    }

    private fun saveCsv(syuzoList: List<Syuzo>, context: Context) {
        val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo.csv"
        val csvFile = File(filePath)

        try {
            // ディレクトリが存在しない場合、作成する
            val parentDir = csvFile.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()  // ディレクトリを作成
            }

            csvFile.bufferedWriter().use { writer ->
                syuzoList.forEach { syuzo ->
                    writer.write(
                        "${syuzo.id},${syuzo.name},${syuzo.representativeSake},${syuzo.latitude},${syuzo.longitude},${if (syuzo.visited) 1 else 0}," +
                                "${syuzo.clear},${syuzo.link},${syuzo.single},${syuzo.beer},${syuzo.fruit},${syuzo.wine},${syuzo.spirits},${syuzo.liquor},${syuzo.other}\n"
                    )
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()  // エラーログを表示
        }
    }

    // CSVファイルを作成する関数
    private fun createCsv(file: File) {
        file.parentFile?.mkdirs()
        file.writeText(
            """
        1,瑞鷹（株）川尻本蔵,瑞鷹,32.73743225,130.6823003,0,1,0,0,0,0,0,0,1,1
        2,瑞鷹（株）東肥蔵,東肥赤酒,32.74092219,130.6813227,0,0,1,1,0,0,0,1,0,1
        3,熊本ワインファーム（株）西里醸造所,菊鹿シャルドネ,33.07834405,130.7703336,0,0,0,0,0,1,0,0,0,0
        4,熊本クラフトビール（有）,熊本クラフトビール,32.84064767,130.7754777,0,0,0,0,1,0,0,0,0,0
        5,（株）ダイヤモンドブルーイング,Mulberry Beliner Wenisse,32.81263289,130.7692731,0,0,0,0,1,0,0,0,0,0
        6,サントリービール（株）九州熊本工場,ザ・プレミヤム・モルツ,32.7467578,130.7914407,0,0,0,0,1,0,0,0,1,0
        7,通潤酒造（株）,蟬,32.68511123,130.9878935,0,1,0,0,0,0,0,0,1,0
        8,（株）福田酒造,甘夏サングリア,32.2854224,130.5623211,0,0,0,0,1,1,0,0,0,0
        9,亀萬酒造（資）,亀萬,32.42791937,130.9968777,0,1,0,0,0,0,0,0,1,0
        10,繊月酒造（株）,繊月,32.34128749,130.6055628,0,0,0,1,0,0,0,0,1,0
        11,（資）壽福酒造場,武者返し,32.20648147,130.7655083,0,0,0,1,0,0,0,0,0,0
        12,深野酒造（株）,彩葉,32.23540363,130.7483955,0,0,0,1,0,0,0,0,1,0
        13,（株）福田農場,山河,32.20343637,130.7611627,0,0,0,1,0,0,0,0,1,0
        14,（資）大和一酒造元,温泉焼酎夢,32.21917863,130.7327364,0,0,0,1,0,0,0,0,1,0
        15,松本酒造場,萬縁,32.23303241,130.8938903,0,0,0,1,0,0,0,0,0,0
        16,松の泉酒造（資）,松の泉,32.24187256,130.9052102,0,0,0,1,0,0,0,0,1,0
        17,（資）高田酒造場,あさぎりの花,32.24546583,130.8758723,0,0,0,1,0,0,0,1,1,0
        18,（株）堤酒造,奥球磨櫻,32.23584619,130.9257499,0,0,0,1,0,0,0,1,1,0
        19,高橋酒造（株）,白岳しろ,32.25818578,130.6910527,0,0,0,1,0,0,0,1,1,0
        20,（有）那須酒造場,球磨の泉,32.24749793,130.94737,0,0,0,1,0,0,0,0,0,0
        21,木下醸造所球磨リキュール製造所,文蔵,32.26144048,130.9427931,0,0,0,1,0,0,0,0,0,0
        22,木下醸造所,梅酒文蔵,32.26144048,130.9427931,0,0,0,0,0,0,0,0,1,0
        23,房の露（株）,蔵八,32.26527412,130.9412771,0,0,0,1,0,0,0,0,0,0
        24,抜群酒造（資）,ばつぐん,32.27659958,130.9430283,0,0,0,1,0,0,0,0,0,0
        25,（名）豊永酒造,豊永蔵,32.28089257,130.9851592,0,0,0,1,0,0,0,1,1,0
        26,（有）林酒造所,極楽,32.28586697,130.9845676,0,0,0,1,0,0,0,0,0,0
        27,六調子酒造（株）,特吟六調子,32.19775304,130.8189891,0,0,0,1,0,0,0,0,0,0
        28,常楽酒造（株）,秋の露,32.19509828,130.8465653,0,0,0,1,0,0,1,1,1,0
        29,（有）大石酒造場,大石,32.29688453,130.9880785,0,0,0,1,0,0,0,0,1,0
        30,（有）松下醸造場,最古蔵,32.2964037,130.9866099,0,0,0,1,0,0,0,0,1,0
        31,企業組合青空中央企画,絆（きずな）,32.97729618,130.4570883,0,0,0,0,0,1,0,0,0,0
        32,花の香り酒造（株）,純米大吟醸花の香桜花,33.07499777,130.5927503,0,1,0,1,0,0,0,0,1,0
        33,（名）天草酒造,天草,32.32296759,130.1919306,0,0,0,1,0,0,0,0,1,0
        34,アマクサソナービール合同会社,ホワイトパール,32.54289799,130.1789321,0,0,0,0,1,0,0,0,0,0
        35,千代の園酒造（株）,大吟醸千代の園EXCEL,33.01123691,130.6876344,0,1,0,1,0,0,0,0,1,1
        36,熊本ワインファーム（株）菊鹿醸造場,菊鹿シャドルネアンウデット,32.85059192,130.6947742,0,0,0,0,0,1,0,0,0,0
        37,（株）美少年,美少年純米酒清夜,32.99694444,130.8720977,0,1,0,1,0,0,0,0,0,0
        38,アパラボ,アソピエス,32.97501398,131.0409505,0,0,0,0,0,0,0,0,1,0
        39,山村酒造（名）,れいざん,32.81738572,131.1251811,0,1,0,0,0,0,0,0,1,0
        40,河津酒造（株）,七歩蛇,33.12135288,131.0670264,0,1,0,1,0,0,0,0,1,0
        41,室原（名）,和田志ら露,33.09606039,131.0694134,0,1,0,0,0,0,0,0,0,0
        42,どぶろく加工所,風の杜,33.11380409,131.0594832,0,0,0,0,0,0,0,0,0,1
        """.trimIndent()
        )
    }

    private fun createLatitudeStr(latitude: Double): String {
        val latitudeStr: String  = latitude.toString()
        if (latitude.toString().contains(".")) {
            val decimalPart = latitude.toString().split(".")[1]
            // 小数点以下の桁数を確認し、3桁の場合にゼロを追加
            if (decimalPart.length == 3) {
                return latitudeStr + "0"
            }
            else {
                return latitudeStr
            }
        }


        return latitudeStr
    }

    private fun createLongitudeStr(longitude: Double): String {
        val longitudeStr: String = longitude.toString()
        if (longitude.toString().contains(".")) {
            val decimalPart = longitude.toString().split(".")[1]
            // 小数点以下の桁数を確認し、3桁の場合にゼロを追加
            if (decimalPart.length == 3) {
                return longitudeStr + "0"
            } else {
                return longitudeStr
            }
        }

        return longitudeStr
    }

}

// Syuzoデータクラス
data class Syuzo(
    val id: Int,
    val name: String,
    val representativeSake: String,
    val latitude: Double,
    val longitude: Double,
    var visited: Boolean,
    val clear: Int,   // 清
    val link: Int,    // 連
    val single: Int,  // 単
    val beer: Int,    // ビ
    val fruit: Int,   // 果
    val wine: Int,    // ウ
    val spirits: Int, // ス
    val liquor: Int,  // リ
    val other: Int    // 他
)