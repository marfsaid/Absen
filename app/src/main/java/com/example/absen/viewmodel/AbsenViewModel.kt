package com.example.absen.viewmodel

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absen.data.FirestoreRepository
import com.example.absen.model.AbsenRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class HistoryUiState(
    val loading: Boolean = false,
    val items: List<AbsenRecord> = emptyList(),
    val error: String? = null
)

class AbsenViewModel(
    private val repo: FirestoreRepository = FirestoreRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _submitError = MutableStateFlow<String?>(null)
    val submitError: StateFlow<String?> = _submitError

    private val _userHistory = MutableStateFlow(HistoryUiState())
    val userHistory: StateFlow<HistoryUiState> = _userHistory

    private val _adminHistory = MutableStateFlow(HistoryUiState())
    val adminHistory: StateFlow<HistoryUiState> = _adminHistory

    fun clearSubmitError() {
        _submitError.value = null
    }

    fun loadRiwayatUser(limit: Long = 50) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _userHistory.value = HistoryUiState(error = "Belum login")
            return
        }

        _userHistory.value = HistoryUiState(loading = true)
        viewModelScope.launch {
            try {
                val list = repo.loadRiwayatUserSuspendCompat(uid, limit)
                _userHistory.value = HistoryUiState(items = list)
            } catch (e: Exception) {
                Log.e("AbsenViewModel", "loadRiwayatUser failed", e)
                _userHistory.value = HistoryUiState(error = e.message ?: "Gagal memuat riwayat")
            }
        }
    }

    fun loadRiwayatAdmin(limit: Long = 150) {
        _adminHistory.value = HistoryUiState(loading = true)
        viewModelScope.launch {
            try {
                val list = repo.loadRiwayatAdminSuspendCompat(limit)
                _adminHistory.value = HistoryUiState(items = list)
            } catch (e: Exception) {
                Log.e("AbsenViewModel", "loadRiwayatAdmin failed", e)
                _adminHistory.value = HistoryUiState(error = e.message ?: "Gagal memuat riwayat admin")
            }
        }
    }

    fun absenWithRadius(
        type: String,
        location: Location,
        officeId: String,
        officeName: String,
        officeLat: Double,
        officeLng: Double,
        officeRadiusMeters: Int,
        onDone: (Boolean, String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            onDone(false, "Belum login")
            return
        }

        val isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }

        _submitError.value = null
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val distance = haversineMeter(location.latitude, location.longitude, officeLat, officeLng)
                val inRadius = distance <= officeRadiusMeters.toDouble()

                repo.submitAbsenSuspend(
                    type = type,
                    location = GeoPoint(location.latitude, location.longitude),
                    officeId = officeId,
                    officeName = officeName,
                    officeLat = officeLat,
                    officeLng = officeLng,
                    officeRadiusM = officeRadiusMeters.toDouble(),
                    distanceMeter = distance,
                    inRadius = inRadius,
                    accuracyM = location.accuracy.toDouble(),
                    isMock = isMock
                )

                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    onDone(true, null)
                }
            } catch (e: Exception) {
                Log.e("AbsenViewModel", "absenWithRadius failed", e)
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    val msg = e.message ?: "Gagal absen"
                    _submitError.value = msg
                    onDone(false, msg)
                }
            }
        }
    }

    private fun haversineMeter(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
