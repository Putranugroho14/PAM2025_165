package com.example.b_manager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.b_manager.databinding.ItemMobilBinding
import com.example.b_manager.model.Mobil

class MobilAdapter(
    private val onItemClick: (Mobil) -> Unit,
    private val onEditClick: (Mobil) -> Unit,
    private val onDeleteClick: (Mobil) -> Unit
) : ListAdapter<Mobil, MobilAdapter.ViewHolder>(MobilDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMobilBinding.inflate(
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
        private val binding: ItemMobilBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mobil: Mobil) {
            binding.apply {
                tvPlatNomor.text = mobil.platNomor
                tvMerek.text = mobil.merek
                tvNamaPelanggan.text = mobil.namaPelanggan ?: "-"

                root.setOnClickListener { onItemClick(mobil) }
                btnEdit.setOnClickListener { onEditClick(mobil) }
                btnDelete.setOnClickListener { onDeleteClick(mobil) }
            }
        }
    }

    private class MobilDiffCallback : DiffUtil.ItemCallback<Mobil>() {
        override fun areItemsTheSame(oldItem: Mobil, newItem: Mobil): Boolean {
            return oldItem.idMobil == newItem.idMobil
        }

        override fun areContentsTheSame(oldItem: Mobil, newItem: Mobil): Boolean {
            return oldItem == newItem
        }
    }
}