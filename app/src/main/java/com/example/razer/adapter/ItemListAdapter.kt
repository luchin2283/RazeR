package com.example.razer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.razer.R
import com.example.razer.model.ListaItem


class ItemListAdapter : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private val items = mutableListOf<ListaItem>()

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)

        fun bind(item: ListaItem) {
            itemName.text = item.nombre
            itemQuantity.text = item.cantidad
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_add_list_element, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addItem(newItem: ListaItem) {
        items.add(newItem)
        notifyItemInserted(items.size - 1)
    }

    fun getItems(): List<ListaItem> {
        return items
    }
}