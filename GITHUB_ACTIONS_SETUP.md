# NusaFit — GitHub Actions build

Project ini sudah disiapkan untuk build APK menggunakan GitHub Actions.

## 1. Upload ke GitHub

Buat repository baru, lalu upload seluruh isi project ini ke branch `main` atau `master`.

## 2. Tambahkan repository secrets

GitHub → Settings → Secrets and variables → Actions → New repository secret.

Buat:

- `NUSAFIT_GOOGLE_SERVICES_JSON` — isi **seluruh isi file** `google-services.json` dari Firebase Android App NusaFit.
- `NUSAFIT_MAPS_API_KEY` — isi Google Maps API key.

Jangan commit `google-services.json` ke repository publik. Workflow membuat file tersebut hanya selama proses build.

## 3. Build debug APK

Setelah push ke `main`/`master`, workflow **NusaFit Android Build** akan:

1. Checkout source.
2. Menyiapkan JDK 17.
3. Menyiapkan Gradle 8.13.
4. Membuat `app/google-services.json` dari secret.
5. Memasukkan Maps API key ke resource build.
6. Menjalankan `assembleDebug`.
7. Mengunggah APK sebagai artifact **NusaFit-debug-apk**.

Workflow juga bisa dijalankan manual melalui **Actions → NusaFit Android Build → Run workflow**.

## 4. Release APK

Workflow **NusaFit Release APK** dapat dijalankan manual. Versi ini menghasilkan `assembleRelease` dan mengunggah artifact **NusaFit-release-apk**.

Catatan: release workflow saat ini menghasilkan release APK tanpa signing key. Untuk APK production yang bisa didistribusikan, tambahkan keystore/signing secrets dan konfigurasi signing sebelum dipakai sebagai release resmi.
