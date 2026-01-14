package com.example.absen

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.absen.work.ReminderWorker
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit

class AbsenApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // OSMDroid init sekali
        Configuration.getInstance().load(
            applicationContext,
            applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        // Notif channel

        // Reminder periodic (tanpa offline mode)
        scheduleReminders()
    }

    private fun scheduleReminders() {
        val wm = WorkManager.getInstance(this)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val masuk = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_MASUK))
            .build()

        val pulang = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInputData(workDataOf(ReminderWorker.KEY_KIND to ReminderWorker.KIND_PULANG))
            .build()

        wm.enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_MASUK,
            ExistingPeriodicWorkPolicy.KEEP,
            masuk
        )
        wm.enqueueUniquePeriodicWork(
            ReminderWorker.UNIQUE_PULANG,
            ExistingPeriodicWorkPolicy.KEEP,
            pulang
        )
    }
}
