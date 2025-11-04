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
import com.example.razer.databinding.FragmentListsContentBinding
import com.example.razer.model.Lista
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ListenerRegistration

class ListsContentFragment : Fragment() {

    private var _binding: FragmentListsContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var listAdapter: PendingListsAdapter

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var firestoreListener: ListenerRegistration? = null

    private val pendingLists = mutableListOf<Lista>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = Firebase.firestore
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadPendingListsFromFirestore()
    }

    private fun setupRecyclerView() {
        val onListClick: (Lista) -> Unit = { lista ->
            Log.d("NAV_DEBUG", "Navegando a detalles para lista ID: ${lista.id}")
            val action = ListsContainerFragmentDirections
                .actionListsContainerToListDetailFragment(lista.id)
            try {
                requireParentFragment().findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("NAV_DEBUG", "Error al navegar", e)
                findNavController().navigate(action)
            }
        }

        val onListLongClick: (Lista) -> Boolean = { lista ->
            AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Lista")
                .setMessage("¿Estás seguro de que deseas eliminar la lista '${lista.title}'? Esta acción no se puede deshacer.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar") { _, _ ->
                    deleteListFromFirestore(lista.id)
                }
                .show()

            true
        }

        listAdapter = PendingListsAdapter(onListClick, onListLongClick)

        binding.recyclerViewListContent.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = listAdapter
        }
    }

    private fun loadPendingListsFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("Firestore", "Usuario no autenticado.")
            Toast.makeText(context, "Error: Debes iniciar sesión.", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("Firestore", "Iniciando carga de listas pendientes para usuario: $userId")

        firestoreListener?.remove()

        firestoreListener = firestore.collection("listas")
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", "PENDIENTE")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Firestore", "Error al escuchar Firestore:", e)
                    Toast.makeText(context, "Error al cargar listas: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots == null) {
                    Log.w("Firestore", "Snapshot recibido es nulo.")
                    return@addSnapshotListener
                }

                pendingLists.clear()
                for (doc in snapshots.documents) {
                    try {
                        val lista = doc.toObject(Lista::class.java)?.copy(id = doc.id)
                        if (lista != null) {
                            pendingLists.add(lista)
                        } else {
                            Log.w("Firestore", "Documento ${doc.id} no pudo ser convertido a Lista.")
                        }
                    } catch (ex: Exception) {
                        Log.e("Firestore", "Error al convertir documento ${doc.id}: ", ex)
                    }
                }

                listAdapter.submitList(pendingLists.toList())
                Log.d("Firestore", "RecyclerView actualizado con ${pendingLists.size} listas pendientes.")

                if (pendingLists.isEmpty()) {
                    Log.d("Firestore", "No se encontraron listas pendientes.")
                }
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
        Log.d("Lifecycle", "ListsContentFragment - onDestroyView: Removiendo listener de Firestore.")
        firestoreListener?.remove()
        _binding = null
    }
}