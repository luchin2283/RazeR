package com.example.razer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.razer.adapter.TabsAdapter
import com.example.razer.databinding.FragmentListsContainerBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ListsContainerFragment : Fragment() {

    private var _binding: FragmentListsContainerBinding? = null
    private val binding get() = _binding!!

    private lateinit var tabsAdapter: TabsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabsAdapter = TabsAdapter(this)
        tabsAdapter.addFragment(ListsContentFragment(), "PENDIENTES")
        tabsAdapter.addFragment(PastListsFragment(), "PASADAS")
        binding.viewPager.adapter = tabsAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabsAdapter.getPageTitle(position)
        }.attach()

        binding.fabAddList.setOnClickListener {
            val action = ListsContainerFragmentDirections.actionListsContainerToAddItemFragment()
            findNavController().navigate(action)
        }

        binding.buttonLogout.setOnClickListener {
            Firebase.auth.signOut()
            val action = NavGraphDirections.actionGlobalToLoginFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}