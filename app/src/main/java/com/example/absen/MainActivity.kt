package com.example.absen

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.absen.data.FirestoreRepository
import com.example.absen.navigation.AppNavGraph
import com.example.absen.ui.theme.AbsenTheme
import com.example.absen.viewmodel.AbsenViewModel
import com.example.absen.viewmodel.LoginViewModel
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestPostNotificationsIfNeeded()

        setContent {
            AbsenTheme {
                val loginVm: LoginViewModel = viewModel(factory = lambdaFactory { LoginViewModel() })
                val absenVm: AbsenViewModel = viewModel(factory = lambdaFactory { AbsenViewModel() })

                OneTapHost { launchOneTap ->
                    AppNavGraph(
                        loginViewModel = loginVm,
                        absenViewModel = absenVm,
                        onLaunchOneTap = launchOneTap
                    )
                }
            }
        }
    }

    private fun requestPostNotificationsIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val perm = android.Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        if (granted) return
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }.launch(perm)
    }
}

@Composable
private fun OneTapHost(content: @Composable (launchOneTap: () -> Unit) -> Unit) {
    val activity = androidx.compose.ui.platform.LocalContext.current as Activity
    val scope = rememberCoroutineScope()

    val oneTapClient = remember { Identity.getSignInClient(activity) }
    val repo = remember { FirestoreRepository() }

    val webClientId = remember { activity.getString(R.string.default_web_client_id) }

    val signInRequest = remember {
        BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        val data = result.data ?: return@rememberLauncherForActivityResult

        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential.googleIdToken
            if (idToken.isNullOrBlank()) {
                Toast.makeText(activity, "One Tap gagal: token kosong", Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance()
                .signInWithCredential(firebaseCred)
                .addOnSuccessListener { res ->
                    val u = res.user
                    if (u != null) {
                        scope.launch(Dispatchers.IO) {
                            runCatching { repo.ensureUserDocSuspend(u.uid, u.email, u.displayName) }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, e.message ?: "Firebase signIn gagal", Toast.LENGTH_LONG).show()
                }
        } catch (e: ApiException) {
            Toast.makeText(activity, e.message ?: "One Tap gagal", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(activity, e.message ?: "One Tap gagal", Toast.LENGTH_LONG).show()
        }
    }

    var launching by remember { mutableStateOf(false) }

    fun launchOneTap() {
        if (launching) return
        launching = true

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                launching = false
                val req = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(req)
            }
            .addOnFailureListener { e ->
                oneTapClient.signOut()
                    .addOnCompleteListener {
                        oneTapClient.beginSignIn(signInRequest)
                            .addOnSuccessListener { result2 ->
                                launching = false
                                val req = IntentSenderRequest.Builder(result2.pendingIntent.intentSender).build()
                                launcher.launch(req)
                            }
                            .addOnFailureListener { e2 ->
                                launching = false
                                Toast.makeText(
                                    activity,
                                    e2.message ?: e.message ?: "One Tap tidak tersedia di device ini",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
            }
    }

    content(::launchOneTap)
}

private inline fun <reified T : ViewModel> lambdaFactory(
    crossinline creator: () -> T
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator() as VM
    }
}
