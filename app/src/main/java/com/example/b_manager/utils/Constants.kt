package com.example.b_manager.utils

object Constants {
    // API Base URL
    const val BASE_URL = "http://10.0.2.2:8080/bengkel_api/"

    // SharedPreferences Keys
    const val PREF_NAME = "BengkelManagerPref"
    const val KEY_IS_LOGGED_IN = "isLoggedIn"
    const val KEY_ADMIN_ID = "adminId"
    const val KEY_USERNAME = "username"
    const val KEY_NAMA_LENGKAP = "namaLengkap"
    const val KEY_KODE_REGISTRASI = "kodeRegistrasi"

    // Request Codes
    const val REQUEST_ADD_PELANGGAN = 100
    const val REQUEST_EDIT_PELANGGAN = 101
    const val REQUEST_ADD_MOBIL = 200
    const val REQUEST_EDIT_MOBIL = 201
    const val REQUEST_ADD_SERVIS = 300
    const val REQUEST_EDIT_SERVIS = 301

    // Status Servis
    const val STATUS_PROSES = "proses"
    const val STATUS_SELESAI = "selesai"
    const val STATUS_BATAL = "batal"
}