package com.example.b_manager.activity.mobil

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
import com.example.b_manager.adapter.MobilAdapter
import com.example.b_manager.databinding.ActivityMobilListBinding
import com.example.b_manager.model.Mobil
import com.example.b_manager.viewmodel.MobilViewModel

class MobilListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMobilListBinding
    private val viewModel: MobilViewModel by viewModels()
    private lateinit var adapter: MobilAdapter

    private val addMobilLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadMobil() // Refresh data setelah simpan/edit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMobilListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadMobil()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = MobilAdapter(
            onItemClick = { mobil -> navigateToDetail(mobil) },
            onEditClick = { mobil -> navigateToEdit(mobil) },
            onDeleteClick = { mobil -> showDeleteDialog(mobil) }
        )

        binding.rvMobil.layoutManager = LinearLayoutManager(this) // Wajib agar scroll lancar
        binding.rvMobil.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.mobilList.observe(this) { result ->
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvMobil.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvMobil.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Data",
                    message = error.message ?: "Terjadi kesalahan saat memuat data mobil.",
                    type = "error"
                )
            }
        }

        viewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Data mobil berhasil dihapus.",
                    type = "success",
                    onDismiss = { viewModel.loadMobil() }
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

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, MobilFormActivity::class.java)
            addMobilLauncher.launch(intent)
        }

        binding.searchView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    viewModel.searchMobil(query)
                } else {
                    viewModel.loadMobil()
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun navigateToDetail(mobil: Mobil) {
        val intent = Intent(this, MobilDetailActivity::class.java)
        intent.putExtra("MOBIL_ID", mobil.idMobil)
        startActivity(intent)
    }

    private fun navigateToEdit(mobil: Mobil) {
        val intent = Intent(this, MobilFormActivity::class.java).apply {
            putExtra("MOBIL_ID", mobil.idMobil)
            putExtra("MOBIL_PLAT", mobil.platNomor)
            putExtra("MOBIL_MEREK", mobil.merek)
            putExtra("MOBIL_ID_PELANGGAN", mobil.idPelanggan)
        }
        addMobilLauncher.launch(intent)
    }

    private fun showDeleteDialog(mobil: Mobil) {
        DialogUtils.showConfirmationDialog(
            context = this,
            title = "Hapus Data",
            message = "Yakin ingin menghapus mobil ${mobil.platNomor}? Data yang dihapus tidak dapat dikembalikan.",
            positiveText = "Hapus",
            negativeText = "Batal",
            type = "danger",
            onPositive = { viewModel.deleteMobil(mobil.idMobil) }
        )
    }
}