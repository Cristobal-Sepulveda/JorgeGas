package com.example.conductor.ui.vistageneral

import android.os.Bundle
import android.view.*
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import org.koin.android.ext.android.inject

class VistaGeneralFragment : BaseFragment() {

    private var _binding: FragmentVistaGeneralBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)

        return _binding!!.root
    }

}