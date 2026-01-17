package com.example.b_manager.utils

import android.util.Patterns

object ValidationHelper {

    /**
     * SINKRON DENGAN PHP: Validasi Nama (Hanya huruf, spasi, dan titik)
     */
    fun isValidNama(nama: String): Boolean {
        return nama.isNotBlank() && nama.matches(Regex("^[a-zA-Z .]+$")) && nama.length in 3..100
    }

    /**
     * Validasi Nomor HP (10-15 digit, diawali 0)
     * PERBAIKAN: Sekarang support format +62
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "").trim()

        // Support berbagai format
        return when {
            // Format +628xxx (10-15 digit setelah +62)
            cleanPhone.startsWith("+62") -> {
                val digits = cleanPhone.substring(3)
                digits.matches(Regex("^8[0-9]{8,13}$"))
            }
            // Format 628xxx
            cleanPhone.startsWith("62") -> {
                val digits = cleanPhone.substring(2)
                digits.matches(Regex("^8[0-9]{8,13}$"))
            }
            // Format 08xxx (format lokal)
            cleanPhone.startsWith("0") -> {
                cleanPhone.matches(Regex("^0[0-9]{9,14}$"))
            }
            else -> false
        }
    }

    /**
     * BARU: Normalisasi nomor HP untuk dikirim ke API
     * Converts +62/62/08 -> 08
     */
    fun normalizePhoneNumber(phone: String): String {
        var cleanPhone = phone.replace(Regex("[^0-9+]"), "").trim()

        // Remove + sign
        cleanPhone = cleanPhone.replace("+", "")

        // Convert 62xxx -> 0xxx
        if (cleanPhone.startsWith("62")) {
            cleanPhone = "0" + cleanPhone.substring(2)
        }

        return cleanPhone
    }

    /**
     * BARU: Format nomor HP untuk WhatsApp (628xxx)
     */
    fun formatWhatsAppNumber(phone: String): String {
        var cleanPhone = phone.replace(Regex("[^0-9]"), "").trim()
        
        if (cleanPhone.startsWith("08")) {
            cleanPhone = "628" + cleanPhone.substring(2)
        } else if (cleanPhone.startsWith("62")) {
             // Already correct
        } else if (phone.startsWith("+")) {
             cleanPhone = phone.replace("+", "").replace(Regex("[^0-9]"), "")
        }
        
        return cleanPhone
    }

    /**
     * SINKRON DENGAN PHP: Validasi Plat Nomor Indonesia
     */
    fun isValidPlatNomor(plat: String): Boolean {
        val upperPlat = plat.uppercase().trim()
        // Support with or without spaces (e.g., AD 1234 ABC or AD1234ABC)
        return upperPlat.matches(Regex("^[A-Z]{1,2}\\s?[0-9]{1,4}\\s?[A-Z]{1,3}$"))
    }

    /**
     * SINKRON DENGAN PHP: Validasi Merek (Huruf, angka, spasi, dan dash)
     */
    fun isValidMerek(merek: String): Boolean {
        return merek.isNotBlank() && merek.matches(Regex("^[a-zA-Z0-9 -]+$")) && merek.length in 2..50
    }

    /**
     * SINKRON DENGAN PHP: Validasi Username (Huruf, angka, dan underscore)
     */
    fun isValidUsername(username: String): Boolean {
        return username.isNotBlank() && username.matches(Regex("^[a-zA-Z0-9_]+$")) && username.length in 3..50
    }

    /**
     * SINKRON DENGAN PHP: Validasi Kode Registrasi (SA + 6 digit)
     */
    fun isValidKodeRegistrasi(kode: String): Boolean {
        return kode.uppercase().matches(Regex("^SA[0-9]{6}$"))
    }

    /**
     * SINKRON DENGAN PHP: Validasi Deskripsi (Minimal 5 karakter)
     */
    fun isValidDeskripsi(deskripsi: String): Boolean {
        return deskripsi.trim().length >= 5
    }

    /**
     * SINKRON DENGAN PHP: Validasi Password (Minimal 5 karakter)
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 5
    }

    /**
     * Validasi Biaya minimal Rp 10.000
     */
    fun isValidBiaya(biaya: String): Boolean {
        val amount = biaya.toDoubleOrNull()
        return amount != null && amount >= 10000
    }

    // --- Helper Formatting ---

    fun formatRupiah(amount: Double): String {
        return "Rp ${String.format("%,.0f", amount).replace(",", ".")}"
    }

    fun formatDate(dateString: String): String {
        return try {
            val parts = dateString.split(" ")[0].split("-")
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2]

            val monthName = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
                "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
            )[month - 1]

            "$day $monthName $year"
        } catch (e: Exception) {
            dateString
        }
    }
}