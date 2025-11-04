package com.example.razer

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.razer.adapter.PendingListsAdapter
import com.example.razer.databinding.FragmentPastListsBinding
import com.example.razer.model.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PastListsFragment : Fragment() {

    private var _binding: FragmentPastListsBinding? = null
    private val binding get() = _binding!!

    private lateinit var pastListsAdapter: PendingListsAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private val pastLists = mutableListOf<Lista>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPastListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPastListsFromFirestore()
    }

    private fun setupRecyclerView() {

        // --- 1. Lógica de Clic Normal (Navegar) ---
        val onListClick: (Lista) -> Unit = { lista ->
            Log.d("NAV_DEBUG", "Navegando a detalles (Pasadas) para lista ID: ${lista.id}")
            val action = ListsContainerFragmentDirections
                .actionListsContainerToListDetailFragment(lista.id)
            try {
                requireParentFragment().findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("NAV_DEBUG", "Error al navegar desde PastLists", e)
                findNavController().navigate(action)
            }
        }

        val onListLongClick: (Lista) -> Boolean = { lista ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Lista")
                .setMessage("¿Estás seguro de que deseas eliminar la lista '${lista.title}'?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteListFromFirestore(lista.id)
                }
                .show()

            true
        }

        pastListsAdapter = PendingListsAdapter(onListClick, onListLongClick)

        binding.recyclerViewPastLists.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = pastListsAdapter
        }
    }

    private fun loadPastListsFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        Log.d("Firestore", "Cargando listas pasadas para el usuario: $userId")

        firestore.collection("listas")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "PASADA") // <-- DEBE SER "PASADA"
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Error al cargar listas pasadas.", e)
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.w("Firestore", "Snapshot (pasadas) recibido es nulo.")
                    return@addSnapshotListener
                }

                pastLists.clear()
                for (doc in snapshots.documents) {
                    try {
                        val lista = doc.toObject(Lista::class.java)?.copy(id = doc.id)
                        if (lista != null) {
                            pastLists.add(lista)
                        } else {
                            Log.w("Firestore", "Documento (pasada) ${doc.id} no pudo ser convertido a Lista.")
                        }
                    } catch (ex: Exception) {
                        Log.e("Firestore", "Error al convertir documento (pasada): ${doc.id}", ex)
                    }
                }

                pastListsAdapter.submitList(pastLists.toList())
                Log.d("Firestore", "RecyclerView actualizado con ${pastLists.size} listas pasadas.")
            }
    }

    private fun deleteListFromFirestore(listId: String) {
        if (listId.isEmpty()) {
            Log.e("Firestore", "ID de lista vacío, no se puede eliminar.")
            return
        }

        Log.d("Firestore", "Intentando eliminar lista con ID: $listId")
        firestore.collection("listas").document(listId)
            .delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Lista $listId eliminada exitosamente.")
                Toast.makeText(context, "Lista eliminada.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al eliminar la lista $listId", e)
                Toast.makeText(context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}