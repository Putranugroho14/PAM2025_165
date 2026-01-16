package com.example.b_manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.b_manager.model.Pelanggan
import com.example.b_manager.repository.PelangganRepository
import kotlinx.coroutines.launch

class PelangganViewModel : ViewModel() {

    private val repository = PelangganRepository()

    private val _pelangganList = MutableLiveData<Result<List<Pelanggan>>>()
    val pelangganList: LiveData<Result<List<Pelanggan>>> = _pelangganList

    private val _pelangganDetail = MutableLiveData<Result<Pelanggan>>()
    val pelangganDetail: LiveData<Result<Pelanggan>> = _pelangganDetail

    private val _createResult = MutableLiveData<Result<Pelanggan>>()
    val createResult: LiveData<Result<Pelanggan>> = _createResult

    private val _updateResult = MutableLiveData<Result<Pelanggan>>()
    val updateResult: LiveData<Result<Pelanggan>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadPelanggan() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getPelanggan()
            _pelangganList.value = result
            _isLoading.value = false
        }
    }

    fun loadPelangganById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getPelangganById(id)
            _pelangganDetail.value = result
            _isLoading.value = false
        }
    }

    fun createPelanggan(namaPelanggan: String, noHp: String, alamat: String?, idAdmin: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createPelanggan(namaPelanggan, noHp, alamat, idAdmin)
            _createResult.value = result
            _isLoading.value = false
        }
    }

    fun updatePelanggan(idPelanggan: Int, namaPelanggan: String, noHp: String, alamat: String?) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updatePelanggan(idPelanggan, namaPelanggan, noHp, alamat)
            _updateResult.value = result
            _isLoading.value = false
        }
    }

    fun deletePelanggan(idPelanggan: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deletePelanggan(idPelanggan)
            _deleteResult.value = result
            _isLoading.value = false
        }
    }

    fun searchPelanggan(keyword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.searchPelanggan(keyword)
            _pelangganList.value = result
            _isLoading.value = false
        }
    }
}