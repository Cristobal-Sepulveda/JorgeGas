package com.example.conductor.ui.nuevautilidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding
import com.example.conductor.databinding.FragmentNuevaUtilidadBinding

class NuevaUtilidadFragment : Fragment() {

    //use Koin to retrieve the ViewModel instance
    private var _binding: FragmentNuevaUtilidadBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNuevaUtilidadBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}