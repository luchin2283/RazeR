package com.example.razer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.razer.adapter.ListDetailAdapter
import com.example.razer.databinding.FragmentListDetailBinding
import com.example.razer.model.Lista
import com.example.razer.model.ListaItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListDetailFragment : Fragment() {

    private var _binding: FragmentListDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ListDetailFragmentArgs by navArgs()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var listDetailAdapter: ListDetailAdapter

    private var loadedItems: MutableList<ListaItem> = mutableListOf()
    private lateinit var currentListId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListDetailBinding.inflate(inflater, container, false)
        firestore = Firebase.firestore
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentListId = args.listId
        Log.d("DetailFragment", "ID de lista recibido: $currentListId")

        binding.btnMarkComplete.setOnClickListener {
            markListAsPast()
        }

        binding.btnRestore.setOnClickListener {
            restoreListToPending()
        }

        binding.btnAddItemDetail.setOnClickListener {
            val itemName = binding.etItemNameDetail.text.toString().trim()
            val itemQty = binding.etItemQuantityDetail.text.toString().trim()

            if (itemName.isEmpty()) {
                Toast.makeText(context, "Escribe el nombre del ítem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val finalQty = itemQty.ifEmpty { "1" }
            val newItem = ListaItem(nombre = itemName, cantidad = finalQty, completado = false)
            loadedItems.add(newItem)
            listDetailAdapter.updateItems(loadedItems)
            updateItemsInFirestore(loadedItems)
            binding.etItemNameDetail.text?.clear()
            binding.etItemQuantityDetail.text?.clear()
            binding.etItemNameDetail.requestFocus()
        }

        loadListDetails(currentListId)
    }


    private fun loadListDetails(listId: String) {
        firestore.collection("listas").document(listId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    Log.d("DetailFragment", "Lista encontrada.")
                    val lista = document.toObject(Lista::class.java)
                    if (lista != null) {
                        binding.tvListTitle.text = lista.title
                        loadedItems = lista.items.toMutableList()

                        setupUiForStatus(lista.status)
                        setupRecyclerView(loadedItems, lista.status == "PENDIENTE")
                    }
                } else {
                    Log.w("DetailFragment", "No se encontró la lista con ID: $listId")
                    Toast.makeText(context, "Error: Lista no encontrada", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("DetailFragment", "Error al cargar la lista", e)
            }
    }

    private fun setupUiForStatus(status: String) {
        if (status == "PENDIENTE") {
            binding.llAddItemSection.visibility = View.VISIBLE
            binding.btnMarkComplete.visibility = View.VISIBLE
            binding.btnRestore.visibility = View.GONE
        } else {
            binding.llAddItemSection.visibility = View.GONE
            binding.btnMarkComplete.visibility = View.GONE
            binding.btnRestore.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView(items: MutableList<ListaItem>, isEditable: Boolean) {

        val onCheckChangeCallback: (ListaItem, Boolean) -> Unit = { item, isChecked ->
            val itemIndex = loadedItems.indexOf(item)
            if (itemIndex != -1) {
                val updatedItem = item.copy(completado = isChecked)
                loadedItems[itemIndex] = updatedItem
                updateItemsInFirestore(loadedItems)
            }
        }

        val onDeleteClickCallback: (ListaItem) -> Unit = { item ->
            loadedItems.remove(item)
            listDetailAdapter.updateItems(loadedItems)
            updateItemsInFirestore(loadedItems)
            Toast.makeText(context, "'${item.nombre}' eliminado", Toast.LENGTH_SHORT).show()
        }

        listDetailAdapter = ListDetailAdapter(
            items,
            isEditable,
            onCheckChangeCallback,
            onDeleteClickCallback
        )

        binding.rvDetailItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listDetailAdapter
        }
    }

    private fun updateItemsInFirestore(updatedItems: List<ListaItem>) {
        firestore.collection("listas").document(currentListId)
            .update("items", updatedItems)
            .addOnSuccessListener {
                Log.d("DetailFragment", "Ítems actualizados en Firestore.")
            }
            .addOnFailureListener { e ->
                Log.e("DetailFragment", "Error al actualizar ítems", e)
            }
    }

    private fun markListAsPast() {
        Log.d("DetailFragment", "Moviendo lista $currentListId a Pasadas")
        firestore.collection("listas").document(currentListId)
            .update("status", "PASADA")
            .addOnSuccessListener {
                Toast.makeText(context, "Lista movida a Pasadas", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("DetailFragment", "Error al mover lista", e)
            }
    }

    private fun restoreListToPending() {
        Log.d("DetailFragment", "Restaurando lista $currentListId a Pendientes")

        firestore.collection("listas").document(currentListId)
            .update("status", "PENDIENTE")
            .addOnSuccessListener {
                Toast.makeText(context, "Lista restaurada a Pendientes", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("DetailFragment", "Error al restaurar lista", e)
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}