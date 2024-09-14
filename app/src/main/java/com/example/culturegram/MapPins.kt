package com.example.culturegram

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
class MapPins {
    private var sakeBreweries: MutableList<SakeBrewery> = mutableListOf()
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    // 地球の半径 (メートル)
    private val earthRadius = 6371000.0

    fun setUserPosition(tmpUserLatitude: Double, tmpUserLongitude: Double){
        userLatitude = tmpUserLatitude
        userLongitude = tmpUserLongitude
    }

    // 距離計算用関数 (ハーバサインの公式を使用)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }

    fun getSakeBreweries(): MutableList<SakeBrewery> {
        return sakeBreweries
    }

    fun getHeritagesInside(distanceLimit: Double): MutableList<SakeBrewery> {
        val result: MutableList<SakeBrewery> = mutableListOf()
        for (i in sakeBreweries) {
            if (i.distance <= distanceLimit) result.add(i)
        }
        return result
    }

    fun readCsvFile(context: Context) {
        try {
            // syuzo.csvファイルを読み込む
            val filePath = "/storage/emulated/0/Android/data/com.example.culturegram/files/csv/syuzo.csv"
            val csvFile = File(filePath)
            val reader = BufferedReader(FileReader(csvFile))
            val header = reader.readLine()
            val headerTokens = header.split(",")

            // Get the indices of each column in the CSV
            val idIndex = 0
            val breweryNameIndex = 1
            val sakeNameIndex = 2
            val latitudeIndex = 3
            val longitudeIndex = 4
            val visitedIndex = 5
            val evaluationIndex = 6

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(",")

                // Parse values from the tokens based on the indices
                val id = tokens.getOrNull(idIndex)?.toIntOrNull()
                val breweryName = tokens.getOrNull(breweryNameIndex)
                val sakeName = tokens.getOrNull(sakeNameIndex)
                val latitude = tokens.getOrNull(latitudeIndex)?.toDoubleOrNull()
                val longitude = tokens.getOrNull(longitudeIndex)?.toDoubleOrNull()
                val visited = tokens.getOrNull(visitedIndex)?.toIntOrNull() == 1  // 1の場合はtrue
                val evaluation = tokens.getOrNull(evaluationIndex)?.toIntOrNull() ?: 3  // 評価がない場合はデフォルト3

                // Ensure all values are not null before adding
                if (id != null && breweryName != null && sakeName != null && latitude != null && longitude != null) {
                    sakeBreweries.add(
                        SakeBrewery(
                            ID = id,
                            breweryName = breweryName,
                            sakeName = sakeName,
                            latitude = latitude,
                            longitude = longitude,
                            distance = calculateDistance(latitude, longitude, userLatitude, userLongitude),
                            yet = visited,
                            evaluation = evaluation
                        )
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}