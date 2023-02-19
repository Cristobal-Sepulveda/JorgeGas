package com.example.conductor.ui.administrarcuentas

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.navigation.fragment.findNavController
import com.example.conductor.R
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding
import com.example.conductor.utils.NavigationCommand
import org.koin.android.ext.android.inject


class AdministrarCuentasFragment : BaseFragment() {

    private var _binding: FragmentAdministrarCuentasBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()
    private val _appDataSource: AppDataSource by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /************************ Inicializando Variables del fragmento****************************/
        _binding = FragmentAdministrarCuentasBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel
        val adapter = UsuarioAdapter(_viewModel,_appDataSource ,UsuarioAdapter.OnClickListener{ usuario -> })
        _binding!!.recyclerviewListaUsuarios.adapter = adapter
        _viewModel.displayUsuariosInRecyclerView()

        _binding!!.textInputEditTextBuscarUsuario.addTextChangedListener(object: TextWatcher{
            var listasDeRespaldo = mutableListOf<MutableList<Usuario>>()

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val filteredList = _viewModel.domainUsuariosInScreen.value!!.filter{
                    it.nombre.lowercase().contains(s.toString().lowercase())
                } as MutableList<Usuario>
                if(count ==1){
                    _viewModel.filtrarUsuariosEnRecyclerViewPorEditText(filteredList)
                    return
                }
                if(before == 1 ){
                    _viewModel.filtrarUsuariosEnRecyclerViewPorEditText(listasDeRespaldo.last())
                    listasDeRespaldo.removeLast()
                    return
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.i("asd","$start    $count   $after")
                if(after==1){
                    listasDeRespaldo.add(_viewModel.domainUsuariosInScreen.value as MutableList<Usuario>)
                    return
                }
            }

            override fun afterTextChanged(s: Editable?) {
                return
            }
        })

        _binding!!.buttonCrearCuenta.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    AdministrarCuentasFragmentDirections
                        .actionNavigationAdministrarCuentasToNavigationDataUsuario())
        }

        _binding!!.imageViewMenu.setOnClickListener{view->
            val popupMenu = PopupMenu(requireActivity(), view)
            popupMenu.inflate(R.menu.administracion_de_cuentas_roles_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.rolAdministradores -> {
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Administrador")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.rolConductores -> {
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Conductor")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.rolPeonetas -> {
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Peoneta")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.rolSecretarias -> {
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Secretaria")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.rolVolanteros -> {
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Volantero")
                        return@setOnMenuItemClickListener true
                    }
                    R.id.rolTodos ->{
                        _viewModel.filtrarUsuariosEnRecyclerViewPorMenu("Todos")
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener true
                    }
                }
            }
            popupMenu.show()
        }

        _viewModel.navigateToSelectedUsuario.observe(viewLifecycleOwner) {
            if (null != it) {
                _viewModel.navigationCommand.value =
                    NavigationCommand.To(AdministrarCuentasFragmentDirections
                        .actionNavigationAdministrarCuentasToEditarUsuarioFragment(it))
            }
        }

        _viewModel.domainUsuariosInScreen.observe(requireActivity()) {
            it.let {
                adapter.submitList(it as MutableList<Usuario>)
            }
        }

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewModel.removeUsuariosInRecyclerView()
    }




}