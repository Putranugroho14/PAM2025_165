package com.example.b_manager.activity.servis

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.b_manager.R
import com.example.b_manager.databinding.ActivityServisDetailBinding
import com.example.b_manager.utils.ValidationHelper
import com.example.b_manager.viewmodel.ServisViewModel
import com.example.b_manager.utils.DialogUtils
import android.content.Intent
import android.net.Uri

class ServisDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServisDetailBinding
    private val viewModel: ServisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServisDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupObservers()

        val servisId = intent.getIntExtra("SERVIS_ID", 0)
        if (servisId != 0) {
            viewModel.loadServisById(servisId)
        } else {
            DialogUtils.showAlertDialog(
                context = this,
                title = "Error",
                message = "ID Servis tidak valid.",
                type = "error",
                onDismiss = { finish() }
            )
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Detail Servis"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupObservers() {
        viewModel.servisDetail.observe(this) { result ->
            result.onSuccess { servis ->
                // Header Info
                binding.tvHeaderTitle.text = "Detail Servis"
                binding.tvHeaderSubtitle.text = "${servis.platNomor} • ${servis.merek}"

                // Vehicle Card
                binding.tvPlatNomor.text = servis.platNomor ?: "-"
                binding.tvMerek.text = servis.merek ?: "-"
                binding.tvNamaPelanggan.text = servis.namaPelanggan ?: "-"

                // Work Card
                binding.tvTanggalServis.text = ValidationHelper.formatDate(servis.tanggalServis)
                binding.tvDeskripsi.text = servis.deskripsi
                binding.tvBiaya.text = ValidationHelper.formatRupiah(servis.biaya)

                // Status with background
                binding.tvStatus.text = servis.status.uppercase()
                when {
                    servis.status.equals("proses", true) -> {
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_proses)
                    }
                    servis.status.equals("selesai", true) -> {
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                    }
                    servis.status.equals("batal", true) -> {
                        binding.tvStatus.setBackgroundResource(R.drawable.bg_status_batal)
                    }
                }

                binding.tvCreatedAt.text = "Dicatat pada: ${servis.createdAt}"
                binding.contentLayout.visibility = View.VISIBLE

                // Setup Click Listeners for Phone & WA
                if (!servis.noHp.isNullOrEmpty()) {
                    binding.btnActionCall.visibility = View.VISIBLE
                    binding.btnActionWa.visibility = View.VISIBLE

                    binding.btnActionCall.setOnClickListener {
                        val intent = Intent(Intent.ACTION_DIAL)
                        intent.data = Uri.parse("tel:${servis.noHp}")
                        startActivity(intent)
                    }

                    binding.btnActionWa.setOnClickListener {
                        val waNumber = ValidationHelper.formatWhatsAppNumber(servis.noHp)
                        val message = "Halo ${servis.namaPelanggan}, saya dari Bengkel B-Manager. Mobil ${servis.platNomor} (${servis.merek}) statusnya saat ini: ${servis.status.uppercase()}."
                        val url = "https://api.whatsapp.com/send?phone=$waNumber&text=${Uri.encode(message)}"
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)
                    }
                } else {
                    binding.btnActionCall.visibility = View.GONE
                    binding.btnActionWa.visibility = View.GONE
                }
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Detail",
                    message = error.message ?: "Terjadi kesalahan saat memuat data servis.",
                    type = "error",
                    onDismiss = { finish() }
                )
            }
        }

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
}