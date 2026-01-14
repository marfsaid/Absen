package com.example.absen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.absen.screen.MapScreen
import com.example.absen.ui.theme.AbsenTheme
import com.example.absen.util.OfficeConfig

class MapActivity : ComponentActivity() {

    companion object {
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LNG = "extra_lng"
        const val EXTRA_OFFICE_LAT = "extra_office_lat"
        const val EXTRA_OFFICE_LNG = "extra_office_lng"
        const val EXTRA_OFFICE_NAME = "extra_office_name"
        const val EXTRA_OFFICE_RADIUS = "extra_office_radius"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val lat = intent.getDoubleExtra(EXTRA_LAT, Double.NaN)
        val lng = intent.getDoubleExtra(EXTRA_LNG, Double.NaN)
        if (lat.isNaN() || lng.isNaN()) {
            finish()
            return
        }

        val officeLat = intent.getDoubleExtra(EXTRA_OFFICE_LAT, OfficeConfig.OFFICE_LAT)
        val officeLng = intent.getDoubleExtra(EXTRA_OFFICE_LNG, OfficeConfig.OFFICE_LNG)
        val officeName = intent.getStringExtra(EXTRA_OFFICE_NAME) ?: OfficeConfig.DEFAULT_OFFICE_NAME
        val officeRadius: Int = intent.extras?.let { b ->
            when {
                b.containsKey(EXTRA_OFFICE_RADIUS) -> {
                    runCatching { b.getInt(EXTRA_OFFICE_RADIUS) }.getOrNull()
                        ?: runCatching { b.getDouble(EXTRA_OFFICE_RADIUS).toInt() }.getOrNull()
                }
                else -> null
            }
        } ?: runCatching { OfficeConfig.OFFICE_RADIUS_M.toInt() }.getOrDefault(100)

        setContent {
            AbsenTheme {
                MapScreen(
                    absenLat = lat,
                    absenLng = lng,
                    officeLat = officeLat,
                    officeLng = officeLng,
                    officeName = officeName,
                    officeRadiusMeters = officeRadius,
                    onBack = { finish() }
                )
            }
        }
    }
}
