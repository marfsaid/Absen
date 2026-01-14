package com.example.absen.data

import android.util.Log
import com.example.absen.model.AbsenRecord
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val absensiCol = db.collection("absensi")
    private val usersCol = db.collection("users")
    private val adminSsoCol = db.collection("admin_sso")

    suspend fun ensureUserDocSuspend(uid: String, email: String?, name: String?) {
        val data = hashMapOf(
            "email" to (email ?: ""),
            "name" to (name ?: ""),
            "updatedAt" to Timestamp.now()
        )
        usersCol.document(uid).set(data, SetOptions.merge()).await()
    }

    suspend fun getUserRoleSuspend(uid: String): String {
        val doc = usersCol.document(uid).get().await()
        val role = doc.getString("role")?.trim().orEmpty()
        return if (role.isBlank()) "user" else role
    }

    suspend fun getAdminEmailByUsernameSuspend(username: String): String {
        val u = username.trim().lowercase()
        require(u.isNotEmpty()) { "Username kosong" }

        val doc = adminSsoCol.document(u).get().await()
        require(doc.exists()) { "Username admin tidak ditemukan" }

        return doc.getString("email")?.trim()?.lowercase()
            ?.takeUnless { it.isEmpty() }
            ?: error("Konfigurasi admin tidak valid (email kosong)")
    }

    suspend fun submitAbsenSuspend(
        type: String,
        location: GeoPoint,
        officeId: String,
        officeName: String,
        officeLat: Double,
        officeLng: Double,
        officeRadiusM: Double,
        distanceMeter: Double,
        inRadius: Boolean,
        accuracyM: Double,
        isMock: Boolean,
        userEmail: String? = null,
        userName: String? = null
    ) {
        val user = auth.currentUser ?: error("User belum login")

        val now = Timestamp.now()
        val nowMillis = now.toDate().time

        val data = hashMapOf(
            "userId" to user.uid,
            "uid" to user.uid,
            "email" to (userEmail ?: user.email ?: ""),
            "userEmail" to (userEmail ?: user.email ?: ""),
            "userName" to (userName ?: user.displayName ?: ""),
            "type" to type,

            "officeId" to officeId,
            "officeName" to officeName,
            "officeLat" to officeLat,
            "officeLng" to officeLng,
            "officeRadiusM" to officeRadiusM,

            "location" to location,
            "distanceMeter" to distanceMeter,
            "inRadius" to inRadius,
            "accuracyM" to accuracyM,
            "isMock" to isMock,

            "timestamp" to now,
            "timestampMillis" to nowMillis
        )

        absensiCol.add(data).await()
    }

    private fun docToRecord(d: DocumentSnapshot): AbsenRecord {
        val gp = d.getGeoPoint("location")
        val tsMillis =
            d.getLong("timestampMillis")
                ?: d.getTimestamp("timestamp")?.toDate()?.time
                ?: 0L

        return AbsenRecord(
            id = d.id,
            userId = d.getString("userId") ?: (d.getString("uid") ?: ""),
            email = d.getString("email") ?: d.getString("userEmail") ?: "",
            userName = d.getString("userName") ?: d.getString("name") ?: "",
            type = d.getString("type") ?: "masuk",
            officeId = d.getString("officeId") ?: "",
            officeName = d.getString("officeName") ?: "",
            lat = gp?.latitude,
            lng = gp?.longitude,
            distanceMeter = d.getDouble("distanceMeter"),
            inRadius = d.getBoolean("inRadius"),
            accuracyM = d.getDouble("accuracyM"),
            timestampMillis = tsMillis
        )
    }

    suspend fun loadRiwayatUserSuspendCompat(uid: String, limit: Long = 50): List<AbsenRecord> {
        try {
            val snap = absensiCol
                .whereEqualTo("userId", uid)
                .orderBy("timestampMillis", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            if (!snap.isEmpty) return snap.documents.map { docToRecord(it) }
        } catch (e: FirebaseFirestoreException) {
            if (e.code != FirebaseFirestoreException.Code.FAILED_PRECONDITION) throw e
        } catch (e: Exception) {
            Log.w("FirestoreRepository", "Primary query gagal", e)
        }

        val fields = listOf("userId", "uid", "userUid")
        val merged = linkedMapOf<String, AbsenRecord>()

        for (f in fields) {
            try {
                val snap = absensiCol
                    .whereEqualTo(f, uid)
                    .limit(limit * 5)
                    .get()
                    .await()
                for (d in snap.documents) merged[d.id] = docToRecord(d)
                if (merged.isNotEmpty()) break
            } catch (e: Exception) {
                Log.w("FirestoreRepository", "Fallback query gagal field=$f", e)
            }
        }

        return merged.values
            .sortedByDescending { it.timestampMillis }
            .take(limit.toInt())
    }

    suspend fun loadRiwayatAdminSuspendCompat(limit: Long = 150): List<AbsenRecord> {
        try {
            val snap = absensiCol
                .orderBy("timestampMillis", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()
            if (!snap.isEmpty) return snap.documents.map { docToRecord(it) }
        } catch (_: Exception) {
        }

        val snap2 = absensiCol
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        return snap2.documents.map { docToRecord(it) }
    }
}
