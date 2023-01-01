package com.example.conductor.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentHomeBinding
import org.koin.android.ext.android.inject

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    override val _viewModel: HomeViewModel by inject()
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("MapFragment", "HomeFragment onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        _viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

/*        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)*/
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("MapFragment", "HomeFragment onDestroyView")
        _binding = null
    }

/*    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.overflow_menu,menu )
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        return NavigationUI.onNavDestinationSelected(
            menuItem,
            requireView().findNavController()) }*/
}