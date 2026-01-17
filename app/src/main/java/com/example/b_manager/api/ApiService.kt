package com.example.b_manager.api

import com.example.b_manager.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTHENTICATION ====================

    @POST("auth/register.php")
    @FormUrlEncoded
    suspend fun register(
        @Field("kode_registrasi") kodeRegistrasi: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("nama_lengkap") namaLengkap: String
    ): Response<ApiResponse<Admin>>

    @POST("auth/login.php")
    @FormUrlEncoded
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ApiResponse<Admin>>

    @POST("auth/reset_password.php")
    @FormUrlEncoded
    suspend fun resetPassword(
        @Field("username") username: String,
        @Field("kode_registrasi") kodeRegistrasi: String,
        @Field("new_password") newPassword: String
    ): Response<ApiResponse<Any>>

    // ==================== PELANGGAN ====================

    @POST("pelanggan/create.php")
    @FormUrlEncoded
    suspend fun createPelanggan(
        @Field("nama_pelanggan") namaPelanggan: String,
        @Field("no_hp") noHp: String,
        @Field("alamat") alamat: String?,
        @Field("id_admin") idAdmin: Int
    ): Response<ApiResponse<Pelanggan>>

    @GET("pelanggan/read.php")
    suspend fun getPelanggan(): Response<ListResponse<Pelanggan>>

    @GET("pelanggan/read_single.php")
    suspend fun getPelangganById(
        @Query("id") idPelanggan: Int
    ): Response<ApiResponse<Pelanggan>>

    @POST("pelanggan/update.php")
    @FormUrlEncoded
    suspend fun updatePelanggan(
        @Field("id_pelanggan") idPelanggan: Int,
        @Field("nama_pelanggan") namaPelanggan: String,
        @Field("no_hp") noHp: String,
        @Field("alamat") alamat: String?
    ): Response<ApiResponse<Pelanggan>>

    @POST("pelanggan/delete.php")
    @FormUrlEncoded
    suspend fun deletePelanggan(
        @Field("id_pelanggan") idPelanggan: Int
    ): Response<ApiResponse<Any>>

    @GET("pelanggan/search.php")
    suspend fun searchPelanggan(
        @Query("keyword") keyword: String
    ): Response<ListResponse<Pelanggan>>

    // ==================== MOBIL ====================

    @POST("mobil/create.php")
    @FormUrlEncoded
    suspend fun createMobil(
        @Field("plat_nomor") platNomor: String,
        @Field("merek") merek: String,
        @Field("id_pelanggan") idPelanggan: Int,
        @Field("id_admin") idAdmin: Int
    ): Response<ApiResponse<Mobil>>

    @GET("mobil/read.php")
    suspend fun getMobil(): Response<ListResponse<Mobil>>

    @GET("mobil/read_single.php")
    suspend fun getMobilById(
        @Query("id") idMobil: Int
    ): Response<ApiResponse<Mobil>>

    @POST("mobil/update.php")
    @FormUrlEncoded
    suspend fun updateMobil(
        @Field("id_mobil") idMobil: Int,
        @Field("plat_nomor") platNomor: String,
        @Field("merek") merek: String,
        @Field("id_pelanggan") idPelanggan: Int
    ): Response<ApiResponse<Mobil>>

    @POST("mobil/delete.php")
    @FormUrlEncoded
    suspend fun deleteMobil(
        @Field("id_mobil") idMobil: Int
    ): Response<ApiResponse<Any>>

    @GET("mobil/search.php")
    suspend fun searchMobil(
        @Query("keyword") keyword: String
    ): Response<ListResponse<Mobil>>

    // ==================== SERVIS ====================

    @POST("servis/create.php")
    @FormUrlEncoded
    suspend fun createServis(
        @Field("id_mobil") idMobil: Int,
        @Field("id_admin") idAdmin: Int,
        @Field("deskripsi") deskripsi: String,
        @Field("biaya") biaya: Double,
        @Field("status") status: String,
        @Field("tanggal_servis") tanggalServis: String
    ): Response<ApiResponse<Servis>>

    @GET("servis/read.php")
    suspend fun getServis(): Response<ListResponse<Servis>>

    @GET("servis/read_single.php")
    suspend fun getServisById(
        @Query("id") idServis: Int
    ): Response<ApiResponse<Servis>>

    @POST("servis/update.php")
    @FormUrlEncoded
    suspend fun updateServis(
        @Field("id_servis") idServis: Int,
        @Field("deskripsi") deskripsi: String,
        @Field("biaya") biaya: Double,
        @Field("status") status: String,
        @Field("tanggal_servis") tanggalServis: String
    ): Response<ApiResponse<Servis>>

    @POST("servis/delete.php")
    @FormUrlEncoded
    suspend fun deleteServis(
        @Field("id_servis") idServis: Int
    ): Response<ApiResponse<Any>>

    @GET("servis/search.php")
    suspend fun searchServis(
        @Query("keyword") keyword: String
    ): Response<ListResponse<Servis>>

    @GET("servis/filter.php")
    suspend fun filterServis(
        @Query("nama") nama: String?,
        @Query("status") status: String?,
        @Query("tanggal_dari") tanggalDari: String?,
        @Query("tanggal_sampai") tanggalSampai: String?
    ): Response<ListResponse<Servis>>

}