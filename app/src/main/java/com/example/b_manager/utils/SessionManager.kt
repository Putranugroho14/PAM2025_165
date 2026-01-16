package com.example.b_manager.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.b_manager.model.Admin

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = prefs.edit()

    // Simpan session login
    fun createLoginSession(admin: Admin) {
        editor.apply {
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
            putInt(Constants.KEY_ADMIN_ID, admin.idAdmin)
            putString(Constants.KEY_USERNAME, admin.username)
            putString(Constants.KEY_NAMA_LENGKAP, admin.namaLengkap)
            putString(Constants.KEY_KODE_REGISTRASI, admin.kodeRegistrasi)
            apply()
        }
    }

    // Cek apakah user sudah login
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false)
    }

    // Get Admin ID
    fun getAdminId(): Int {
        return prefs.getInt(Constants.KEY_ADMIN_ID, 0)
    }

    // Get Username
    fun getUsername(): String? {
        return prefs.getString(Constants.KEY_USERNAME, null)
    }

    // Get Nama Lengkap
    fun getNamaLengkap(): String? {
        return prefs.getString(Constants.KEY_NAMA_LENGKAP, null)
    }

    // Get Admin Data
    fun getAdminData(): Admin? {
        if (!isLoggedIn()) return null

        return Admin(
            idAdmin = getAdminId(),
            username = getUsername() ?: "",
            namaLengkap = getNamaLengkap() ?: "",
            kodeRegistrasi = prefs.getString(Constants.KEY_KODE_REGISTRASI, "") ?: "",
            createdAt = ""
        )
    }

    // Logout - hapus semua data session
    fun logout() {
        editor.clear()
        editor.apply()
    }
}