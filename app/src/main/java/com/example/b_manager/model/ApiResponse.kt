package com.example.b_manager.model

data class ApiResponse<T>(
    val status: String,
    val message: String,
    val data: T? = null
)

data class ListResponse<T>(
    val status: String,
    val message: String,
    val data: List<T>? = null,
    val total: Int? = null
)