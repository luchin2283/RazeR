package com.example.razer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.razer.R
import com.example.razer.model.ListaItem

class ListDetailAdapter(
    private var items: List<ListaItem>,
    private val isEditable: Boolean,
    private val onCheckChanged: (item: ListaItem, isChecked: Boolean) -> Unit,
    private val onDeleteClicked: (item: ListaItem) -> Unit // ⬅️ ¡NUEVO CALLBACK!
) : RecyclerView.Adapter<ListDetailAdapter.DetailViewHolder>() {

    inner class DetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemName: TextView = itemView.findViewById(R.id.tvItemName)
        val itemQuantity: TextView = itemView.findViewById(R.id.tvItemQuantity)
        val checkBox: CheckBox = itemView.findViewById(R.id.cbItemCompleted)
        val deleteButton: ImageButton = itemView.findViewById(R.id.btnDeleteItem) // ⬅️ ¡VISTA AÑADIDA!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_detail, parent, false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val item = items[position]

        holder.itemName.text = item.nombre
        holder.itemQuantity.text = item.cantidad

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.completado

        if (isEditable) {
            holder.checkBox.isEnabled = true
            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                onCheckChanged(item, isChecked)
            }

            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                onDeleteClicked(item)
            }
        } else {
            holder.checkBox.isEnabled = false
            holder.deleteButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<ListaItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}