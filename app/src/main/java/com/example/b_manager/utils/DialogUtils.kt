package com.example.b_manager.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.content.res.ColorStateList
import com.example.b_manager.R

object DialogUtils {

    /**
     * Show custom confirmation dialog
     * @param type: "warning", "danger", "success", "info"
     */
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String = "Ya",
        negativeText: String = "Tidak",
        type: String = "warning",
        onPositive: () -> Unit,
        onNegative: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_confirmation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val ivIcon = dialog.findViewById<ImageView>(R.id.iv_dialog_icon)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_dialog_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_dialog_message)
        val btnPositive = dialog.findViewById<Button>(R.id.btn_dialog_positive)
        val btnNegative = dialog.findViewById<Button>(R.id.btn_dialog_negative)

        // Set icon and color based on type
        when (type) {
            "danger" -> {
                ivIcon.setImageResource(R.drawable.ic_delete)
                ivIcon.setColorFilter(context.getColor(R.color.error))
                btnPositive.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.error))
            }
            "warning" -> {
                ivIcon.setImageResource(R.drawable.ic_logout)
                ivIcon.setColorFilter(context.getColor(R.color.warning))
                btnPositive.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.warning))
            }
            "success" -> {
                ivIcon.setImageResource(R.drawable.ic_check)
                ivIcon.setColorFilter(context.getColor(R.color.success))
                btnPositive.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.success))
            }
            else -> { // info
                ivIcon.setImageResource(R.drawable.ic_info)
                ivIcon.setColorFilter(context.getColor(R.color.primary))
                btnPositive.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.primary))
            }
        }

        tvTitle.text = title
        tvMessage.text = message
        btnPositive.text = positiveText
        btnNegative.text = negativeText

        btnPositive.setOnClickListener {
            onPositive()
            dialog.dismiss()
        }

        btnNegative.setOnClickListener {
            onNegative?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }

    /**
     * Show simple alert dialog (OK only)
     */
    fun showAlertDialog(
        context: Context,
        title: String,
        message: String,
        type: String = "info",
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_alert)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val ivIcon = dialog.findViewById<ImageView>(R.id.iv_alert_icon)
        val tvTitle = dialog.findViewById<TextView>(R.id.tv_alert_title)
        val tvMessage = dialog.findViewById<TextView>(R.id.tv_alert_message)
        val btnOk = dialog.findViewById<Button>(R.id.btn_alert_ok)

        // Set icon based on type
        when (type) {
            "success" -> {
                ivIcon.setImageResource(R.drawable.ic_check)
                ivIcon.setColorFilter(context.getColor(R.color.success))
            }
            "error" -> {
                ivIcon.setImageResource(R.drawable.ic_delete)
                ivIcon.setColorFilter(context.getColor(R.color.error))
            }
            "warning" -> {
                ivIcon.setImageResource(R.drawable.ic_logout)
                ivIcon.setColorFilter(context.getColor(R.color.warning))
            }
            else -> {
                ivIcon.setImageResource(R.drawable.ic_info)
                ivIcon.setColorFilter(context.getColor(R.color.primary))
            }
        }

        tvTitle.text = title
        tvMessage.text = message

        btnOk.setOnClickListener {
            onDismiss?.invoke()
            dialog.dismiss()
        }

        dialog.show()
    }
}
