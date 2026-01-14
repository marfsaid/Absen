package com.example.absen.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.absen.ui.components.AppScaffold

@Composable
fun DashboardAdminScreen(
    onRiwayatSemuaClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    AppScaffold(title = "Admin") { pad ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = pad.calculateTopPadding() + 16.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Text("Admin Panel", style = MaterialTheme.typography.titleMedium)
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text("Riwayat Semua") },
                        supportingContent = { Text("Monitor absensi seluruh user") },
                        leadingContent = { Icon(Icons.Outlined.History, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { onRiwayatSemuaClick() }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Profil") },
                        supportingContent = { Text("Info akun") },
                        leadingContent = { Icon(Icons.Outlined.AccountCircle, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { onProfileClick() }
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Logout") },
                        supportingContent = { Text("Keluar dari akun") },
                        leadingContent = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth().clickable { onLogoutClick() }
                    )
                }
            }
        }
    }
}
