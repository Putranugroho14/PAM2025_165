package com.example.b_manager.activity.pelanggan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.b_manager.databinding.ActivityPelangganFormBinding
import com.example.b_manager.utils.SessionManager
import com.example.b_manager.utils.ValidationHelper
import com.example.b_manager.viewmodel.PelangganViewModel
import com.example.b_manager.utils.DialogUtils

class PelangganFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPelangganFormBinding
    private val viewModel: PelangganViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    private var pelangganId: Int? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPelangganFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        setupToolbar()
        loadIntentData()
        setupObservers()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Title is set on toolbar directly if needed, or we rely on the xml
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadIntentData() {
        pelangganId = intent.getIntExtra("PELANGGAN_ID", 0)

        if (pelangganId != null && pelangganId != 0) {
            isEditMode = true

            // Set title on toolbar
            binding.toolbar.title = "Edit Pelanggan"

            binding.btnSave.text = "UPDATE PELANGGAN"

            binding.etNamaPelanggan.setText(intent.getStringExtra("PELANGGAN_NAMA"))
            binding.etNoHp.setText(intent.getStringExtra("PELANGGAN_HP"))
            binding.etAlamat.setText(intent.getStringExtra("PELANGGAN_ALAMAT"))
        } else {
            // Set title on toolbar
            binding.toolbar.title = "Tambah Pelanggan"

            binding.btnSave.text = "SIMPAN PELANGGAN"
        }
    }

    private fun setupObservers() {
        viewModel.createResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data pelanggan berhasil ditambahkan.",
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

        viewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data pelanggan berhasil diperbarui.",
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

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            val namaPelanggan = binding.etNamaPelanggan.text.toString().trim()
            val noHp = binding.etNoHp.text.toString().trim()
            val alamat = binding.etAlamat.text.toString().trim()

            when {
                namaPelanggan.isEmpty() -> {
                    binding.etNamaPelanggan.error = "Nama tidak boleh kosong"
                    binding.etNamaPelanggan.requestFocus()
                }
                noHp.isEmpty() -> {
                    binding.etNoHp.error = "Nomor HP tidak boleh kosong"
                    binding.etNoHp.requestFocus()
                }
                !ValidationHelper.isValidPhoneNumber(noHp) -> {
                    binding.etNoHp.error = "Format: 08xx / +628xx / 628xx (10-15 digit)"
                    binding.etNoHp.requestFocus()
                }
                else -> {
                    // Normalisasi nomor HP ke format 08xx
                    val normalizedPhone = ValidationHelper.normalizePhoneNumber(noHp)

                    if (isEditMode) {
                        viewModel.updatePelanggan(
                            pelangganId!!,
                            namaPelanggan,
                            normalizedPhone,
                            alamat.ifEmpty { null }
                        )
                    } else {
                        val adminId = sessionManager.getAdminId()
                        viewModel.createPelanggan(
                            namaPelanggan,
                            normalizedPhone,
                            alamat.ifEmpty { null },
                            adminId
                        )
                    }
                }
            }
        }
    }
}