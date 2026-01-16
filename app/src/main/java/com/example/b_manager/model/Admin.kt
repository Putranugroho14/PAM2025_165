package com.example.b_manager.model

import com.google.gson.annotations.SerializedName

data class Admin(
    @SerializedName("id_admin")
    val idAdmin: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("nama_lengkap")
    val namaLengkap: String,

    @SerializedName("kode_registrasi")
    val kodeRegistrasi: String,

    @SerializedName("created_at")
    val createdAt: String
)

// Request model untuk Register
data class RegisterRequest(
    @SerializedName("kode_registrasi")
    val kodeRegistrasi: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("nama_lengkap")
    val namaLengkap: String
)

// Request model untuk Login
data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)