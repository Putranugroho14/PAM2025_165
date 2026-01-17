package com.example.b_manager.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.b_manager.databinding.ActivityRegisterBinding
import com.example.b_manager.utils.ValidationHelper
import com.example.b_manager.viewmodel.AuthViewModel
import com.example.b_manager.utils.DialogUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Registrasi Berhasil",
                    message = "Akun Anda telah berhasil dibuat. Silakan login untuk melanjutkan.",
                    type = "success",
                    onDismiss = { finish() }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Registrasi Gagal",
                    message = error.message ?: "Terjadi kesalahan saat registrasi.",
                    type = "error"
                )
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // ID progressBar otomatis jadi cammelCase dari progress_bar
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnRegister.isEnabled = !isLoading
            // Sekarang btnToLogin sudah ada di XML
            binding.btnToLogin.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val kodeRegistrasi = binding.etKodeRegistrasi.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val namaLengkap = binding.etNamaLengkap.text.toString().trim()

            if (validateInput(kodeRegistrasi, username, password, namaLengkap)) {
                viewModel.register(kodeRegistrasi, username, password, namaLengkap)
            }
        }

        binding.btnToLogin.setOnClickListener {
            finish() // Kembali ke halaman Login
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun validateInput(kode: String, user: String, pass: String, nama: String): Boolean {
        return when {
            kode.isEmpty() -> {
                binding.etKodeRegistrasi.error = "Kode registrasi kosong"
                false
            }
            !ValidationHelper.isValidKodeRegistrasi(kode) -> {
                binding.etKodeRegistrasi.error = "Format: SA + 6 digit angka (Cth: SA123456)"
                false
            }
            user.isEmpty() -> {
                binding.etUsername.error = "Username kosong"
                false
            }
            pass.length < 5 -> {
                binding.etPassword.error = "Password minimal 5 karakter"
                false
            }
            nama.isEmpty() -> {
                binding.etNamaLengkap.error = "Nama lengkap kosong"
                false
            }
            else -> true
        }
    }
}