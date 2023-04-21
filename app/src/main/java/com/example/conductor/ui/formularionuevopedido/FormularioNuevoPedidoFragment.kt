package com.example.conductor.ui.formularionuevopedido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.databinding.FragmentFormularioNuevoPedidoBinding
import com.example.conductor.ui.base.BaseFragment
import org.koin.android.ext.android.inject

class FormularioNuevoPedidoFragment: BaseFragment() {

    private var _binding: FragmentFormularioNuevoPedidoBinding? = null
    override val _viewModel: FormularioNuevoPedidoViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View{

        _binding = FragmentFormularioNuevoPedidoBinding.inflate(inflater, container, false)

        return _binding!!.root
    }
}