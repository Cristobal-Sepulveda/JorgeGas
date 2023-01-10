package com.example.conductor.ui.administrarcuentas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.android.inject

class AdministrarCuentasFragment : BaseFragment() {

    private var _binding: FragmentAdministrarCuentasBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()
    val cloudDB = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdministrarCuentasBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this

        _binding!!.recyclerviewListaUsuarios.adapter = UsuarioAdapter(
            UsuarioAdapter.OnClickListener{
                _viewModel.displayUsuarioDetails(it)
            }
        )
/*        _viewModel.navigateToSelectedUsuario.observe(viewLifecycleOwner, Observer {
            if (null != it){
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        })*/
        return _binding!!.root
    }


}