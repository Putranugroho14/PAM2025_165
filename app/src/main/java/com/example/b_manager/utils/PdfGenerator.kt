package com.example.b_manager.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.b_manager.model.Servis
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class PdfGenerator(private val context: Context) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
    private val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    // Warna
    private val colorPrimary = Color.parseColor("#667eea")
    private val colorAccent = Color.parseColor("#764ba2")
    private val colorGreen = Color.parseColor("#38ef7d")
    private val colorOrange = Color.parseColor("#f5576c")
    private val colorBlue = Color.parseColor("#00f2fe")
    private val colorGray = Color.parseColor("#666666")
    private val colorLightGray = Color.parseColor("#f8f9fa")

    fun generatePdf(
        servisList: List<Servis>,
        periode: String,
        onSuccess: (File) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Create PDF Document
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            var yPos = 40f

            // HEADER
            yPos = drawHeader(canvas, periode, yPos)

            // INFO BOX
            yPos = drawInfoBox(canvas, yPos)

            // STATISTICS
            yPos = drawStatistics(canvas, servisList, yPos)

            // TABLE
            yPos = drawTable(canvas, servisList, yPos)

            // SUMMARY
            drawSummary(canvas, servisList, yPos)

            // FOOTER
            drawFooter(canvas)

            pdfDocument.finishPage(page)

            // Save PDF
            val fileName = "Laporan_Servis_${System.currentTimeMillis()}.pdf"
            val file = savePdfToFile(pdfDocument, fileName)
            pdfDocument.close()

            if (file != null) {
                onSuccess(file)
            } else {
                onError("Gagal menyimpan PDF")
            }

        } catch (e: Exception) {
            e.printStackTrace()
            onError("Error: ${e.message}")
        }
    }

    private fun drawHeader(canvas: Canvas, periode: String, startY: Float): Float {
        var y = startY

        // Title
        val titlePaint = Paint().apply {
            color = colorPrimary
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("📊 LAPORAN SERVIS", 40f, y, titlePaint)
        y += 25f

        // Periode
        val subtitlePaint = Paint().apply {
            color = colorGray
            textSize = 12f
        }
        canvas.drawText("Periode: $periode", 40f, y, subtitlePaint)

        // Company name (right side)
        val companyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("B-MANAGER", 555f, startY, companyPaint)

        val taglinePaint = Paint().apply {
            color = colorGray
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Sistem Manajemen Bengkel", 555f, startY + 15f, taglinePaint)

        y += 20f

        // Line
        val linePaint = Paint().apply {
            color = colorPrimary
            strokeWidth = 3f
        }
        canvas.drawLine(40f, y, 555f, y, linePaint)

        return y + 20f
    }

    private fun drawInfoBox(canvas: Canvas, startY: Float): Float {
        val boxHeight = 80f
        val boxPaint = Paint().apply {
            color = colorLightGray
            style = Paint.Style.FILL
        }

        // Draw box with rounded corners
        val rect = RectF(40f, startY, 555f, startY + boxHeight)
        canvas.drawRoundRect(rect, 8f, 8f, boxPaint)

        val labelPaint = Paint().apply {
            color = colorGray
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val valuePaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Column 1
        canvas.drawText("TANGGAL CETAK", 60f, startY + 20f, labelPaint)
        canvas.drawText(dateTimeFormat.format(Date()), 60f, startY + 35f, valuePaint)

        canvas.drawText("PERIODE LAPORAN", 60f, startY + 55f, labelPaint)
        canvas.drawText(dateFormat.format(Date()), 60f, startY + 70f, valuePaint)

        // Column 2
        canvas.drawText("DICETAK OLEH", 320f, startY + 20f, labelPaint)
        canvas.drawText("Admin Bengkel", 320f, startY + 35f, valuePaint)

        canvas.drawText("STATUS FILTER", 320f, startY + 55f, labelPaint)
        canvas.drawText("Semua Status", 320f, startY + 70f, valuePaint)

        return startY + boxHeight + 20f
    }

    private fun drawStatistics(canvas: Canvas, servisList: List<Servis>, startY: Float): Float {
        val totalServis = servisList.size
        val selesai = servisList.count { it.status.equals("selesai", true) }
        val proses = servisList.count { it.status.equals("proses", true) }
        val batal = servisList.count { it.status.equals("batal", true) }

        val cardWidth = 120f
        val cardHeight = 60f
        val gap = 10f

        val stats = listOf(
            Triple("Total Servis", totalServis.toString(), colorPrimary),
            Triple("Selesai", selesai.toString(), colorGreen),
            Triple("Proses", proses.toString(), colorOrange),
            Triple("Batal", batal.toString(), colorBlue)
        )

        stats.forEachIndexed { index, (label, value, color) ->
            val x = 40f + (cardWidth + gap) * index

            // Draw card
            val cardPaint = Paint().apply {
                this.color = color
                style = Paint.Style.FILL
            }
            val rect = RectF(x, startY, x + cardWidth, startY + cardHeight)
            canvas.drawRoundRect(rect, 8f, 8f, cardPaint)

            // Draw label
            val labelPaint = Paint().apply {
                this.color = Color.WHITE
                textSize = 9f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(label.uppercase(), x + cardWidth / 2, startY + 20f, labelPaint)

            // Draw value
            val valuePaint = Paint().apply {
                this.color = Color.WHITE
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(value, x + cardWidth / 2, startY + 45f, valuePaint)
        }

        return startY + cardHeight + 20f
    }

    private fun drawTable(canvas: Canvas, servisList: List<Servis>, startY: Float): Float {
        var y = startY

        // Section title
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Detail Servis", 40f, y, titlePaint)
        y += 5f

        // Line under title
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 2f
        }
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 15f

        // Table header
        val headerPaint = Paint().apply {
            color = colorPrimary
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, y, 555f, y + 25f, headerPaint)

        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Header columns
        canvas.drawText("No", 50f, y + 17f, headerTextPaint)
        canvas.drawText("Tanggal", 75f, y + 17f, headerTextPaint)
        canvas.drawText("Plat Nomor", 140f, y + 17f, headerTextPaint)
        canvas.drawText("Merek", 215f, y + 17f, headerTextPaint)
        canvas.drawText("Deskripsi", 290f, y + 17f, headerTextPaint)
        canvas.drawText("Biaya", 420f, y + 17f, headerTextPaint)
        canvas.drawText("Status", 505f, y + 17f, headerTextPaint)

        y += 25f

        // Table rows
        val rowPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
        }

        servisList.take(10).forEachIndexed { index, servis ->
            // Alternate row background
            if (index % 2 == 0) {
                val bgPaint = Paint().apply {
                    color = colorLightGray
                    style = Paint.Style.FILL
                }
                canvas.drawRect(40f, y, 555f, y + 20f, bgPaint)
            }

            // Format tanggal tanpa jam
            val tanggalFormatted = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val date = inputFormat.parse(servis.tanggalServis ?: "")
                date?.let { outputFormat.format(it) } ?: servis.tanggalServis?.take(10) ?: "-"
            } catch (e: Exception) {
                servis.tanggalServis?.take(10) ?: "-"
            }

            // Row data
            canvas.drawText("${index + 1}", 50f, y + 14f, rowPaint)
            canvas.drawText(tanggalFormatted, 75f, y + 14f, rowPaint)
            canvas.drawText(servis.platNomor ?: "-", 140f, y + 14f, rowPaint)
            canvas.drawText(servis.merek ?: "-", 215f, y + 14f, rowPaint)

            // Truncate deskripsi if too long
            val deskripsi = servis.deskripsi ?: "-"
            val shortDesc = if (deskripsi.length > 18) "${deskripsi.take(18)}..." else deskripsi
            canvas.drawText(shortDesc, 290f, y + 14f, rowPaint)

            canvas.drawText(formatRupiah(servis.biaya ?: 0.0), 420f, y + 14f, rowPaint)

            // Status badge
            val statusColor = when (servis.status?.lowercase()) {
                "selesai" -> colorGreen
                "proses" -> colorOrange
                else -> Color.RED
            }
            val statusPaint = Paint().apply {
                color = statusColor
                style = Paint.Style.FILL
            }
            val statusRect = RectF(505f, y + 3f, 545f, y + 17f)
            canvas.drawRoundRect(statusRect, 8f, 8f, statusPaint)

            val statusTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(servis.status?.uppercase() ?: "?", 525f, y + 13f, statusTextPaint)

            y += 20f
        }

        return y + 10f
    }

    private fun drawSummary(canvas: Canvas, servisList: List<Servis>, startY: Float) {
        val totalBiaya = servisList.sumOf { it.biaya ?: 0.0 }

        val summaryPaint = Paint().apply {
            color = colorLightGray
            style = Paint.Style.FILL
        }
        val rect = RectF(40f, startY, 555f, startY + 60f)
        canvas.drawRoundRect(rect, 8f, 8f, summaryPaint)

        // Left text
        val leftPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
        }
        canvas.drawText("${servisList.size} transaksi dalam periode ini", 60f, startY + 35f, leftPaint)

        // Right text
        val labelPaint = Paint().apply {
            color = colorGray
            textSize = 10f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("TOTAL PENDAPATAN", 535f, startY + 25f, labelPaint)

        val totalPaint = Paint().apply {
            color = colorPrimary
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(formatRupiah(totalBiaya), 535f, startY + 50f, totalPaint)
    }

    private fun drawFooter(canvas: Canvas) {
        val y = 820f

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
        canvas.drawLine(40f, y - 20f, 555f, y - 20f, linePaint)

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("Dokumen ini dibuat secara otomatis oleh sistem B-Manager", 297.5f, y, footerPaint)
        canvas.drawText("Dicetak pada: ${dateTimeFormat.format(Date())} WIB", 297.5f, y + 12f, footerPaint)
        canvas.drawText("© 2026 B-Manager. All rights reserved.", 297.5f, y + 24f, footerPaint)
    }

    private fun savePdfToFile(pdfDocument: PdfDocument, fileName: String): File? {
        return try {
            // Save to Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val file = File(downloadsDir, fileName)
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatRupiah(value: Double): String {
        return rupiahFormat.format(value).replace("Rp", "Rp ")
    }

    // Helper function to open PDF
    fun openPdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Helper function to share PDF
    fun sharePdf(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Servis")
                putExtra(Intent.EXTRA_TEXT, "Terlampir laporan servis dari B-Manager")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}