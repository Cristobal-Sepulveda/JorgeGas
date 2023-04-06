package com.example.conductor.ui.asistencia

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.AsistenciaAdapter
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.ui.estadoactual.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AsistenciaFragment: BaseFragment() {
    private var _binding: FragmentAsistenciaBinding? = null
    override val _viewModel: AsistenciaViewModel by inject()
    private val _appDataSource: AppDataSource by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentAsistenciaBinding.inflate(inflater, container, false)
        val adapter = AsistenciaAdapter(_viewModel,_appDataSource , AsistenciaAdapter.OnClickListener{ _ -> })
        _binding!!.recyclerviewAsistenciaListadoDeAsistencia.adapter = adapter


        _binding!!.buttonAsistenciaEntrada.setOnClickListener{
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.registrarIngresoDeJornada(requireContext())
                }
            }
        }


        _binding!!.buttonAsistenciaSalida.setOnClickListener{
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.registrarSalidaDeJornada(requireContext())
                }
            }
        }

        return _binding!!.root
    }
}