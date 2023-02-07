package com.example.conductor.ui.gestiondevolanteros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.adapter.VolanteroAdapter
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentGestionDeVolanterosBinding
import org.koin.android.ext.android.inject


class GestionDeVolanterosFragment : BaseFragment() {
    override val _viewModel: GestionDeVolanterosViewModel by inject()
    private var _binding: FragmentGestionDeVolanterosBinding? = null
    private val _appDataSource : AppDataSource by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGestionDeVolanterosBinding.inflate(inflater, container, false)

        val adapter = VolanteroAdapter(_viewModel,_appDataSource, VolanteroAdapter.OnClickListener{

        })

        _binding!!.recyclerviewListaVolanteros.adapter = adapter
        _viewModel.displayUsuariosInRecyclerView()

        _viewModel.domainUsuariosInScreen.observe(requireActivity()) {
            it.let {
                adapter.submitList(it as MutableList<Usuario>)
            }
        }

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}