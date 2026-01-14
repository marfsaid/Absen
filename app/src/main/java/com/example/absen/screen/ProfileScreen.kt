package com.example.absen.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.absen.ui.components.AppScaffold
import com.example.absen.ui.components.SecondaryButton
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    val u = FirebaseAuth.getInstance().currentUser

    AppScaffold(title = "Profile", onBack = onBack) { pad ->
        Column(
            modifier = Modifier.padding(pad).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(u?.displayName ?: "-", style = MaterialTheme.typography.titleMedium)
                    Text(u?.email ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(u?.uid ?: "-", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SecondaryButton(text = "Ganti akun", onClick = onSwitchAccount)
        }
    }
}
