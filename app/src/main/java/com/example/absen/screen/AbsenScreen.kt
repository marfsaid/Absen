package com.example.absen.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.absen.ui.components.AppScaffold
import com.example.absen.ui.components.PrimaryButton
import com.example.absen.ui.components.SecondaryButton
import com.example.absen.util.OfficeConfig
import com.example.absen.util.calculateDistanceMeter
import com.example.absen.util.formatDistance
import com.example.absen.viewmodel.AbsenViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun AbsenScreen(
    viewModel: AbsenViewModel,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    var currentLocation by remember { mutableStateOf<Location?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showConfirm by remember { mutableStateOf(false) }

    var pendingAutoRetry by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (result[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        if (!granted) localError = "Izin lokasi ditolak."
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isGpsEnabled(): Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    fun openGpsSettings() {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }

    fun requestLocationPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun fetchLocation(openConfirm: Boolean) {
        localError = null
        currentLocation = null
        showConfirm = false
        pendingAutoRetry = false

        if (!hasLocationPermission()) {
            localError = "Izin lokasi belum diberikan."
            return
        }
        if (!isGpsEnabled()) {
            localError = "GPS belum aktif. Aktifkan lokasi terlebih dahulu."
            return
        }

        fusedClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    localError = "Lokasi belum tersedia. Coba lagi."
                } else {
                    currentLocation = loc
                    showConfirm = openConfirm

                    val acc = loc.accuracy.toDouble()
                    if (acc > 50.0) pendingAutoRetry = true
                }
            }
            .addOnFailureListener {
                localError = "Gagal mengambil lokasi."
            }
    }

    val isLoading by viewModel.isLoading.collectAsState()

    val accuracyM by remember(currentLocation) {
        derivedStateOf { currentLocation?.accuracy?.toDouble() }
    }

    val distanceToOfficeM by remember(currentLocation) {
        derivedStateOf {
            val loc = currentLocation ?: return@derivedStateOf null
            calculateDistanceMeter(
                loc.latitude,
                loc.longitude,
                OfficeConfig.OFFICE_LAT,
                OfficeConfig.OFFICE_LNG
            )
        }
    }

    val inRadius by remember(distanceToOfficeM) {
        derivedStateOf {
            val d = distanceToOfficeM ?: return@derivedStateOf false
            d <= OfficeConfig.OFFICE_RADIUS_M
        }
    }

    LaunchedEffect(pendingAutoRetry) {
        if (!pendingAutoRetry) return@LaunchedEffect
        delay(900)
        pendingAutoRetry = false
        fetchLocation(openConfirm = showConfirm)
    }

    AppScaffold(title = "Absen", onBack = onBack) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Status Lokasi", style = MaterialTheme.typography.titleMedium)

                    val loc = currentLocation
                    if (loc == null) {
                        Text(
                            "Belum ada lokasi.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            "Akurasi lokasi: ${String.format("%.0f", accuracyM ?: 0.0)} m",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Jarak ke kantor: ${formatDistance(distanceToOfficeM ?: 0.0)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            if (inRadius) "Status radius: di dalam radius" else "Status radius: di luar radius",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (inRadius) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!localError.isNullOrBlank()) {
                        Text(localError ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Memproses...", style = MaterialTheme.typography.bodyMedium)
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SecondaryButton(
                        text = "Refresh",
                        enabled = !isLoading,
                        onClick = { fetchLocation(openConfirm = false) }
                    )

                    PrimaryButton(
                        text = "Absen Sekarang",
                        enabled = !isLoading,
                        onClick = { fetchLocation(openConfirm = true) }
                    )

                    OutlinedButton(
                        onClick = {
                            if (!hasLocationPermission()) requestLocationPermission() else openGpsSettings()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (!hasLocationPermission()) "Minta Izin Lokasi" else "Buka Pengaturan GPS")
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Catatan", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Jika akurasi tinggi (angka meter besar), sistem akan coba ambil lokasi sekali lagi otomatis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (showConfirm && currentLocation != null) {
        val loc = currentLocation!!
        val acc = accuracyM ?: 0.0
        val dist = distanceToOfficeM ?: 0.0

        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Konfirmasi Absen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Akurasi lokasi: ${String.format("%.0f", acc)} m")
                    Text("Jarak ke kantor: ${formatDistance(dist)}")
                    Text("Lat: ${loc.latitude}")
                    Text("Lng: ${loc.longitude}")
                    Text(
                        if (inRadius) "Di dalam radius (${OfficeConfig.OFFICE_RADIUS_M} m)"
                        else "Di luar radius (${OfficeConfig.OFFICE_RADIUS_M} m)",
                        color = if (inRadius) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        showConfirm = false
                        viewModel.absenWithRadius(
                            type = "masuk",
                            location = loc,
                            officeId = OfficeConfig.DEFAULT_OFFICE_ID,
                            officeName = OfficeConfig.DEFAULT_OFFICE_NAME,
                            officeLat = OfficeConfig.OFFICE_LAT,
                            officeLng = OfficeConfig.OFFICE_LNG,
                            officeRadiusMeters = OfficeConfig.OFFICE_RADIUS_M.toInt(),
                            onDone = { ok, msg ->
                                if (!ok) localError = msg
                                viewModel.loadRiwayatUser()
                            }
                        )
                    }
                ) { Text("Ya") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Batal") }
            }
        )
    }
}
