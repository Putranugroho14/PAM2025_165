package com.example.b_manager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.b_manager.R
import com.example.b_manager.databinding.ItemServisBinding
import com.example.b_manager.model.Servis
import com.example.b_manager.utils.ValidationHelper

class ServisAdapter(
    private val onItemClick: (Servis) -> Unit,
    private val onEditClick: (Servis) -> Unit,
    private val onDeleteClick: (Servis) -> Unit
) : ListAdapter<Servis, ServisAdapter.ViewHolder>(ServisDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemServisBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemServisBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(servis: Servis) {
            binding.apply {
                tvTanggal.text = ValidationHelper.formatDate(servis.tanggalServis)
                tvPlatNomor.text = servis.platNomor ?: "-"
                tvNamaPelanggan.text = servis.namaPelanggan ?: "-"
                tvDeskripsi.text = servis.deskripsi
                tvBiaya.text = ValidationHelper.formatRupiah(servis.biaya)

                // Set status dengan warna
                tvStatus.text = servis.status.uppercase()
                when {
                    servis.status.equals("proses", true) -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_status_proses)
                    }
                    servis.status.equals("selesai", true) -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
                    }
                    servis.status.equals("batal", true) -> {
                        tvStatus.setBackgroundResource(R.drawable.bg_status_batal)
                    }
                }

                // Explicitly set click listener on root and ensure it's clickable
                root.isClickable = true
                root.isFocusable = true
                root.setOnClickListener { 
                    onItemClick(servis) 
                }
                
                btnEdit.setOnClickListener { onEditClick(servis) }
                btnDelete.setOnClickListener { onDeleteClick(servis) }
            }
        }
    }

    private class ServisDiffCallback : DiffUtil.ItemCallback<Servis>() {
        override fun areItemsTheSame(oldItem: Servis, newItem: Servis): Boolean {
            return oldItem.idServis == newItem.idServis
        }

        override fun areContentsTheSame(oldItem: Servis, newItem: Servis): Boolean {
            return oldItem == newItem
        }
    }
}