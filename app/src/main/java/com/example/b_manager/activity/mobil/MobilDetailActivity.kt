package com.example.b_manager.activity.mobil

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.b_manager.databinding.ActivityMobilDetailBinding
import com.example.b_manager.viewmodel.MobilViewModel
import com.example.b_manager.utils.DialogUtils

class MobilDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobilDetailBinding
    private val viewModel: MobilViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobilDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()

        val mobilId = intent.getIntExtra("MOBIL_ID", 0)
        if (mobilId != 0) {
            viewModel.loadMobilById(mobilId)
        } else {
            DialogUtils.showAlertDialog(
                context = this,
                title = "Error",
                message = "ID Mobil tidak valid.",
                type = "error",
                onDismiss = { finish() }
            )
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Mobil"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.mobilDetail.observe(this) { result ->
            result.onSuccess { mobil ->
                binding.tvPlatNomor.text = mobil.platNomor
                binding.tvMerek.text = mobil.merek
                binding.tvNamaPelanggan.text = mobil.namaPelanggan ?: "-"
                binding.tvCreatedAt.text = mobil.createdAt
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Detail",
                    message = error.message ?: "Terjadi kesalahan saat memuat data mobil.",
                    type = "error",
                    onDismiss = { finish() }
                )
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }
}