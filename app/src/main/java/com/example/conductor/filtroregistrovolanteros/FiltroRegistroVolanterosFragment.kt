package com.example.conductor.filtroregistrovolanteros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentFiltroRegistroVolanterosBinding
import org.koin.android.ext.android.inject

class FiltroRegistroVolanterosFragment: BaseFragment() {
    override val _viewModel: FiltroRegistroVolanterosViewModel by inject()
    private var _binding: FragmentFiltroRegistroVolanterosBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFiltroRegistroVolanterosBinding.inflate(inflater, container, false)

        return _binding!!.root
    }
}