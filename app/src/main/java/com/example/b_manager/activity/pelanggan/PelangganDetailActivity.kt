package com.example.b_manager.activity.pelanggan

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.b_manager.databinding.ActivityPelangganDetailBinding
import com.example.b_manager.viewmodel.PelangganViewModel
import com.example.b_manager.utils.DialogUtils
import com.example.b_manager.utils.ValidationHelper
import android.content.Intent
import android.net.Uri

class PelangganDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPelangganDetailBinding
    private val viewModel: PelangganViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPelangganDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()

        val pelangganId = intent.getIntExtra("PELANGGAN_ID", 0)
        if (pelangganId != 0) {
            viewModel.loadPelangganById(pelangganId)
        } else {
            DialogUtils.showAlertDialog(
                context = this,
                title = "Error",
                message = "ID Pelanggan tidak valid.",
                type = "error",
                onDismiss = { finish() }
            )
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Pelanggan"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.pelangganDetail.observe(this) { result ->
            result.onSuccess { pelanggan ->
                binding.tvHeaderNama.text = pelanggan.namaPelanggan
                binding.tvNamaPelanggan.text = pelanggan.namaPelanggan
                binding.tvNoHp.text = pelanggan.noHp
                binding.tvAlamat.text = pelanggan.alamat ?: "-"
                binding.tvCreatedAt.text = pelanggan.createdAt ?: "-"

                // Setup Click Listeners for Phone & WA
                binding.btnActionCall.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:${pelanggan.noHp}")
                    startActivity(intent)
                }

                binding.btnActionWa.setOnClickListener {
                    val waNumber = ValidationHelper.formatWhatsAppNumber(pelanggan.noHp)
                    val message = "Halo ${pelanggan.namaPelanggan}, saya dari Bengkel B-Manager..."
                    val url = "https://api.whatsapp.com/send?phone=$waNumber&text=${Uri.encode(message)}"
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Detail",
                    message = error.message ?: "Terjadi kesalahan saat memuat data pelanggan.",
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