// app/src/main/java/com/example/absen/util/TimeUtil.kt
package com.example.absen.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtil {
    private val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    fun formatMillis(ms: Long): String = sdf.format(Date(ms))
}
