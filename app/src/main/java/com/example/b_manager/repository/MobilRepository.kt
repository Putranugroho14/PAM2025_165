package com.example.b_manager.repository

import com.example.b_manager.model.Mobil
import com.example.b_manager.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.b_manager.model.ApiResponse
import retrofit2.Response

class MobilRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun createMobil(
        platNomor: String,
        merek: String,
        idPelanggan: Int,
        idAdmin: Int
    ): Result<Mobil> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createMobil(platNomor, merek, idPelanggan, idAdmin)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    val resultData = body.data ?: Mobil(
                        idMobil = 0,
                        platNomor = platNomor,
                        merek = merek,
                        idPelanggan = idPelanggan,
                        idAdmin = idAdmin,
                        createdAt = ""
                    )
                    Result.success(resultData)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal menambahkan mobil"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMobil(): Result<List<Mobil>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMobil()

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

    suspend fun getMobilById(id: Int): Result<Mobil> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getMobilById(id)

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

    suspend fun updateMobil(
        idMobil: Int,
        platNomor: String,
        merek: String,
        idPelanggan: Int
    ): Result<Mobil> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateMobil(idMobil, platNomor, merek, idPelanggan)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    val resultData = body.data ?: Mobil(
                        idMobil = idMobil,
                        platNomor = platNomor,
                        merek = merek,
                        idPelanggan = idPelanggan,
                        idAdmin = 0,
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

    suspend fun deleteMobil(idMobil: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMobil(idMobil)

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

    suspend fun searchMobil(keyword: String): Result<List<Mobil>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchMobil(keyword)

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