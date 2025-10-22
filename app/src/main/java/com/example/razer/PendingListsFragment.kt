package com.example.razer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.razer.databinding.FragmentPendingListsBinding // Asegúrate de que este es tu paquete

class PendingListsFragment : Fragment() {

    // Variable para manejar el View Binding
    private var _binding: FragmentPendingListsBinding? = null
    private val binding get() = _binding!! // Acceso seguro al binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inicializa el View Binding
        _binding = FragmentPendingListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configuración inicial del RecyclerView
        binding.recyclerViewPendingLists.layoutManager = LinearLayoutManager(context)

        // TODO: Enlazar el adaptador aquí (en un paso posterior)

        // Implementar el clic del FAB para navegar a la pantalla de agregar ítem
        binding.fabAddList.setOnClickListener {
            // TODO: Lógica de navegación a AddItemFragment (Mockup 3)
            // findNavController().navigate(R.id.action_pendingListsFragment_to_addItemFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Limpia la referencia de binding para evitar fugas de memoria
        _binding = null
    }
}