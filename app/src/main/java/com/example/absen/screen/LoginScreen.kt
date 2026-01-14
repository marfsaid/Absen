package com.example.absen.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.absen.ui.components.AppTextField
import com.example.absen.ui.components.PasswordField
import com.example.absen.ui.components.PrimaryButton
import com.example.absen.ui.components.SecondaryButton

@Composable
fun LoginScreen(
    onOneTapLoginClick: () -> Unit,
    onAdminLogin: (String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var user by rememberSaveable { mutableStateOf("") }
    var pass by rememberSaveable { mutableStateOf("") }

    val canLogin by remember(user, pass, isLoading) {
        derivedStateOf { user.isNotBlank() && pass.isNotBlank() && !isLoading }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Absen", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Login untuk lanjut",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                AppTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = "Username / Email",
                    keyboardType = KeyboardType.Email,
                    enabled = !isLoading
                )

                PasswordField(
                    value = pass,
                    onValueChange = { pass = it },
                    enabled = !isLoading
                )

                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(Modifier.height(2.dp))

                PrimaryButton(
                    text = if (isLoading) "Memproses..." else "Login",
                    enabled = canLogin,
                    onClick = { onAdminLogin(user.trim(), pass) }
                )

                SecondaryButton(
                    text = "One Tap Google",
                    enabled = !isLoading,
                    onClick = onOneTapLoginClick
                )
            }
        }
    }
}
