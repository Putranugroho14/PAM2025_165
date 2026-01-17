package com.example.b_manager.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.b_manager.R
import com.example.b_manager.activity.pelanggan.PelangganListActivity
import com.example.b_manager.activity.mobil.MobilListActivity
import com.example.b_manager.activity.servis.LaporanServisActivity
import com.example.b_manager.activity.servis.ServisListActivity
import com.example.b_manager.utils.RetrofitClient
import com.example.b_manager.databinding.ActivityMainBinding
import com.example.b_manager.utils.SessionManager
import com.example.b_manager.utils.DialogUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        setupUI()
        setupListeners()
        setupLineChart()
        loadStatistics()
    }

    private fun setupUI() {
        val admin = sessionManager.getAdminData()
        binding.tvAdminName.text = admin?.namaLengkap ?: "Admin Bengkel"
    }

    private fun setupListeners() {
        // 1. Custom Bottom Navigation Logic
        binding.btnNavPelanggan.setOnClickListener {
            startActivity(Intent(this, PelangganListActivity::class.java))
        }

        binding.btnNavMobil.setOnClickListener {
            startActivity(Intent(this, MobilListActivity::class.java))
        }

        binding.btnNavServis.setOnClickListener {
            startActivity(Intent(this, ServisListActivity::class.java))
        }

        binding.btnNavLaporan.setOnClickListener {
            startActivity(Intent(this, LaporanServisActivity::class.java))
        }

        // 2. Center Search Logic (BottomSheet)
        binding.btnNavSearch.setOnClickListener {
            showSearchBottomSheet()
        }

        // 3. Logout (Now in Top Bar)
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showSearchBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_search, null)
        bottomSheetDialog.setContentView(view)

        // Bind clicks in bottom sheet
        view.findViewById<View>(R.id.btn_search_pelanggan).setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, PelangganListActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_search_mobil).setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, MobilListActivity::class.java))
        }

        view.findViewById<View>(R.id.btn_search_servis).setOnClickListener {
            bottomSheetDialog.dismiss()
            startActivity(Intent(this, ServisListActivity::class.java))
        }

        bottomSheetDialog.show()
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                val apiService = RetrofitClient.apiService
                loadTotalPelanggan(apiService)
                loadTotalMobil(apiService)
                loadServisBulanIni(apiService)
                loadTotalPendapatanHariIni(apiService)
                loadChartData(apiService) // Load data untuk grafik
            } catch (e: Exception) {
                Log.e(TAG, "Error loading statistics", e)
            }
        }
    }

    private suspend fun loadTotalPelanggan(apiService: com.example.b_manager.api.ApiService) {
        try {
            val response = apiService.getPelanggan()
            if (response.isSuccessful) {
                val total = response.body()?.data?.size ?: 0
                binding.tvTotalPelanggan.text = total.toString()
            }
        } catch (e: Exception) {}
    }

    private suspend fun loadTotalMobil(apiService: com.example.b_manager.api.ApiService) {
        try {
            val response = apiService.getMobil()
            if (response.isSuccessful) {
                val total = response.body()?.data?.size ?: 0
                binding.tvTotalMobil.text = total.toString()
            }
        } catch (e: Exception) {}
    }

    private suspend fun loadServisBulanIni(apiService: com.example.b_manager.api.ApiService) {
        try {
            val response = apiService.getServis()
            if (response.isSuccessful) {
                val allServis = response.body()?.data ?: emptyList()
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                
                var count = 0
                for (servis in allServis) {
                    if (isCurrentMonth(servis.tanggalServis, currentMonth, currentYear)) count++
                }
                binding.tvServisBulanIni.text = count.toString()
            }
        } catch (e: Exception) {}
    }

    private suspend fun loadTotalPendapatanHariIni(apiService: com.example.b_manager.api.ApiService) {
        try {
            val response = apiService.getServis()
            if (response.isSuccessful) {
                val allServis = response.body()?.data ?: emptyList()
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                var totalPendapatan = 0.0
                for (servis in allServis) {
                    if (servis.tanggalServis.startsWith(todayStr)) {
                         totalPendapatan += servis.biaya
                    }
                }
                val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                val formattedPrice = format.format(totalPendapatan).replace("Rp", "Rp ").replace(",00", "")
                binding.tvRevenueToday.text = formattedPrice
            }
        } catch (e: Exception) {
             Log.e(TAG, "Error calculating revenue", e)
        }
    }

    private fun setupLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)

            axisRight.isEnabled = false
            
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                textColor = Color.parseColor("#94A3B8")
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F1F5F9")
                textColor = Color.parseColor("#94A3B8")
            }

            legend.isEnabled = false
        }
    }

    private suspend fun loadChartData(apiService: com.example.b_manager.api.ApiService) {
        try {
            val response = apiService.getServis()
            if (response.isSuccessful) {
                val allServis = response.body()?.data ?: emptyList()
                
                // Grouping data by last 7 days
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val displaySdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                val calendar = Calendar.getInstance()
                
                val last7Days = mutableListOf<String>()
                val last7DaysDisplay = mutableListOf<String>()
                
                for (i in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    last7Days.add(sdf.format(cal.time))
                    last7DaysDisplay.add(displaySdf.format(cal.time))
                }

                val entries = mutableListOf<Entry>()
                
                last7Days.forEachIndexed { index, dateStr ->
                    val dailyTotal = allServis
                        .filter { it.tanggalServis.startsWith(dateStr) }
                        .sumOf { it.biaya }
                    entries.add(Entry(index.toFloat(), dailyTotal.toFloat()))
                }

                val dataSet = LineDataSet(entries, "Pendapatan")
                dataSet.apply {
                        color = Color.parseColor("#6366F1")
                        valueTextColor = Color.parseColor("#6366F1")
                        lineWidth = 3f
                        setDrawCircles(true)
                        setCircleColor(Color.parseColor("#6366F1"))
                        circleRadius = 5f
                        setDrawCircleHole(true)
                        circleHoleRadius = 3f
                        setDrawFilled(true)
                        fillColor = Color.parseColor("#6366F1")
                        fillAlpha = 30
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                        valueTextSize = 10f
                    }

                    binding.lineChart.apply {
                        xAxis.valueFormatter = IndexAxisValueFormatter(last7DaysDisplay)
                        data = LineData(dataSet)
                        animateY(1000)
                        invalidate()
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading chart data", e)
        }
    }

    private fun isCurrentMonth(tanggal: String, targetMonth: Int, targetYear: Int): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(tanggal)
            if (date != null) {
                val cal = Calendar.getInstance()
                cal.time = date
                cal.get(Calendar.MONTH) + 1 == targetMonth && cal.get(Calendar.YEAR) == targetYear
            } else false
        } catch (e: Exception) { false }
    }

    override fun onResume() {
        super.onResume()
        loadStatistics()
    }

    private fun showLogoutDialog() {
        DialogUtils.showConfirmationDialog(
            context = this,
            title = "Konfirmasi Logout",
            message = "Apakah Anda yakin ingin keluar dari aplikasi?",
            positiveText = "Ya, Keluar",
            negativeText = "Batal",
            type = "warning",
            onPositive = {
                sessionManager.logout()
                navigateToLogin()
            }
        )
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}