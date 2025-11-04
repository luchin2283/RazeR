package com.example.razer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.razer.adapter.ItemListAdapter
import com.example.razer.databinding.FragmentAddItemBinding
import com.example.razer.model.Lista
import com.example.razer.model.ListaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddItemFragment : Fragment() {

    private var _binding: FragmentAddItemBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var itemListAdapter: ItemListAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializar Firebase
        firestore = Firebase.firestore
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.buttonAddItem.setOnClickListener { addItemToList() }
        binding.buttonSaveList.setOnClickListener { saveListToFirestore() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        itemListAdapter = ItemListAdapter()
        binding.recyclerViewItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = itemListAdapter
        }
    }

    private fun addItemToList() {
        val itemName = binding.editTextItemName.text.toString().trim()
        val quantity = binding.editTextQuantity.text.toString().trim()

        if (itemName.isEmpty()) {
            Toast.makeText(context, "El nombre del ítem no puede estar vacío.", Toast.LENGTH_SHORT).show()
            return
        }
        if (quantity.isEmpty()) {
            Toast.makeText(context, "Ingresa la cantidad o unidad.", Toast.LENGTH_SHORT).show()
            return
        }

        val newItem = ListaItem(nombre = itemName, cantidad = quantity)
        itemListAdapter.addItem(newItem)

        binding.editTextItemName.text?.clear()
        binding.editTextQuantity.text?.clear()
        binding.editTextItemName.requestFocus()
    }

    private fun saveListToFirestore() {
        Log.d("SAVE_LIST", "Función saveListToFirestore iniciada.")

        val title = binding.editTextTitle.text.toString().trim()
        val items = itemListAdapter.getItems()

        if (title.isEmpty()) {
            Log.e("SAVE_LIST", "Error: El título está vacío.")
            Toast.makeText(context, "El título de la lista no puede estar vacío.", Toast.LENGTH_SHORT).show()
            binding.textInputLayoutTitle.error = "Campo requerido"
            return
        }
        binding.textInputLayoutTitle.error = null

        if (items.isEmpty()) {
            Log.e("SAVE_LIST", "Error: La lista de ítems está vacía.")
            Toast.makeText(context, "Agrega al menos un ítem a la lista.", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("SAVE_LIST", "Error: No se pudo obtener el ID de usuario.")
            Toast.makeText(context, "Error: No se pudo obtener el ID de usuario. Inicia sesión de nuevo.", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("SAVE_LIST", "Datos validados. Título: $title, Ítems: ${items.size}, UserID: $userId")

        val nuevaLista = Lista(
            title = title,
            items = items,
            userId = userId
        )

        Log.d("SAVE_LIST", "Intentando guardar en Firestore...")

        firestore.collection("listas")
            .add(nuevaLista)
            .addOnSuccessListener { documentReference ->
                Log.d("SAVE_LIST", "Éxito al guardar en Firestore. ID: ${documentReference.id}")
                Toast.makeText(context, "✅ Lista '$title' guardada.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.e("SAVE_LIST", "Error al guardar en Firestore: ${e.message}", e)
                Toast.makeText(context, "Error al guardar la lista: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}