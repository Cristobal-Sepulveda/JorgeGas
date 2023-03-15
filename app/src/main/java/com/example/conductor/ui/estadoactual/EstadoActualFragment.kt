package com.example.conductor.ui.estadoactual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentEstadoActualBinding
import com.example.conductor.databinding.FragmentMapBinding
import org.koin.android.ext.android.inject

class EstadoActualFragment: BaseFragment() {
    override val _viewModel: EstadoActualViewModel by inject()
    private var _binding: FragmentEstadoActualBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentEstadoActualBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        return _binding!!.root
    }
}