package com.example.conductor.ui.asistencia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import org.koin.android.ext.android.inject

class AsistenciaFragment: BaseFragment() {
    private var _binding: FragmentAsistenciaBinding? = null
    override val _viewModel: AsistenciaViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAsistenciaBinding.inflate(inflater,container,false)
        return _binding!!.root
    }

}