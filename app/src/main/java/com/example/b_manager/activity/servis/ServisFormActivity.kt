package com.example.b_manager.activity.servis

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.b_manager.databinding.ActivityServisFormBinding
import com.example.b_manager.model.Mobil
import com.example.b_manager.utils.SessionManager
import com.example.b_manager.utils.ValidationHelper
import com.example.b_manager.viewmodel.MobilViewModel
import com.example.b_manager.viewmodel.ServisViewModel
import com.example.b_manager.utils.DialogUtils
import java.text.SimpleDateFormat
import java.util.*

class ServisFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServisFormBinding
    private val servisViewModel: ServisViewModel by viewModels()
    private val mobilViewModel: MobilViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var servisId: Int? = null
    private var isEditMode = false
    private var mobilList: List<Mobil> = emptyList()
    private var selectedMobilId: Int = 0
    private var selectedDate: String = ""

    private lateinit var mobilAdapter: ArrayAdapter<String>
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServisFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupAutoComplete()
        setupStatusAutoComplete()
        loadMobilData()
        loadIntentData()
        setupObservers()
        setupListeners()

        // Set tanggal hari ini sebagai default
        if (!isEditMode) {
            setTodayDate()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupAutoComplete() {
        // Setup adapter untuk AutoCompleteTextView Mobil
        mobilAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.actvMobil.setAdapter(mobilAdapter)

        // ✅ LIVE SEARCH: Search saat mengetik
        binding.actvMobil.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 1) {
                    // Search dengan minimal 1 karakter
                    mobilViewModel.searchMobil(query)
                } else if (query.isEmpty()) {
                    // Kosong = load semua
                    mobilViewModel.loadMobil()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Saat item dipilih dari dropdown
        binding.actvMobil.setOnItemClickListener { parent, view, position, id ->
            if (position < mobilList.size) {
                selectedMobilId = mobilList[position].idMobil
                binding.tilMobil.error = null
            }
        }
    }

    private fun setupStatusAutoComplete() {
        val statusList = arrayOf("proses", "selesai", "batal")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        binding.actvStatus.setAdapter(adapter)
        binding.actvStatus.setText("proses", false)
    }

    private fun setTodayDate() {
        val today = Calendar.getInstance()
        selectedDate = dateFormat.format(today.time)
        binding.etTanggalServis.setText(displayDateFormat.format(today.time))
    }

    private fun loadMobilData() {
        mobilViewModel.loadMobil()
    }

    private fun loadIntentData() {
        servisId = intent.getIntExtra("SERVIS_ID", 0)

        if (servisId != null && servisId != 0) {
            isEditMode = true

            binding.toolbar.title = "Edit Servis"

            binding.btnSave.text = "UPDATE"

            // Load data dari intent
            val platNomor = intent.getStringExtra("SERVIS_PLAT")
            val merek = intent.getStringExtra("SERVIS_MEREK")
            if (platNomor != null && merek != null) {
                binding.actvMobil.setText("$platNomor - $merek", false)
            }

            selectedMobilId = intent.getIntExtra("SERVIS_ID_MOBIL", 0)
            selectedDate = intent.getStringExtra("SERVIS_TANGGAL") ?: ""

            binding.etDeskripsi.setText(intent.getStringExtra("SERVIS_DESKRIPSI"))
            binding.etBiaya.setText(intent.getDoubleExtra("SERVIS_BIAYA", 0.0).toInt().toString())

            val status = intent.getStringExtra("SERVIS_STATUS") ?: "proses"
            binding.actvStatus.setText(status, false)

            // Format tanggal untuk display
            if (selectedDate.isNotEmpty()) {
                try {
                    val date = dateFormat.parse(selectedDate)
                    if (date != null) {
                        binding.etTanggalServis.setText(displayDateFormat.format(date))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            binding.toolbar.title = "Catat Servis Baru"

            binding.btnSave.text = "SIMPAN"
        }
    }

    private fun setupObservers() {
        mobilViewModel.mobilList.observe(this) { result ->
            result.onSuccess { list ->
                mobilList = list
                updateMobilList(list)

                // Set selected item untuk edit mode
                if (isEditMode && selectedMobilId != 0 && binding.actvMobil.text.isEmpty()) {
                    val mobil = list.find { it.idMobil == selectedMobilId }
                    mobil?.let {
                        binding.actvMobil.setText("${it.platNomor} - ${it.merek}", false)
                    }
                }
            }.onFailure { error ->
                Toast.makeText(this, "Gagal load mobil: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        servisViewModel.createResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data servis berhasil dicatat.",
                    type = "success",
                    onDismiss = {
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Mencatat",
                    message = error.message ?: "Terjadi kesalahan saat menyimpan data.",
                    type = "error"
                )
            }
        }

        servisViewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data servis berhasil diperbarui.",
                    type = "success",
                    onDismiss = {
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Update",
                    message = error.message ?: "Terjadi kesalahan saat memperbarui data.",
                    type = "error"
                )
            }
        }

        servisViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }
    }

    private fun updateMobilList(list: List<Mobil>) {
        val items = list.map { "${it.platNomor} - ${it.merek}" }
        mobilAdapter.clear()
        mobilAdapter.addAll(items)
        mobilAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        // ✅ Date Picker
        binding.etTanggalServis.setOnClickListener {
            showDatePicker()
        }

        // Save Button
        binding.btnSave.setOnClickListener {
            val mobilText = binding.actvMobil.text.toString().trim()
            val tanggal = selectedDate
            val deskripsi = binding.etDeskripsi.text.toString().trim()
            val biayaStr = binding.etBiaya.text.toString().trim()
            val status = binding.actvStatus.text.toString().trim()

            when {
                mobilText.isEmpty() -> {
                    binding.tilMobil.error = "Pilih mobil terlebih dahulu"
                    binding.actvMobil.requestFocus()
                }
                selectedMobilId == 0 -> {
                    binding.tilMobil.error = "Pilih mobil dari dropdown"
                    binding.actvMobil.requestFocus()
                }
                tanggal.isEmpty() -> {
                    Toast.makeText(this, "Pilih tanggal servis", Toast.LENGTH_SHORT).show()
                    binding.etTanggalServis.requestFocus()
                }
                !ValidationHelper.isValidDeskripsi(deskripsi) -> {
                    binding.etDeskripsi.error = "Deskripsi minimal 5 karakter"
                    binding.etDeskripsi.requestFocus()
                }
                !ValidationHelper.isValidBiaya(biayaStr) -> {
                    binding.etBiaya.error = "Biaya minimal Rp 10.000"
                    binding.etBiaya.requestFocus()
                }
                status.isEmpty() -> {
                    Toast.makeText(this, "Pilih status servis", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.tilMobil.error = null

                    val biaya = biayaStr.toDouble()

                    if (isEditMode) {
                        servisViewModel.updateServis(
                            servisId!!,
                            deskripsi,
                            biaya,
                            status,
                            tanggal
                        )
                    } else {
                        val adminId = sessionManager.getAdminId()
                        servisViewModel.createServis(
                            selectedMobilId,
                            adminId,
                            deskripsi,
                            biaya,
                            status,
                            tanggal
                        )
                    }
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        // Parse tanggal yang sudah dipilih jika ada
        if (selectedDate.isNotEmpty()) {
            try {
                val date = dateFormat.parse(selectedDate)
                if (date != null) {
                    calendar.time = date
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = dateFormat.format(calendar.time)
                binding.etTanggalServis.setText(displayDateFormat.format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}