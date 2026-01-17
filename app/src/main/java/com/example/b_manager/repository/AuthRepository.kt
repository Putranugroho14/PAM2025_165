package com.example.b_manager.repository

import com.example.b_manager.model.Admin
import com.example.b_manager.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import com.example.b_manager.model.ApiResponse
import com.google.gson.Gson

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
                Result.failure(Exception(parseError(response)))
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
                Result.failure(Exception(parseError(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(
        username: String,
        kodeRegistrasi: String,
        newPassword: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.resetPassword(username, kodeRegistrasi, newPassword)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.status == "success") {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal mereset password"))
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