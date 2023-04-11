package com.example.conductor.ui.registrodeasistencia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.databinding.FragmentRegistroDeAsistenciaBinding
import com.example.conductor.ui.asistencia.AsistenciaViewModel
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.ui.base.BaseViewModel
import org.koin.android.ext.android.inject

class RegistroDeAsistenciaFragment: BaseFragment() {

    private var _binding: FragmentRegistroDeAsistenciaBinding? = null
    override val _viewModel: RegistroDeAsistenciaViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentRegistroDeAsistenciaBinding.inflate(inflater, container, false)

        return _binding!!.root
    }
}