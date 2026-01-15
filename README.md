<div align="center">

# ABSEN APP
### Attendance App — Jetpack Compose • Firebase Auth • Firestore • Location Radius

<p align="center"> <a href="https://firebase.google.com/" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/firebase/firebase-icon.svg" alt="firebase" width="55" height="55"/> </a> <a href="https://kotlinlang.org" target="_blank" rel="noreferrer"> <img src="https://www.vectorlogo.zone/logos/kotlinlang/kotlinlang-icon.svg" alt="kotlin" width="55" height="55"/> </a> <img src="https://www.vectorlogo.zone/logos/android/android-icon.svg" width ="55" height ="55"> </p>


Aplikasi absensi Android berbasis lokasi. User absen masuk/keluar dengan validasi radius kantor, admin memantau riwayat seluruh user.

</div>

---

## Ringkasan
Absen (MyAbsen) adalah aplikasi absensi yang memverifikasi lokasi user terhadap titik kantor (radius). Data absensi disimpan di Cloud Firestore, autentikasi memakai Firebase Auth. UI dibangun dengan Jetpack Compose (Material 3) dan navigasi memakai Navigation Compose.

---

## Fitur

### User
- Login (Google/One Tap sesuai konfigurasi project)
- Absen masuk/keluar berbasis lokasi (GPS)
- Validasi radius kantor (jarak user ke kantor)
- Riwayat absensi user
- Profil & Logout

### Admin
- Dashboard admin
- Riwayat absensi semua user
- Profil & Logout

---

## Tech Stack
- Kotlin
- Jetpack Compose (Material 3)
- Navigation Compose
- Firebase Authentication
- Cloud Firestore
- Google Play Services Location
- OSMDroid (Map)
  



---
# Setup

```bash
git clone https://github.com/<USERNAME>/<REPO>.git
cd <REPO>
```

###  Firebase

- Buka Firebase Console

- Buat Project Firebase

- Tambahkan Android App

- applicationId: com.example.absen

- Download google-services.json

- Letakkan file ke:
```
app/google-services.json
```

###  Aktifkan Layanan Firebase

- Authentication (Google Sign-In jika dipakai)

- Cloud Firestore

###  Konfigurasi Lokasi Kantor

Edit OfficeConfig.kt:

- OFFICE_LAT

- OFFICE_LNG

- OFFICE_RADIUS_M

- DEFAULT_OFFICE_NAME

###  Sync dan Run

- Android Studio → Sync Gradle

- Build → Clean Project

- Build → Rebuild Project

- Run → pilih device/emulator



