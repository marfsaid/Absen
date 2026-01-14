package com.example.absen.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.abs

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        const val KEY_KIND = "kind"
        const val KIND_MASUK = "masuk"
        const val KIND_PULANG = "pulang"

        const val UNIQUE_MASUK = "reminder_absen_masuk"
        const val UNIQUE_PULANG = "reminder_absen_pulang"

        private const val HOUR_MASUK = 8
        private const val MIN_MASUK = 30
        private const val HOUR_PULANG = 17
        private const val MIN_PULANG = 0
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Kalau belum login, tidak spam notif
        if (FirebaseAuth.getInstance().currentUser == null) return@withContext Result.success()

        val kind = inputData.getString(KEY_KIND) ?: KIND_MASUK

        // Guard waktu supaya periodic drift tidak bikin notif jam sembarang
        val now = Calendar.getInstance()
        val currentMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        val targetMin = if (kind == KIND_PULANG) {
            HOUR_PULANG * 60 + MIN_PULANG
        } else {
            HOUR_MASUK * 60 + MIN_MASUK
        }

        if (abs(currentMin - targetMin) > 20) return@withContext Result.success()

        val (title, text, id) = if (kind == KIND_PULANG) {
            Triple("Pengingat Absen Pulang", "Lakukan absen pulang.", 8102)
        } else {
            Triple("Pengingat Absen Masuk", "Lakukan absen masuk.", 8101)
        }

        Result.success()
    }
}
