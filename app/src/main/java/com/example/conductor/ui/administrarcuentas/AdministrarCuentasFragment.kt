package com.example.conductor.ui.administrarcuentas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding

class AdministrarCuentasFragment : Fragment() {

    //use Koin to retrieve the ViewModel instance
    private var _binding: FragmentAdministrarCuentasBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdministrarCuentasBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}