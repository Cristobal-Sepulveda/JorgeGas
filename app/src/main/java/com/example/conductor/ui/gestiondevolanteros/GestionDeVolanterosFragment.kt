package com.example.conductor.ui.gestiondevolanteros

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.adapter.VolanteroAdapter
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentGestionDeVolanterosBinding
import com.example.conductor.utils.NavigationCommand
import com.google.android.material.tabs.TabLayout
import org.koin.android.ext.android.inject


class GestionDeVolanterosFragment : BaseFragment() {
    override val _viewModel: GestionDeVolanterosViewModel by inject()
    private var _binding: FragmentGestionDeVolanterosBinding? = null
    private val _appDataSource : AppDataSource by inject()
    private var filtroSeleccionado = "Activos"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGestionDeVolanterosBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        val adapter = VolanteroAdapter(_viewModel,_appDataSource, VolanteroAdapter.OnClickListener{
            _viewModel.navigationCommand.value = NavigationCommand.To(
                GestionDeVolanterosFragmentDirections
                    .actionNavigationGestionDeVolanterosToNavigationDetalleVolantero(it))
        })

        _binding!!.recyclerviewListaVolanteros.adapter = adapter

        _binding!!.textInputEditTextGestionDeVolanterosBuscarUsuario.addTextChangedListener(object:
            TextWatcher {
            var listasDeRespaldoActivos = mutableListOf<MutableList<Usuario>>()
            var listasDeRespaldoInactivos = mutableListOf<MutableList<Usuario>>()

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (filtroSeleccionado == "Activos") {
                    val filteredList = _viewModel.domainUsuariosVolanterosActivos.filter {
                        it.nombre.lowercase().contains(s.toString().lowercase())
                    } as MutableList<Usuario>
                    _viewModel.filtrarUsuariosActivosEnRecyclerViewPorEditText(filteredList)
                } else {
                    val filteredList = _viewModel.domainUsuariosVolanterosInactivos.filter {
                        it.nombre.lowercase().contains(s.toString().lowercase())
                    } as MutableList<Usuario>
                    _viewModel.filtrarUsuariosInactivosEnRecyclerViewPorEditText(filteredList)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /*Log.i("asd","$start    $count   $after")
                if(filtroSeleccionado =="Activos"){
                    if(after==1){
                        listasDeRespaldoActivos.add(_viewModel.domainUsuariosActivosInScreen.value as MutableList<Usuario>)
                        return
                    }
                }else{
                    if(after==1){
                        listasDeRespaldoInactivos.add(_viewModel.domainUsuariosInactivosInScreen.value as MutableList<Usuario>)
                        return
                    }
                }*/
            }

            override fun afterTextChanged(s: Editable?) {
                return
            }
        })

        _binding!!.tabLayoutSwitchPersonalizado.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab!!.text=="Activos"){
                    filtroSeleccionado = "Activos"
                    _viewModel.filtrarUsuariosActivosEnRecyclerViewPorSwitch()
                    if(_viewModel.domainUsuariosVolanterosActivos.isEmpty()){
                        _viewModel.noHayVolanterosActivos(true)
                    }
                }else{
                    filtroSeleccionado = "Inactivos"
                    _viewModel.noHayVolanterosActivos(false)
                    _viewModel.filtrarUsuariosInactivosEnRecyclerViewPorSwitch()
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

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
        filtroSeleccionado = "Activos"
        _binding = null
    }

}