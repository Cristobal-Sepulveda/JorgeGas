package com.example.conductor.ui.administrarcuentas

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import androidx.lifecycle.Observer
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding
import com.example.conductor.ui.crearusuario.CrearUsuarioFragment
import com.example.conductor.ui.editarusuario.EditarUsuarioFragment
import org.koin.android.ext.android.inject

class AdministrarCuentasFragment : BaseFragment() {

    private var _binding: FragmentAdministrarCuentasBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /************************ Inicializando Variables del fragmento****************************/
        _binding = FragmentAdministrarCuentasBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this

        val adapter = UsuarioAdapter(UsuarioAdapter.OnClickListener{
            _viewModel.displayUsuarioDetails(it)
        })

        _binding!!.recyclerviewListaUsuarios.adapter = adapter
        _viewModel.displayUsuariosInRecyclerView()


        _viewModel.domainUsuariosInScreen.observe(requireActivity()) {
            it.let {
                adapter.submitList(it as MutableList<Usuario>)
            }
        }

        _viewModel.navigateToSelectedUsuario.observe(viewLifecycleOwner, Observer{
            if(null!=it){
                val modalBottomSheet = EditarUsuarioFragment()
                val bundle = Bundle()
                bundle.putParcelable("key",it)
                modalBottomSheet.arguments = bundle
                modalBottomSheet.show(requireActivity().supportFragmentManager, "EditarUsuarioFragment")
            }
        })

        _binding!!.buttonCrearCuenta.setOnClickListener {
            val modalBottomSheet = CrearUsuarioFragment()
            modalBottomSheet.show(requireActivity().supportFragmentManager, "CrearUsuarioFragment")
        }

        Log.i("AdministrarCuentasFragment", "onCreateView")
        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewModel.removeUsuariosInRecyclerView()
    }

}