package com.example.culturegram

/*class SakeBrewery (
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val yet: Int,
)*/

class SakeBrewery(
    val ID: Int,
    val breweryName: String,
    val sakeName: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Double,
    val yet: Boolean,
    var evaluation: Int,
//    val attributes: List<Int> // Store attributes as a list of integers
)
