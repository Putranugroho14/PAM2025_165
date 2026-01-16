package com.example.b_manager.activity.pelanggan

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.b_manager.utils.DialogUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.b_manager.adapter.PelangganAdapter
import com.example.b_manager.databinding.ActivityPelangganListBinding
import com.example.b_manager.model.Pelanggan
import com.example.b_manager.viewmodel.PelangganViewModel

class PelangganListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPelangganListBinding
    private val viewModel: PelangganViewModel by viewModels()
    private lateinit var adapter: PelangganAdapter

    private val addPelangganLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadPelanggan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPelangganListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadPelanggan()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = PelangganAdapter(
            onItemClick = { pelanggan ->
                navigateToDetail(pelanggan)
            },
            onEditClick = { pelanggan ->
                navigateToEdit(pelanggan)
            },
            onDeleteClick = { pelanggan ->
                showDeleteDialog(pelanggan)
            }
        )

        binding.rvPelanggan.layoutManager = LinearLayoutManager(this)
        binding.rvPelanggan.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.pelangganList.observe(this) { result ->
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    val query = binding.etSearch.text.toString()
                    if (query.isNotEmpty()) {
                        binding.tvEmpty.text = "Tidak ada hasil untuk \"$query\""
                    } else {
                        binding.tvEmpty.text = "Belum ada data pelanggan"
                    }
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvPelanggan.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvPelanggan.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Data",
                    message = error.message ?: "Terjadi kesalahan saat memuat data pelanggan.",
                    type = "error"
                )
            }
        }

        viewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data pelanggan berhasil dihapus.",
                    type = "success",
                    onDismiss = { viewModel.loadPelanggan() }
                )
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Menghapus",
                    message = error.message ?: "Terjadi kesalahan saat menghapus data.",
                    type = "error"
                )
            }
        }


    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, PelangganFormActivity::class.java)
            addPelangganLauncher.launch(intent)
        }

        // Live Search with EditText
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isEmpty()) {
                    viewModel.loadPelanggan()
                } else {
                    viewModel.searchPelanggan(query)
                }
            }

            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun navigateToDetail(pelanggan: Pelanggan) {
        val intent = Intent(this, PelangganDetailActivity::class.java)
        intent.putExtra("PELANGGAN_ID", pelanggan.idPelanggan)
        startActivity(intent)
    }

    private fun navigateToEdit(pelanggan: Pelanggan) {
        val intent = Intent(this, PelangganFormActivity::class.java)
        intent.putExtra("PELANGGAN_ID", pelanggan.idPelanggan)
        intent.putExtra("PELANGGAN_NAMA", pelanggan.namaPelanggan)
        intent.putExtra("PELANGGAN_HP", pelanggan.noHp)
        intent.putExtra("PELANGGAN_ALAMAT", pelanggan.alamat)
        addPelangganLauncher.launch(intent)
    }

    private fun showDeleteDialog(pelanggan: Pelanggan) {
        DialogUtils.showConfirmationDialog(
            context = this,
            title = "Hapus Data",
            message = "Yakin ingin menghapus pelanggan ${pelanggan.namaPelanggan}? Data yang dihapus tidak dapat dikembalikan.",
            positiveText = "Hapus",
            negativeText = "Batal",
            type = "danger",
            onPositive = {
                viewModel.deletePelanggan(pelanggan.idPelanggan)
            }
        )
    }
}