package com.example.b_manager.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.b_manager.model.Servis
import com.example.b_manager.repository.ServisRepository
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.*


class ServisViewModel : ViewModel() {

    private val repository = ServisRepository()

    private val _servisList = MutableLiveData<Result<List<Servis>>>()
    val servisList: LiveData<Result<List<Servis>>> = _servisList

    private val _servisDetail = MutableLiveData<Result<Servis>>()
    val servisDetail: LiveData<Result<Servis>> = _servisDetail

    private val _createResult = MutableLiveData<Result<Servis>>()
    val createResult: LiveData<Result<Servis>> = _createResult

    private val _updateResult = MutableLiveData<Result<Servis>>()
    val updateResult: LiveData<Result<Servis>> = _updateResult

    private val _deleteResult = MutableLiveData<Result<Boolean>>()
    val deleteResult: LiveData<Result<Boolean>> = _deleteResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // 🔑 SIMPAN SEMUA DATA SERVIS
    private var allServis: List<Servis> = emptyList()

    private val _totalBiaya = MutableLiveData<Double>()
    val totalBiaya: LiveData<Double> = _totalBiaya

    private val _jumlahServis = MutableLiveData<Int>()
    val jumlahServis: LiveData<Int> = _jumlahServis

    private fun updateSummary(list: List<Servis>) {
        _jumlahServis.value = list.size
        _totalBiaya.value = list.sumOf { it.biaya ?: 0.0 }
    }





    fun loadServis() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getServis()
            result.onSuccess {
                allServis = it
                _servisList.value = Result.success(it)
            }.onFailure {
                _servisList.value = Result.failure(it)
            }
            _isLoading.value = false
        }

    }


    fun loadServisById(id: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            _servisDetail.value = repository.getServisById(id)
            _isLoading.value = false
        }
    }

    fun createServis(
        idMobil: Int,
        idAdmin: Int,
        deskripsi: String,
        biaya: Double,
        status: String,
        tanggalServis: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            _createResult.value =
                repository.createServis(idMobil, idAdmin, deskripsi, biaya, status, tanggalServis)
            _isLoading.value = false
        }
    }

    fun updateServis(
        idServis: Int,
        deskripsi: String,
        biaya: Double,
        status: String,
        tanggalServis: String
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            _updateResult.value =
                repository.updateServis(idServis, deskripsi, biaya, status, tanggalServis)
            _isLoading.value = false
        }
    }

    fun deleteServis(idServis: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            _deleteResult.value = repository.deleteServis(idServis)
            _isLoading.value = false
        }
    }

    fun searchServis(keyword: String) {
        val filtered = allServis.filter {
            (it.platNomor ?: "").contains(keyword, true) ||
                    (it.namaPelanggan ?: "").contains(keyword, true)
        }
        _servisList.value = Result.success(filtered)
    }


    // ✅ FILTER LAPORAN DI ANDROID (TANPA API)
    fun filterServis(
        nama: String?,
        status: String?,
        tanggalDari: String?,
        tanggalSampai: String?
    ) {
        viewModelScope.launch {
            // 1. Jika data lokal (allServis) kosong, coba load dari API dulu
            if (allServis.isEmpty()) {
                val result = repository.getServis()
                result.onSuccess {
                    allServis = it
                }
            }

            // 2. Jalankan Filter pada data yang sudah ada (allServis)
            val sdfOnlyDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateDari = tanggalDari?.let { sdfOnlyDate.parse(it) }
            val dateSampai = tanggalSampai?.let { sdfOnlyDate.parse(it) }

            val filteredResult = allServis.filter { servis ->
                // Filter Nama & Plat (Gunakan contains untuk pencarian sebagian kata)
                val targetNama = nama?.lowercase()?.trim() ?: ""
                val cocokNama = targetNama.isEmpty() || 
                    (servis.namaPelanggan?.lowercase()?.contains(targetNama) == true) ||
                    (servis.platNomor?.lowercase()?.contains(targetNama) == true)

                // Filter Status
                val cocokStatus = status.isNullOrBlank() || status == "Semua" || 
                    servis.status.equals(status, true)

                // Filter Tanggal
                val formatTanggal = if (servis.tanggalServis.length > 10) "yyyy-MM-dd HH:mm:ss" else "yyyy-MM-dd"
                val tanggalServisDate = try {
                    SimpleDateFormat(formatTanggal, Locale.getDefault()).parse(servis.tanggalServis)
                } catch (e: Exception) {
                    null
                }

                var cocokTanggal = true
                if (tanggalServisDate != null) {
                    val recordDateStr = sdfOnlyDate.format(tanggalServisDate)
                    val recordDate = sdfOnlyDate.parse(recordDateStr)!!
                    val dariOk = dateDari == null || !recordDate.before(dateDari)
                    val sampaiOk = dateSampai == null || !recordDate.after(dateSampai)
                    cocokTanggal = dariOk && sampaiOk
                }

                cocokNama && cocokStatus && cocokTanggal
            }

            _servisList.value = Result.success(filteredResult)
        }
    }


}
