package com.example.b_manager.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.b_manager.databinding.ItemPelangganBinding
import com.example.b_manager.model.Pelanggan

class PelangganAdapter(
    private val onItemClick: (Pelanggan) -> Unit,
    private val onEditClick: (Pelanggan) -> Unit,
    private val onDeleteClick: (Pelanggan) -> Unit
) : ListAdapter<Pelanggan, PelangganAdapter.ViewHolder>(PelangganDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPelangganBinding.inflate(
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
        private val binding: ItemPelangganBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(pelanggan: Pelanggan) {
            binding.apply {
                tvNamaPelanggan.text = pelanggan.namaPelanggan
                tvNomorHP.text = pelanggan.noHp
                tvAlamat.text = pelanggan.alamat ?: "-"

                root.setOnClickListener { onItemClick(pelanggan) }
                btnEdit.setOnClickListener { onEditClick(pelanggan) }
                btnDelete.setOnClickListener { onDeleteClick(pelanggan) }
            }
        }
    }

    private class PelangganDiffCallback : DiffUtil.ItemCallback<Pelanggan>() {
        override fun areItemsTheSame(oldItem: Pelanggan, newItem: Pelanggan): Boolean {
            return oldItem.idPelanggan == newItem.idPelanggan
        }

        override fun areContentsTheSame(oldItem: Pelanggan, newItem: Pelanggan): Boolean {
            return oldItem == newItem
        }
    }
}