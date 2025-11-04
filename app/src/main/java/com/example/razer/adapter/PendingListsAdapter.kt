package com.example.razer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.razer.databinding.ItemListCardBinding
import com.example.razer.model.Lista


class PendingListsAdapter(
    private val clickListener: (Lista) -> Unit,
    private val longClickListener: (Lista) -> Boolean
) : ListAdapter<Lista, PendingListsAdapter.ListViewHolder>(PendingListDiffCallback()) {

    inner class ListViewHolder(private val binding: ItemListCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lista: Lista) {
            binding.textListName.text = lista.title
            binding.textItemCount.text = "${lista.items.size} ítems"

            binding.textLastUpdated.visibility = View.GONE

            binding.root.setOnClickListener {
                clickListener(lista)
            }

            binding.root.setOnLongClickListener {
                longClickListener(lista)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentList = getItem(position)
        holder.bind(currentList)
    }
}

class PendingListDiffCallback : DiffUtil.ItemCallback<Lista>() {
    override fun areItemsTheSame(oldItem: Lista, newItem: Lista): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Lista, newItem: Lista): Boolean {
        return oldItem == newItem
    }
}