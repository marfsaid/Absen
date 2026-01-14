package com.example.absen.logging

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics

class AnalyticsLogger(
    private val analytics: FirebaseAnalytics,
    private val crashlytics: FirebaseCrashlytics = FirebaseCrashlytics.getInstance()
) {
    fun event(name: String, params: Map<String, Any?> = emptyMap()) {
        val b = Bundle()
        for ((k, v) in params) {
            when (v) {
                null -> Unit
                is String -> b.putString(k, v)
                is Int -> b.putInt(k, v)
                is Long -> b.putLong(k, v)
                is Double -> b.putDouble(k, v)
                is Float -> b.putFloat(k, v)
                is Boolean -> b.putBoolean(k, v)
                else -> b.putString(k, v.toString())
            }
        }
        analytics.logEvent(name, b)
    }

    fun breadcrumb(message: String) {
        crashlytics.log(message)
    }

    fun recordError(t: Throwable, context: Map<String, Any?> = emptyMap()) {
        for ((k, v) in context) crashlytics.setCustomKey(k, v?.toString() ?: "null")
        crashlytics.recordException(t)
    }
}
