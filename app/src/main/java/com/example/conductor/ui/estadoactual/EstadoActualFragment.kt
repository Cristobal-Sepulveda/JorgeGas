package com.example.conductor.ui.estadoactual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.adapter.EstadoActualVolanteroAdapter
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.VolanteroYRecorrido
import com.example.conductor.databinding.FragmentEstadoActualBinding
import org.koin.android.ext.android.inject

class EstadoActualFragment: BaseFragment() {
    override val _viewModel: EstadoActualViewModel by inject()
    private var _binding: FragmentEstadoActualBinding? = null
    private val _appDataSource : AppDataSource by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        _binding = FragmentEstadoActualBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel
        val adapter = EstadoActualVolanteroAdapter(_viewModel, _appDataSource, EstadoActualVolanteroAdapter.OnClickListener{})
        _binding!!.recyclerviewEstadoActualListaDeVolanterosActivos.adapter = adapter

        _viewModel.displayUsuariosInRecyclerView()

        _viewModel.volanterosInScreen.observe(requireActivity()) {
            it.let {
                adapter.submitList(it as MutableList<VolanteroYRecorrido>)
            }
        }

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}