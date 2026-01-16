package com.example.b_manager.repository

import com.example.b_manager.model.Pelanggan
import com.example.b_manager.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.gson.Gson
import com.example.b_manager.model.ApiResponse
import retrofit2.Response

class PelangganRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun createPelanggan(
        namaPelanggan: String,
        noHp: String,
        alamat: String?,
        idAdmin: Int
    ): Result<Pelanggan> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createPelanggan(namaPelanggan, noHp, alamat, idAdmin)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    // Jika API tidak mengembalikan object data, buat dummy agar tidak Result.failure
                    val resultData = body.data ?: Pelanggan(
                        idPelanggan = 0,
                        namaPelanggan = namaPelanggan,
                        noHp = noHp,
                        alamat = alamat,
                        idAdmin = idAdmin,
                        createdAt = ""
                    )
                    Result.success(resultData)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal menambahkan pelanggan"))
                }
            } else {
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPelanggan(): Result<List<Pelanggan>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPelanggan()

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

    suspend fun getPelangganById(id: Int): Result<Pelanggan> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getPelangganById(id)

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

    suspend fun updatePelanggan(
        idPelanggan: Int,
        namaPelanggan: String,
        noHp: String,
        alamat: String?
    ): Result<Pelanggan> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updatePelanggan(idPelanggan, namaPelanggan, noHp, alamat)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    // Jika API tidak mengembalikan object data, buat dummy agar tidak Result.failure
                    val resultData = body.data ?: Pelanggan(
                        idPelanggan = idPelanggan,
                        namaPelanggan = namaPelanggan,
                        noHp = noHp,
                        alamat = alamat,
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

    suspend fun deletePelanggan(idPelanggan: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deletePelanggan(idPelanggan)

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

    suspend fun searchPelanggan(keyword: String): Result<List<Pelanggan>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchPelanggan(keyword)

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