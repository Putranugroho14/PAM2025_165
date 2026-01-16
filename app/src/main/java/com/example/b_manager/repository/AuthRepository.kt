package com.example.b_manager.repository

import com.example.b_manager.model.Admin
import com.example.b_manager.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository {

    private val apiService = RetrofitClient.apiService

    suspend fun register(
        kodeRegistrasi: String,
        username: String,
        password: String,
        namaLengkap: String
    ): Result<Admin> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.register(kodeRegistrasi, username, password, namaLengkap)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Registrasi gagal"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(
        username: String,
        password: String
    ): Result<Admin> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(username, password)

            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success" && body.data != null) {
                    Result.success(body.data)
                } else {
                    Result.failure(Exception(body?.message ?: "Login gagal"))
                }
            } else {
                Result.failure(Exception("HTTP Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}