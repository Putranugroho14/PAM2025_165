package com.example.b_manager.repository


import com.example.b_manager.model.Servis
import com.example.b_manager.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.b_manager.model.ApiResponse
import retrofit2.Response

class ServisRepository {

    private val apiService = RetrofitClient.apiService

    // Create Servis - Mengembalikan Result<Servis> agar cocok dengan ViewModel
    suspend fun createServis(
        idMobil: Int,
        idAdmin: Int,
        deskripsi: String,
        biaya: Double,
        status: String,
        tanggalServis: String
    ): Result<Servis> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createServis(idMobil, idAdmin, deskripsi, biaya, status, tanggalServis)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    val resultData = body.data ?: Servis(
                        idServis = 0,
                        idMobil = idMobil,
                        idAdmin = idAdmin,
                        tanggalServis = tanggalServis,
                        deskripsi = deskripsi,
                        biaya = biaya,
                        status = status,
                        createdAt = ""
                    )
                    Result.success(resultData)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mencatat servis"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServis(): Result<List<Servis>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getServis()
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mengambil data"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServisById(id: Int): Result<Servis> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getServisById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Data tidak ditemukan"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE: Diubah ke Result<Servis> untuk menghilangkan "Assignment type mismatch"
    suspend fun updateServis(
        idServis: Int,
        deskripsi: String,
        biaya: Double,
        status: String,
        tanggalServis: String
    ): Result<Servis> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateServis(idServis, deskripsi, biaya, status, tanggalServis)

            if (response.isSuccessful) {
                val body = response.body()
                // Sekarang body.data sudah ada!
                if (body?.status == "success") {
                    val resultData = body.data ?: Servis(
                        idServis = idServis,
                        idMobil = 0, // dummy
                        idAdmin = 0, // dummy
                        tanggalServis = tanggalServis,
                        deskripsi = deskripsi,
                        biaya = biaya,
                        status = status,
                        createdAt = ""
                    )
                    Result.success(resultData)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mengupdate data"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteServis(idServis: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteServis(idServis)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal menghapus data"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchServis(keyword: String): Result<List<Servis>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchServis(keyword)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Pencarian gagal"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun filterServis(
        nama: String?,
        status: String?,
        tanggalDari: String?,
        tanggalSampai: String?
    ): Result<List<Servis>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.filterServis(
                nama,
                status,
                tanggalDari,
                tanggalSampai
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Filter gagal"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseError(response: Response<*>): String {
        return try {
            val errorJson = response.errorBody()?.string()
            val apiResponse = Gson().fromJson(errorJson, ApiResponse::class.java)
            apiResponse.message ?: "HTTP Error: ${response.code()}"
        } catch (e: Exception) {
            "HTTP Error: ${response.code()}"
        }
    }
}