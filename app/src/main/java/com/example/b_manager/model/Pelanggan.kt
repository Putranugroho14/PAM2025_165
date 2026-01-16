package com.example.b_manager.model

import com.google.gson.annotations.SerializedName

data class Pelanggan(
    @SerializedName("id_pelanggan")
    val idPelanggan: Int,

    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,

    @SerializedName("no_hp")
    val noHp: String,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("id_admin")
    val idAdmin: Int,

    @SerializedName("created_at")
    val createdAt: String
)

// Request model untuk Create/Update
data class PelangganRequest(
    @SerializedName("nama_pelanggan")
    val namaPelanggan: String,

    @SerializedName("no_hp")
    val noHp: String,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("id_admin")
    val idAdmin: Int
)