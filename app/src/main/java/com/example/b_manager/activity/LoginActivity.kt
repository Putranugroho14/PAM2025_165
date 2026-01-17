package com.example.b_manager.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.LayoutInflater
import android.widget.Toast
import android.widget.EditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.b_manager.R
import com.example.b_manager.databinding.ActivityLoginBinding
import com.example.b_manager.utils.SessionManager
import com.example.b_manager.viewmodel.AuthViewModel
import com.example.b_manager.utils.DialogUtils

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Cek apakah sudah login
        if (sessionManager.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { admin ->
                sessionManager.createLoginSession(admin)
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Login Berhasil",
                    message = "Selamat datang kembali, ${admin.namaLengkap}!",
                    type = "success",
                    onDismiss = { navigateToMain() }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Login Gagal",
                    message = error.message ?: "Terjadi kesalahan saat login. Periksa username dan password Anda.",
                    type = "error"
                )
            }
        }

        viewModel.resetResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Password Anda telah berhasil direset. Silakan login dengan password baru.",
                    type = "success"
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal",
                    message = error.message ?: "Gagal mereset password.",
                    type = "error"
                )
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnToRegister.isEnabled = !isLoading
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                username.isEmpty() -> {
                    binding.etUsername.error = "Username tidak boleh kosong"
                    binding.etUsername.requestFocus()
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Password tidak boleh kosong"
                    binding.etPassword.requestFocus()
                }
                else -> {
                    viewModel.login(username, password)
                }
            }
        }

        binding.btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.et_reset_username)
        val etKode = dialogView.findViewById<EditText>(R.id.et_reset_kode)
        val etNewPass = dialogView.findViewById<EditText>(R.id.et_new_password)

        MaterialAlertDialogBuilder(this)
            .setTitle("Lupa Password")
            .setMessage("Masukkan data di bawah ini untuk mereset password Anda.")
            .setView(dialogView)
            .setPositiveButton("Reset") { dialog, _ ->
                val username = etUsername.text.toString().trim()
                val kode = etKode.text.toString().trim()
                val newPass = etNewPass.text.toString().trim()

                if (username.isNotEmpty() && kode.isNotEmpty() && newPass.length >= 6) {
                    viewModel.resetPassword(username, kode, newPass)
                } else {
                    Toast.makeText(this, "Mohon isi semua data dengan benar (Password min 6 karakter)", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}