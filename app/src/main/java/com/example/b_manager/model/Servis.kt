package com.example.b_manager.model

import com.google.gson.annotations.SerializedName


data class Servis(
    @SerializedName("id_servis")
    val idServis: Int,

    @SerializedName("id_mobil")
    val idMobil: Int,

    @SerializedName("plat_nomor")
    val platNomor: String? = null,

    @SerializedName("merek")
    val merek: String? = null,

    @SerializedName("nama_pelanggan")
    val namaPelanggan: String? = null,

    @SerializedName("no_hp")
    val noHp: String? = null,

    @SerializedName("id_admin")
    val idAdmin: Int,

    @SerializedName("tanggal_servis")
    val tanggalServis: String,

    @SerializedName("deskripsi")
    val deskripsi: String,

    @SerializedName("biaya")
    val biaya: Double,

    @SerializedName("status")
    val status: String,

    @SerializedName("created_at")
    val createdAt: String
)

// Request model untuk Create/Update
data class ServisRequest(
    @SerializedName("id_mobil")
    val idMobil: Int,

    @SerializedName("id_admin")
    val idAdmin: Int,

    @SerializedName("deskripsi")
    val deskripsi: String,

    @SerializedName("biaya")
    val biaya: Double,

    @SerializedName("status")
    val status: String = "proses"
)