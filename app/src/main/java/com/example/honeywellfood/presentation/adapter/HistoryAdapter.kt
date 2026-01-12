package com.example.honeywellfood.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.honeywellfood.databinding.ItemScanBinding
import com.example.honeywellfood.domain.model.ScanItem
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter : ListAdapter<ScanItem, HistoryAdapter.ScanViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScanViewHolder {
        val binding = ItemScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ScanViewHolder(private val binding: ItemScanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScanItem) {
            binding.tvData.text = item.data
            binding.tvSymbology.text = item.symbology
            binding.tvDate.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(item.timestamp))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScanItem>() {
        override fun areItemsTheSame(oldItem: ScanItem, newItem: ScanItem) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ScanItem, newItem: ScanItem) =
            oldItem == newItem
    }
}