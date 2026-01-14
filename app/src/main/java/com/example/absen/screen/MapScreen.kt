@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.absen.screen

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding

@Composable
fun MapScreen(
    absenLat: Double,
    absenLng: Double,
    officeLat: Double,
    officeLng: Double,
    officeName: String,
    officeRadiusMeters: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        Configuration.getInstance().load(
            context.applicationContext,
            context.getSharedPreferences("osmdroid", 0)
        )
        Configuration.getInstance().userAgentValue = context.packageName

        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val obs = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            mapView.onDetach()
        }
    }

    LaunchedEffect(absenLat, absenLng, officeLat, officeLng, officeName, officeRadiusMeters) {
        val absenPoint = GeoPoint(absenLat, absenLng)
        val officePoint = GeoPoint(officeLat, officeLng)

        mapView.overlays.clear()

        // Marker: titik absen
        Marker(mapView).apply {
            position = absenPoint
            title = "Titik Absen"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(this)
        }

        // Marker: kantor
        Marker(mapView).apply {
            position = officePoint
            title = officeName
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            mapView.overlays.add(this)
        }

        // Polyline kantor -> absen (ringan, membantu orientasi)
        Polyline().apply {
            setPoints(listOf(officePoint, absenPoint))
            outlinePaint.color = Color.argb(220, 37, 99, 235)
            outlinePaint.strokeWidth = 6f
            mapView.overlays.add(this)
        }

        // Radius kantor (CIRCLE) - FIX: tampilkan radius
        Polygon().apply {
            points = Polygon.pointsAsCircle(officePoint, officeRadiusMeters.toDouble())

            // FIX WARNING: jangan pakai setFillColor/setStrokeColor/setStrokeWidth (deprecated)
            fillPaint.color = Color.argb(40, 37, 99, 235)
            outlinePaint.color = Color.argb(210, 37, 99, 235)
            outlinePaint.strokeWidth = 4f

            mapView.overlays.add(this)
        }

        // Center & zoom
        mapView.controller.setZoom(18.0)
        mapView.controller.setCenter(absenPoint)
        mapView.invalidate()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            }
        }
    ) { pad ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(pad)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView }
            )
        }
    }
}
