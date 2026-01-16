package com.example.b_manager.model

import com.google.gson.annotations.SerializedName

data class Mobil(
    @SerializedName("id_mobil")
    val idMobil: Int,

    @SerializedName("plat_nomor")
    val platNomor: String,

    @SerializedName("merek")
    val merek: String,

    @SerializedName("id_pelanggan")
    val idPelanggan: Int,

    @SerializedName("nama_pelanggan")
    val namaPelanggan: String? = null,

    @SerializedName("id_admin")
    val idAdmin: Int,

    @SerializedName("created_at")
    val createdAt: String
)

// Request model untuk Create/Update
data class MobilRequest(
    @SerializedName("plat_nomor")
    val platNomor: String,

    @SerializedName("merek")
    val merek: String,

    @SerializedName("id_pelanggan")
    val idPelanggan: Int,

    @SerializedName("id_admin")
    val idAdmin: Int
)