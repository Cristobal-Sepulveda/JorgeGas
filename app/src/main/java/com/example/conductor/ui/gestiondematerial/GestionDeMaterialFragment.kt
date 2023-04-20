package com.example.conductor.ui.gestiondematerial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.adapter.VolanteroSinMaterialAdapter
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentGestionDeMaterialBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.example.conductor.ui.base.BaseFragment
import org.koin.android.ext.android.inject


class GestionDeMaterialFragment: BaseFragment() {
    private var _binding: FragmentGestionDeMaterialBinding? = null
    override val _viewModel: GestionDeMaterialViewModel by inject()
    private val _appDataSource: AppDataSource by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGestionDeMaterialBinding.inflate(inflater, container, false)
        _binding!!.viewModel = _viewModel
        _binding!!.lifecycleOwner = this

        val adapter = VolanteroSinMaterialAdapter(_viewModel,_appDataSource , VolanteroSinMaterialAdapter.OnClickListener{ usuario -> })
        _binding!!.recyclerViewGestionDeMaterialVolanterosSinMaterial.adapter = adapter

        _viewModel.displayVolanterosInRecyclerView(requireActivity())

        _viewModel.domainUsuariosInScreen.observe(requireActivity()) {
            it.let {
                adapter.submitList(it as MutableList<Usuario>)
            }
        }

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewModel.resetearHayVolanterosSinMaterial()
        _viewModel.vaciarRecyclerView()
    }
}

