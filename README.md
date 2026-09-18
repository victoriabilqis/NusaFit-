# NusaFit Android

NusaFit adalah project Android native Kotlin untuk tracker olahraga.

## Fitur yang sudah disiapkan
- Profil: jenis kelamin, usia, tinggi, berat, BMI dan rentang berat berbasis BMI 18,5–24,9.
- Tracking GPS aktif sampai tombol STOP ditekan.
- Foreground location service agar tracking tetap berjalan saat layar mati/aplikasi di-background.
- Google Maps + titik lokasi terakhir.
- Jarak, durasi, estimasi kalori dan lemak.
- Nama aktivitas dan riwayat lokal di HP.
- Pemilihan foto/video yang dikaitkan dengan sesi.
- Pengingat jadwal menggunakan AlarmManager.
- Statistik 7/30/365 hari.
- Firebase Auth anonim + Firestore + Storage (opsional sampai konfigurasi Firebase ditambahkan).
- Dependensi Health Connect untuk integrasi data kesehatan/smartwatch pada tahap berikutnya.

## Konfigurasi wajib
1. Buka project di Android Studio.
2. Tambahkan `app/google-services.json` dari Firebase Console.
3. Aktifkan plugin `com.google.gms.google-services` (sudah aktif di project ini).
4. Buat Firestore dan Storage, lalu pasang Security Rules yang membatasi data per user.
5. Ganti `YOUR_GOOGLE_MAPS_API_KEY` pada `app/src/main/res/values/strings.xml` dengan API key Google Maps yang memiliki Maps SDK for Android.
6. Build & Run pada perangkat Android nyata.

## Catatan pengujian
Project ini belum dapat menghasilkan APK di lingkungan pembuatan ini karena Android SDK/Gradle build tool dan kredensial Google Maps/Firebase milik Anda tidak tersedia di lingkungan tersebut. Source project disusun untuk Android Studio dan membutuhkan konfigurasi layanan tersebut sebelum build produksi.

## Keamanan
Jangan memasukkan service-account key ke APK. Gunakan Firebase Authentication + Firestore/Storage Security Rules.

## GitHub Actions

Project ini sudah memiliki workflow CI di `.github/workflows/android.yml` untuk build debug APK dan `.github/workflows/release.yml` untuk build release APK secara manual. Lihat `GITHUB_ACTIONS_SETUP.md` untuk konfigurasi secrets Firebase dan Google Maps.
