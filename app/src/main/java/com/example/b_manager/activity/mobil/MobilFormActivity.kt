package com.example.b_manager.activity.mobil

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.b_manager.databinding.ActivityMobilFormBinding
import com.example.b_manager.model.Pelanggan
import com.example.b_manager.utils.SessionManager
import com.example.b_manager.utils.ValidationHelper
import com.example.b_manager.viewmodel.MobilViewModel
import com.example.b_manager.viewmodel.PelangganViewModel
import com.example.b_manager.utils.DialogUtils

class MobilFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobilFormBinding
    private val mobilViewModel: MobilViewModel by viewModels()
    private val pelangganViewModel: PelangganViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var mobilId: Int? = null
    private var isEditMode = false
    private var pelangganList: List<Pelanggan> = emptyList()
    private var selectedPelangganId: Int = 0

    // Adapter untuk AutoCompleteTextView
    private lateinit var pelangganAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobilFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        setupAutoComplete()
        loadPelangganData()
        loadIntentData()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupAutoComplete() {
        // Setup adapter untuk AutoCompleteTextView
        pelangganAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf()
        )
        binding.actvPelanggan.setAdapter(pelangganAdapter)

        // Live search saat mengetik
        binding.actvPelanggan.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length >= 1) {
                    // Search dengan minimal 1 karakter
                    pelangganViewModel.searchPelanggan(query)
                } else if (query.isEmpty()) {
                    // Kosong = load semua
                    pelangganViewModel.loadPelanggan()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Saat item dipilih dari dropdown
        binding.actvPelanggan.setOnItemClickListener { parent, view, position, id ->
            if (position < pelangganList.size) {
                selectedPelangganId = pelangganList[position].idPelanggan
                binding.tilPelanggan.error = null
            }
        }
    }

    private fun loadPelangganData() {
        pelangganViewModel.loadPelanggan()
    }

    private fun loadIntentData() {
        mobilId = intent.getIntExtra("MOBIL_ID", 0)

        if (mobilId != null && mobilId != 0) {
            isEditMode = true

            binding.toolbar.title = "Edit Mobil"

            binding.btnSave.text = "UPDATE"

            binding.etPlatNomor.setText(intent.getStringExtra("MOBIL_PLAT"))
            binding.etMerek.setText(intent.getStringExtra("MOBIL_MEREK"))
            selectedPelangganId = intent.getIntExtra("MOBIL_ID_PELANGGAN", 0)

            // Set text untuk edit mode (akan di-set setelah data pelanggan loaded)
            val namaPelanggan = intent.getStringExtra("MOBIL_NAMA_PELANGGAN")
            if (namaPelanggan != null) {
                binding.actvPelanggan.setText(namaPelanggan, false)
            }
        } else {
            binding.toolbar.title = "Tambah Mobil"

            binding.btnSave.text = "SIMPAN"
        }
    }

    private fun setupObservers() {
        pelangganViewModel.pelangganList.observe(this) { result ->
            result.onSuccess { list ->
                pelangganList = list
                updateAutoCompleteList(list)

                // Jika edit mode, set selected item
                if (isEditMode && selectedPelangganId != 0 && binding.actvPelanggan.text.isEmpty()) {
                    val pelanggan = list.find { it.idPelanggan == selectedPelangganId }
                    pelanggan?.let {
                        binding.actvPelanggan.setText("${it.namaPelanggan} - ${it.noHp}", false)
                    }
                }
            }.onFailure { error ->
                Toast.makeText(this, "Gagal load pelanggan: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        }

        mobilViewModel.createResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data mobil berhasil ditambahkan.",
                    type = "success",
                    onDismiss = {
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Menambahkan",
                    message = error.message ?: "Terjadi kesalahan saat menyimpan data.",
                    type = "error"
                )
            }
        }

        mobilViewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data mobil berhasil diperbarui.",
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

        mobilViewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }
    }

    private fun updateAutoCompleteList(list: List<Pelanggan>) {
        val items = list.map { "${it.namaPelanggan} - ${it.noHp}" }
        pelangganAdapter.clear()
        pelangganAdapter.addAll(items)
        pelangganAdapter.notifyDataSetChanged()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val platNomor = binding.etPlatNomor.text.toString().trim().uppercase()
            val merek = binding.etMerek.text.toString().trim()
            val pelangganText = binding.actvPelanggan.text.toString().trim()

            when {
                !ValidationHelper.isValidPlatNomor(platNomor) -> {
                    binding.etPlatNomor.error = "Format plat tidak valid (Contoh: B 1234 XYZ)"
                    binding.etPlatNomor.requestFocus()
                }
                !ValidationHelper.isValidMerek(merek) -> {
                    binding.etMerek.error = "Merek hanya boleh huruf, angka, spasi, dan dash (-)"
                    binding.etMerek.requestFocus()
                }
                pelangganText.isEmpty() -> {
                    binding.tilPelanggan.error = "Pilih pelanggan terlebih dahulu"
                    binding.actvPelanggan.requestFocus()
                }
                selectedPelangganId == 0 -> {
                    binding.tilPelanggan.error = "Pilih pelanggan dari dropdown"
                    binding.actvPelanggan.requestFocus()
                }
                else -> {
                    binding.tilPelanggan.error = null

                    if (isEditMode) {
                        mobilViewModel.updateMobil(mobilId!!, platNomor, merek, selectedPelangganId)
                    } else {
                        val adminId = sessionManager.getAdminId()
                        mobilViewModel.createMobil(platNomor, merek, selectedPelangganId, adminId)
                    }
                }
            }
        }
    }
}