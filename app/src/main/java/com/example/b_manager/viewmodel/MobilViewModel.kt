package com.example.b_manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.b_manager.model.Mobil
import com.example.b_manager.repository.MobilRepository
import kotlinx.coroutines.launch

class MobilViewModel : ViewModel() {

    private val repository = MobilRepository()

    private val _mobilList = MutableLiveData<Result<List<Mobil>>>()
    val mobilList: LiveData<Result<List<Mobil>>> = _mobilList

    private val _mobilDetail = MutableLiveData<Result<Mobil>>()
    val mobilDetail: LiveData<Result<Mobil>> = _mobilDetail

    private val _createResult = MutableLiveData<Result<Mobil>>()
    val createResult: LiveData<Result<Mobil>> = _createResult

    private val _updateResult = MutableLiveData<Result<Mobil>>()
    val updateResult: LiveData<Result<Mobil>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadMobil() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getMobil()
            _mobilList.value = result
            _isLoading.value = false
        }
    }

    fun loadMobilById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getMobilById(id)
            _mobilDetail.value = result
            _isLoading.value = false
        }
    }

    fun createMobil(platNomor: String, merek: String, idPelanggan: Int, idAdmin: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.createMobil(platNomor, merek, idPelanggan, idAdmin)
            _createResult.value = result
            _isLoading.value = false
        }
    }

    fun updateMobil(idMobil: Int, platNomor: String, merek: String, idPelanggan: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateMobil(idMobil, platNomor, merek, idPelanggan)
            _updateResult.value = result
            _isLoading.value = false
        }
    }

    fun deleteMobil(idMobil: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteMobil(idMobil)
            _deleteResult.value = result
            _isLoading.value = false
        }
    }

    fun searchMobil(keyword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.searchMobil(keyword)
            _mobilList.value = result
            _isLoading.value = false
        }
    }
}