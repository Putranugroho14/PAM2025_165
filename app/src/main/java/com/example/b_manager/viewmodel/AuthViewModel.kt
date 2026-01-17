package com.example.b_manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.b_manager.model.Admin
import com.example.b_manager.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _registerResult = MutableLiveData<Result<Admin>>()
    val registerResult: LiveData<Result<Admin>> = _registerResult

    private val _loginResult = MutableLiveData<Result<Admin>>()
    val loginResult: LiveData<Result<Admin>> = _loginResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _resetResult = MutableLiveData<Result<Boolean>>()
    val resetResult: LiveData<Result<Boolean>> = _resetResult

    fun register(kodeRegistrasi: String, username: String, password: String, namaLengkap: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(kodeRegistrasi, username, password, namaLengkap)
            _registerResult.value = result
            _isLoading.value = false
        }
    }

    fun login(username: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(username, password)
            _loginResult.value = result
            _isLoading.value = false
        }
    }

    fun resetPassword(username: String, kodeRegistrasi: String, newPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.resetPassword(username, kodeRegistrasi, newPassword)
            _resetResult.value = result
            _isLoading.value = false
        }
    }
}