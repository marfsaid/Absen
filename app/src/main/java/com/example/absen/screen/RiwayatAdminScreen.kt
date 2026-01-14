package com.example.absen.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.absen.model.AbsenRecord
import com.example.absen.ui.components.AppScaffold
import com.example.absen.viewmodel.AbsenViewModel
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RiwayatAdminScreen(
    viewModel: AbsenViewModel,
    onBack: () -> Unit,
    onShowMap: (GeoPoint) -> Unit
) {
    val state by viewModel.adminHistory.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadRiwayatAdmin() }

    AppScaffold(title = "Riwayat Semua", onBack = onBack) { pad ->
        Box(
            modifier = Modifier.padding(pad).fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> Text(state.error ?: "Error", color = MaterialTheme.colorScheme.error)
                state.items.isEmpty() -> Text("Belum ada data", color = MaterialTheme.colorScheme.onSurfaceVariant)
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(items = state.items, key = { it.id }) { item ->
                            AdminHistoryCard(item = item, onShowMap = onShowMap)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHistoryCard(
    item: AbsenRecord,
    onShowMap: (GeoPoint) -> Unit
) {
    val timeText = remember(item.timestampMillis) { fmt(item.timestampMillis) }
    val lat = item.lat
    val lng = item.lng

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "${item.userName.ifBlank { item.email }} • ${item.type.uppercase()}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = item.officeName.ifBlank { "-" })

            if (lat != null && lng != null) {
                Spacer(Modifier.height(6.dp))
                OutlinedButton(onClick = { onShowMap(GeoPoint(lat, lng)) }) {
                    Text("Map")
                }
            }
        }
    }
}

private fun fmt(ts: Long): String {
    if (ts <= 0L) return "-"
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(ts))
}
