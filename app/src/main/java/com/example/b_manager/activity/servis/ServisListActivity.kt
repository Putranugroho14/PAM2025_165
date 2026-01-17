package com.example.b_manager.activity.servis

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.example.b_manager.utils.DialogUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.b_manager.R
import com.example.b_manager.adapter.ServisAdapter
import com.example.b_manager.databinding.ActivityServisListBinding
import com.example.b_manager.model.Servis
import com.example.b_manager.viewmodel.ServisViewModel
import com.example.b_manager.databinding.BottomSheetFilterServisBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import java.text.SimpleDateFormat
import java.util.*
import java.text.NumberFormat



class ServisListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServisListBinding
    private val viewModel: ServisViewModel by viewModels()
    private lateinit var adapter: ServisAdapter

    private var dariTanggal: String? = null
    private var sampaiTanggal: String? = null
    private var statusFilter: String? = null
    private var namaFilter: String? = null

    private val addServisLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.loadServis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServisListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadServis()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ServisAdapter(
            onItemClick = { servis ->
                navigateToDetail(servis)
            },
            onEditClick = { servis ->
                navigateToEdit(servis)
            },
            onDeleteClick = { servis ->
                showDeleteDialog(servis)
            }
        )

        binding.rvServis.layoutManager = LinearLayoutManager(this)
        binding.rvServis.adapter = adapter
    }

    private fun setupObservers() {

        viewModel.servisList.observe(this) { result ->
            result.onSuccess { list ->
                // === LIST ===
                if (list.isEmpty()) {
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.rvServis.visibility = View.GONE
                } else {
                    binding.tvEmpty.visibility = View.GONE
                    binding.rvServis.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Data",
                    message = error.message ?: "Terjadi kesalahan saat memuat data servis.",
                    type = "error"
                )
            }
        }

        viewModel.deleteResult.observe(this) { result ->
            result.onSuccess {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Berhasil",
                    message = "Riwayat servis berhasil dihapus.",
                    type = "success",
                    onDismiss = { viewModel.loadServis() }
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

        viewModel.isLoading.observe(this) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }


    private fun setupListeners() {
        binding.fabAdd.setOnClickListener {
            val intent = Intent(this, ServisFormActivity::class.java)
            addServisLauncher.launch(intent)
        }

        binding.searchView.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        binding.ivFilter.setOnClickListener {
            showFilterBottomSheet()
        }
    }

    private fun applyFilters() {
        val query = binding.searchView.text.toString().trim()
        
        // Gabungkan filter dari search bar dan bottom sheet
        // Jika namaFilter ada di bottom sheet, prioritaskan itu atau gabungkan jika perlu.
        // Namun biasanya user mengharapkan search bar sebagai filter global.
        
        viewModel.filterServis(
            nama = if (query.isEmpty()) namaFilter else query,
            status = statusFilter,
            tanggalDari = dariTanggal,
            tanggalSampai = sampaiTanggal
        )
    }

    private fun navigateToDetail(servis: Servis) {
        val intent = Intent(this, ServisDetailActivity::class.java)
        intent.putExtra("SERVIS_ID", servis.idServis)
        startActivity(intent)
    }

    private fun navigateToEdit(servis: Servis) {
        val intent = Intent(this, ServisFormActivity::class.java)
        intent.putExtra("SERVIS_ID", servis.idServis)
        intent.putExtra("SERVIS_ID_MOBIL", servis.idMobil)
        intent.putExtra("SERVIS_DESKRIPSI", servis.deskripsi)
        intent.putExtra("SERVIS_BIAYA", servis.biaya)
        intent.putExtra("SERVIS_STATUS", servis.status)
        addServisLauncher.launch(intent)
    }

    private fun showDeleteDialog(servis: Servis) {
        DialogUtils.showConfirmationDialog(
            context = this,
            title = "Hapus Data",
            message = "Yakin ingin menghapus riwayat servis ini? Data yang dihapus tidak dapat dikembalikan.",
            positiveText = "Hapus",
            negativeText = "Batal",
            type = "danger",
            onPositive = {
                viewModel.deleteServis(servis.idServis)
            }
        )
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val sheetBinding = BottomSheetFilterServisBinding.inflate(layoutInflater)
        dialog.setContentView(sheetBinding.root)

        sheetBinding.btnTanggalDari.setOnClickListener {
            showDatePicker { date ->
                sheetBinding.btnTanggalDari.text = date
            }
        }

        sheetBinding.btnTanggalSampai.setOnClickListener {
            showDatePicker { date ->
                sheetBinding.btnTanggalSampai.text = date
            }
        }
        val statusList = listOf(
            "Semua",
            "Proses",
            "Selesai",
            "Batal"
        )

        val statusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            statusList
        )

        sheetBinding.spinnerFilterStatus.setAdapter(statusAdapter)
        sheetBinding.spinnerFilterStatus.setText("Semua", false)


        sheetBinding.btnApplyFilter.setOnClickListener {
            namaFilter = sheetBinding.etFilterNamaPelanggan.text.toString().trim()
            if (namaFilter?.isEmpty() == true) namaFilter = null
            
            val statusText = sheetBinding.spinnerFilterStatus.text.toString()
            statusFilter = if (statusText == "Semua" || statusText.isEmpty()) null else statusText

            dariTanggal = sheetBinding.btnTanggalDari.text.toString()
                .takeIf { it != "Dari" }

            sampaiTanggal = sheetBinding.btnTanggalSampai.text.toString()
                .takeIf { it != "Sampai" }

            applyFilters()
            dialog.dismiss()
        }


        sheetBinding.btnResetFilter.setOnClickListener {
            namaFilter = null
            statusFilter = null
            dariTanggal = null
            sampaiTanggal = null
            binding.searchView.text?.clear()
            viewModel.loadServis()
            dialog.dismiss()
        }

        dialog.show()
    }



    /* Removing Options Menu as filter is now in the search bar */
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean = false
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean = super.onOptionsItemSelected(item)


    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedCal = Calendar.getInstance()
                selectedCal.set(year, month, day)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                onDateSelected(sdf.format(selectedCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }





}