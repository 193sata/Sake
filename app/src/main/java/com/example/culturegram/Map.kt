package com.example.culturegram

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.Circle
import java.io.IOException


class Map {
  var currentLocation by mutableStateOf(LatLng(32.81449335495487, 130.72729505562057)) // デフォルトの位置（熊本）
  private var sakeBreweries: MutableList<SakeBrewery> = mutableListOf()
  private var heritagesInside: MutableList<SakeBrewery> = mutableListOf()
  private var mapPins = MapPins()
  private var allowedR = 100000.0

  @Composable
  fun Content(navController: NavController) {
    val context = LocalContext.current
    val gps = remember { GPS(context) } // GPSクラスを記憶する
    var selectedHeritage by remember { mutableStateOf<SakeBrewery?>(null) } // 選択された遺産を保存する状態
    var isMapLoaded by remember { mutableStateOf(false) }  // マップがロードされたかを追跡するためのフラグ

    // GPSから位置情報を取得する（非同期）
    gps.GetCurrentLocation()

    // GPSの位置情報が更新されたらcurrentLocationを更新
    gps.currentLocation.value?.let { location ->
      currentLocation = LatLng(location.latitude, location.longitude)
    }

    mapPins.setUserPosition(currentLocation.latitude, currentLocation.longitude)
    mapPins.readCsvFile(context)

    try {
      sakeBreweries = mapPins.getSakeBreweries() // 全て入るように
      heritagesInside = mapPins.getHeritagesInside(allowedR)
    } catch (e: IOException) {
      e.printStackTrace()
    }

    // カメラポジションは変更しない（ズームレベルや位置をそのまま）
    val cameraPositionState = rememberCameraPositionState {
      position = CameraPosition.fromLatLngZoom(currentLocation, 7f) // 初期位置を設定
    }

    Box(modifier = Modifier.fillMaxSize()) {
      GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,  // カメラ位置をそのまま
        onMapLoaded = {
          isMapLoaded = true  // マップがロードされたらフラグを設定
        }
      ) {
        if (isMapLoaded) {
          // カスタムマーカー（青丸）を表示する
          Marker(
            state = MarkerState(position = LatLng(currentLocation.latitude, currentLocation.longitude)),
            icon = createCustomMarker(Color(0xFF4A90E2)),  // 淡い青色のカスタムマーカー
            title = "現在地",
            //snippet = "現在地",
            anchor = Offset(0.5f, 0.5f)  // アンカーをマーカーの中央に設定
          )

          // 現在地を中心に半径500mの円を描画
          Circle(
            center = LatLng(currentLocation.latitude, currentLocation.longitude), // Markerと同じ位置に円を描画
            radius = allowedR, // 半径500m
            strokeColor = Color(0x220000FF), // 円の輪郭の色
            fillColor = Color(0x220000FF), // 透明な青
            strokeWidth = 2f // 円の輪郭の太さ
          )

          // 遺産のピンを立てる
          sakeBreweries.forEach { sakeBrewery ->
            val isInside = heritagesInside.contains(sakeBrewery) // heritagesInsideに含まれているか確認

            Marker(
              state = MarkerState(position = LatLng(sakeBrewery.latitude, sakeBrewery.longitude)),
              title = sakeBrewery.breweryName, // デフォルトのタイトル（カスタムUIで表示するためここでの自動表示は抑制）
              icon = when {
                isInside -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)  // heritagesInsideに含まれている場合は黄色
                sakeBrewery.yet == 1 -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)  // 行った場所
                else -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)  // 行ってない場所
              },
              onClick = {
                // クリック時にカスタムUIを表示する
                if (isInside) {
                  selectedHeritage = sakeBrewery // カスタムUIで表示するための遺産を設定
                } else {
                  selectedHeritage = null // クリックされた遺産がisInsideでない場合は選択をリセット
                }
                false // カスタムの動作を実行し、デフォルトの動作は無効化
              }
            )
          }
        }
      }

      // カメラアイコンを表示する
      selectedHeritage?.let { sakeBrewry ->
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)  // 画面の中央下方に配置
            .padding(bottom = 30.dp)  // 画面下から少し上に配置
        ) {
          // 背景の丸いボックス（半透明）
          Box(
            modifier = Modifier
              .size(100.dp)  // 背景のサイズを設定
              .background(Color.Gray.copy(alpha = 0.5f), shape = CircleShape)  // 半透明の丸い背景を設定
          )

          // カメラアイコンのボタン（背景の上に配置）
          IconButton(
            onClick = {
              // カメラアイコンが押されたら"camera"画面へ遷移
              navController.navigate("camera/${sakeBrewry.breweryName}")
            },
            modifier = Modifier.size(100.dp)  // アイコンボタンのサイズ
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "カメラアイコン",
              tint = Color.White,  // アイコンの色を白に設定
              modifier = Modifier.size(50.dp)  // アイコン自体のサイズ
            )
          }
        }
      }
    }
  }

  // カスタムの青丸マーカーを作成する関数
  private fun createCustomMarker(color: Color): BitmapDescriptor {
    val size = 50 // マーカーの大きさ（固定）
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint()

    // 白い縁を描画
    paint.color = Color.White.toArgb() // 白色
    paint.isAntiAlias = true
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint) // 外側の白い円

    // 内側の青い円を描画
    paint.color = Color(0xFF007AFF).toArgb() // 濃い青色
    canvas.drawCircle(size / 2f, size / 2f, size / 2.5f, paint) // 内側の青い円

    return BitmapDescriptorFactory.fromBitmap(bitmap)
  }
}

