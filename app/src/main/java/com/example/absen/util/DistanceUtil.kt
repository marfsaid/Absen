package com.example.absen.util

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0

fun calculateDistanceMeter(
    lat1: Double, lng1: Double,
    lat2: Double, lng2: Double
): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) *
            cos(Math.toRadians(lat2)) *
            sin(dLng / 2).pow(2.0)

    return 2 * EARTH_RADIUS_M * atan2(sqrt(a), sqrt(1 - a))
}

fun formatDistance(distance: Double): String =
    if (distance < 1000) "${distance.toInt()} m"
    else String.format("%.1f km", distance / 1000.0)
