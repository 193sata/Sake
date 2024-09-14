package com.example.culturegram

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
class MapPins {
    //private var heritages: MutableList<SakeBrewery> = mutableListOf()
    private var SakeBreweries: MutableList<SakeBrewery> = mutableListOf()
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
        return SakeBreweries
    }

    fun getHeritagesInside(distanceLimit: Double): MutableList<SakeBrewery> {
        val result: MutableList<SakeBrewery> = mutableListOf()
        for (i in SakeBreweries) {
            if (i.distance <= distanceLimit) result.add(i)
        }
        return result
    }

    fun readCsvFile(context: Context) {
        try {
            val inputStream = context.assets.open("syuzo0.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            val header = reader.readLine()
            val headerTokens = header.split(",")

            // Get the indices of each column in the CSV
            val idIndex = headerTokens.indexOf("ID")
            val breweryNameIndex = headerTokens.indexOf("製造所名")
            val sakeNameIndex = headerTokens.indexOf("代表酒")
            val latitudeIndex = headerTokens.indexOf("緯度")
            val longitudeIndex = headerTokens.indexOf("経度")
            val yetIndex = headerTokens.indexOf("bool値")
            val attrIndex = headerTokens.indexOf("属性")

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(",")

                // Parse values from the tokens based on the indices
                val id = tokens.getOrNull(idIndex)?.toIntOrNull()
                val breweryName = tokens.getOrNull(breweryNameIndex)
                val sakeName = tokens.getOrNull(sakeNameIndex)
                val latitude = tokens.getOrNull(latitudeIndex)?.toDoubleOrNull()
                val longitude = tokens.getOrNull(longitudeIndex)?.toDoubleOrNull()
                val yet = tokens.getOrNull(yetIndex)?.toIntOrNull()
                val attr = tokens.getOrNull(attrIndex) // Treat 'attr' as a String

                // Ensure all values are not null before adding
                if (id != null && breweryName != null && sakeName != null && latitude != null && longitude != null && yet != null && attr != null) {
                    SakeBreweries.add(
                        SakeBrewery(
                            ID = id,
                            breweryName = breweryName,
                            sakeName = sakeName,
                            latitude = latitude,
                            longitude = longitude,
                            distance = calculateDistance(latitude, longitude, userLatitude, userLongitude),
                            yet = yet,
                            attr = attr // Directly assign the 'attr' as a String
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

