package com.example.absen.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absen.data.FirestoreRepository
import com.example.absen.logging.AnalyticsLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class LoginViewModel(
    private val logger: AnalyticsLogger? = null
) : ViewModel() {

    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    private val auth = FirebaseAuth.getInstance()
    private val repo = FirestoreRepository()

    fun logout() {
        auth.signOut()
        logger?.event("logout")
    }

    fun loginAdminSso(
        usernameOrEmail: String,
        password: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val input = usernameOrEmail.trim()
        val pass = password.trim()

        if (input.isEmpty() || pass.isEmpty()) {
            val msg = "Input dan password wajib diisi"
            errorMessage.value = msg
            logger?.event("login_fail", mapOf("method" to "admin_sso", "reason" to "empty_input"))
            onResult(false, msg)
            return
        }

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val email = withContext(Dispatchers.IO) {
                    if (input.contains("@")) input.lowercase()
                    else repo.getAdminEmailByUsernameSuspend(input)
                }

                val u = withContext(Dispatchers.IO) {
                    val res = auth.signInWithEmailAndPassword(email, pass).await()
                    res.user ?: error("Login gagal (user null)")
                }

                withContext(Dispatchers.IO) {
                    runCatching { repo.ensureUserDocSuspend(u.uid, u.email, u.displayName) }
                }

                val role = withContext(Dispatchers.IO) {
                    runCatching { repo.getUserRoleSuspend(u.uid) }.getOrDefault("user")
                }

                if (role.trim().lowercase() != "admin") {
                    auth.signOut()
                    val msg = "Akun ini bukan admin"
                    errorMessage.value = msg
                    isLoading.value = false
                    logger?.event("login_fail", mapOf("method" to "admin_sso", "reason" to "not_admin"))
                    onResult(false, msg)
                    return@launch
                }

                isLoading.value = false
                logger?.event("login_success", mapOf("method" to "admin_sso"))
                onResult(true, "OK")
            } catch (e: FirebaseAuthInvalidUserException) {
                val msg = "Akun belum dibuat di Firebase Auth (Email/Password)"
                errorMessage.value = msg
                isLoading.value = false
                logger?.event("login_fail", mapOf("method" to "admin_sso", "reason" to "invalid_user"))
                logger?.recordError(e, mapOf("scope" to "login_admin_sso"))
                onResult(false, msg)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                val msg = "Password salah"
                errorMessage.value = msg
                isLoading.value = false
                logger?.event("login_fail", mapOf("method" to "admin_sso", "reason" to "invalid_credentials"))
                onResult(false, msg)
            } catch (e: Throwable) {
                val msg = e.message ?: "Gagal login"
                errorMessage.value = msg
                isLoading.value = false
                logger?.event("login_fail", mapOf("method" to "admin_sso", "reason" to (msg.take(80))))
                logger?.recordError(e, mapOf("scope" to "login_admin_sso"))
                onResult(false, msg)
            }
        }
    }
}
