package com.example.absen.model

data class AbsenRecord(
    val id: String = "",
    val userId: String = "",
    val email: String = "",
    val userName: String = "",
    val type: String = "masuk",

    val officeId: String = "",
    val officeName: String = "",

    val lat: Double? = null,
    val lng: Double? = null,

    val distanceMeter: Double? = null,
    val inRadius: Boolean? = null,
    val accuracyM: Double? = null,

    val timestampMillis: Long = 0L
)

/**
 * Alias kompatibilitas: beberapa file/versi lama pernah memakai `userEmail`.
 */
val AbsenRecord.userEmail: String
    get() = email
