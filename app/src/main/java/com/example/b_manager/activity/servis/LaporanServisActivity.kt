package com.example.b_manager.activity.servis

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import com.example.b_manager.utils.DialogUtils
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.b_manager.adapter.ServisAdapter
import com.example.b_manager.databinding.ActivityLaporanServisBinding
import com.example.b_manager.utils.PdfGenerator
import com.example.b_manager.viewmodel.ServisViewModel
import com.google.android.material.chip.Chip
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LaporanServisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLaporanServisBinding
    private val viewModel: ServisViewModel by viewModels()
    private lateinit var adapter: ServisAdapter

    private var dariTanggal: String? = null
    private var sampaiTanggal: String? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanServisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupChipFilters()
        setupCustomFilter()
        setupExportButtons()
        setupFAB()
        observeData()

        // Load data awal
        viewModel.loadServis()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = ServisAdapter(
            onItemClick = {},
            onEditClick = {},
            onDeleteClick = {}
        )
        binding.rvServis.layoutManager = LinearLayoutManager(this)
        binding.rvServis.adapter = adapter
    }

    private fun setupChipFilters() {
        // Chip: Hari Ini
        binding.chipHariIni.setOnClickListener {
            if ((it as Chip).isChecked) {
                filterHariIni()
                binding.cardCustomFilter.visibility = View.GONE
            }
        }

        // Chip: Minggu Ini
        binding.chipMingguIni.setOnClickListener {
            if ((it as Chip).isChecked) {
                filterMingguIni()
                binding.cardCustomFilter.visibility = View.GONE
            }
        }

        // Chip: Bulan Ini
        binding.chipBulanIni.setOnClickListener {
            if ((it as Chip).isChecked) {
                filterBulanIni()
                binding.cardCustomFilter.visibility = View.GONE
            }
        }

        // Chip: Custom
        binding.chipCustom.setOnClickListener {
            if ((it as Chip).isChecked) {
                binding.cardCustomFilter.visibility = View.VISIBLE
            } else {
                binding.cardCustomFilter.visibility = View.GONE
            }
        }
    }

    private fun setupCustomFilter() {
        // Setup Status AutoCompleteTextView
        val statusList = arrayOf("Semua", "proses", "selesai", "batal")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusList)
        binding.actvStatus.setAdapter(statusAdapter)
        binding.actvStatus.setText("Semua", false)

        // Date Picker untuk Dari Tanggal
        binding.etDariTanggal.setOnClickListener {
            showDatePicker { date ->
                dariTanggal = date
                try {
                    val parsedDate = dateFormat.parse(date)
                    binding.etDariTanggal.setText(parsedDate?.let { displayDateFormat.format(it) })
                } catch (e: Exception) {
                    binding.etDariTanggal.setText(date)
                }
            }
        }

        // Date Picker untuk Sampai Tanggal
        binding.etSampaiTanggal.setOnClickListener {
            showDatePicker { date ->
                sampaiTanggal = date
                try {
                    val parsedDate = dateFormat.parse(date)
                    binding.etSampaiTanggal.setText(parsedDate?.let { displayDateFormat.format(it) })
                } catch (e: Exception) {
                    binding.etSampaiTanggal.setText(date)
                }
            }
        }

        // Tombol Reset
        binding.btnReset.setOnClickListener {
            dariTanggal = null
            sampaiTanggal = null
            binding.etDariTanggal.text?.clear()
            binding.etSampaiTanggal.text?.clear()
            binding.actvStatus.setText("Semua", false)
            binding.chipGroupFilter.clearCheck()
            binding.cardCustomFilter.visibility = View.GONE
            binding.tvPeriode.text = "📅 Periode: Semua Data"
            viewModel.loadServis()
        }

        // Tombol Terapkan
        binding.btnTerapkan.setOnClickListener {
            val status = binding.actvStatus.text.toString()
            val statusFilter = if (status == "Semua") null else status

            if (dariTanggal != null && sampaiTanggal != null) {
                viewModel.filterServis(
                    nama = null,
                    status = statusFilter,
                    tanggalDari = dariTanggal,
                    tanggalSampai = sampaiTanggal
                )
                updatePeriodeText(dariTanggal!!, sampaiTanggal!!)
            } else {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Filter Tidak Lengkap",
                    message = "Harap pilih tanggal dari dan sampai untuk menerapkan filter custom.",
                    type = "info"
                )
            }
        }
    }

    private fun setupExportButtons() {
        binding.btnExportPDF.setOnClickListener {
            checkPermissionAndExportPdf()
        }
    }

    private fun checkPermissionAndExportPdf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ tidak perlu permission
            exportToPdf()
        } else {
            // Android 9 ke bawah perlu permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                exportToPdf()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToPdf()
            } else {
                Toast.makeText(this, "Permission ditolak", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToPdf() {
        viewModel.servisList.value?.onSuccess { list ->
            if (list.isEmpty()) {
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Data Kosong",
                    message = "Tidak ada data yang dapat di-export untuk periode ini.",
                    type = "info"
                )
                return@onSuccess
            }

            Toast.makeText(this, "Membuat PDF...", Toast.LENGTH_SHORT).show()

            val pdfGenerator = PdfGenerator(this)
            val periode = binding.tvPeriode.text.toString().replace("📅 Periode: ", "")

            pdfGenerator.generatePdf(
                servisList = list,
                periode = periode,
                onSuccess = { file ->
                    showPdfSuccessDialog(file)
                },
                onError = { error ->
                    DialogUtils.showAlertDialog(
                        context = this,
                        title = "Gagal Membuat PDF",
                        message = "Terjadi kesalahan: $error",
                        type = "error"
                    )
                }
            )
        }
    }

    private fun showPdfSuccessDialog(file: File) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("✅ PDF Berhasil Dibuat")
        builder.setMessage("File tersimpan di:\n${file.absolutePath}")
        builder.setPositiveButton("Buka") { _, _ ->
            PdfGenerator(this).openPdf(file)
        }
        builder.setNegativeButton("Share") { _, _ ->
            PdfGenerator(this).sharePdf(file)
        }
        builder.setNeutralButton("Tutup", null)
        builder.show()
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION = 100
    }

    private fun setupFAB() {
        binding.fabRefresh.setOnClickListener {
            viewModel.loadServis()
            Toast.makeText(this, "Data diperbarui", Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterHariIni() {
        val today = Calendar.getInstance()
        val todayStr = dateFormat.format(today.time)

        viewModel.filterServis(
            nama = null,
            status = null,
            tanggalDari = todayStr,
            tanggalSampai = todayStr
        )
        binding.tvPeriode.text = "📅 Periode: Hari Ini"
    }

    private fun filterMingguIni() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        val startWeek = dateFormat.format(cal.time)

        cal.add(Calendar.DAY_OF_WEEK, 6)
        val endWeek = dateFormat.format(cal.time)

        viewModel.filterServis(
            nama = null,
            status = null,
            tanggalDari = startWeek,
            tanggalSampai = endWeek
        )
        binding.tvPeriode.text = "📅 Periode: Minggu Ini"
    }

    private fun filterBulanIni() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val startMonth = dateFormat.format(cal.time)

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endMonth = dateFormat.format(cal.time)

        viewModel.filterServis(
            nama = null,
            status = null,
            tanggalDari = startMonth,
            tanggalSampai = endMonth
        )
        binding.tvPeriode.text = "📅 Periode: Bulan Ini"
    }

    private fun observeData() {
        viewModel.servisList.observe(this) { result ->
            result.onSuccess { list ->
                if (list.isEmpty()) {
                    binding.rvServis.visibility = View.GONE
                    binding.layoutEmpty.visibility = View.VISIBLE
                } else {
                    binding.rvServis.visibility = View.VISIBLE
                    binding.layoutEmpty.visibility = View.GONE
                    adapter.submitList(list)
                }

                updateStatistics(list)
            }.onFailure { error ->
                DialogUtils.showAlertDialog(
                    context = this,
                    title = "Gagal Memuat Data",
                    message = error.message ?: "Terjadi kesalahan saat memuat data laporan.",
                    type = "error"
                )
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.layoutLoading.visibility = View.VISIBLE
                binding.rvServis.visibility = View.GONE
                binding.layoutEmpty.visibility = View.GONE
            } else {
                binding.layoutLoading.visibility = View.GONE
            }
        }
    }

    private fun updateStatistics(list: List<com.example.b_manager.model.Servis>) {
        val totalBiaya = list.sumOf { it.biaya ?: 0.0 }
        val selesai = list.count { it.status.equals("selesai", true) }
        val proses = list.count { it.status.equals("proses", true) }
        val batal = list.count { it.status.equals("batal", true) }

        binding.tvJumlahServis.text = list.size.toString()
        binding.tvTotalBiaya.text = formatRupiah(totalBiaya)
        binding.tvSelesai.text = selesai.toString()
        binding.tvProses.text = proses.toString()
        binding.tvBatal.text = batal.toString()
    }

    private fun updatePeriodeText(dari: String, sampai: String) {
        try {
            val dariDate = dateFormat.parse(dari)
            val sampaiDate = dateFormat.parse(sampai)
            val dariStr = dariDate?.let { displayDateFormat.format(it) }
            val sampaiStr = sampaiDate?.let { displayDateFormat.format(it) }
            binding.tvPeriode.text = "📅 Periode: $dariStr - $sampaiStr"
        } catch (e: Exception) {
            binding.tvPeriode.text = "📅 Periode: $dari - $sampai"
        }
    }

    private fun showDatePicker(onSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                onSelected(dateFormat.format(cal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatRupiah(value: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(value)
    }
}