package com.example.conductor.ui.registrodeasistencia

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.AsistenciaAdapter
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.databinding.FragmentRegistroDeAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class RegistroDeAsistenciaFragment: BaseFragment() {

    private var _binding: FragmentRegistroDeAsistenciaBinding? = null
    override val _viewModel: RegistroDeAsistenciaViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentRegistroDeAsistenciaBinding.inflate(inflater, container, false)

        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                _viewModel.obtenerRegistroDeAsistencia(requireActivity())
            }
            val volanteros = _viewModel.obtenerListaDeVolanterosEnElRegistroDeAsistencia()
            val autoCompleteTextViewAdapter = ArrayAdapter(requireActivity(), R.layout.simple_spinner_dropdown_item, volanteros)
            autoCompleteTextViewAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            withContext(Dispatchers.Main){
                _binding!!.editTextRegistroDeAsistenciaVolantero.setAdapter(autoCompleteTextViewAdapter)
            }
        }

        val recyclerViewAdapter = AsistenciaAdapter(AsistenciaAdapter.OnClickListener { _ -> })

        _binding!!.recyclerViewRegistroDeAsistenciaListadoDeAsistencia.adapter = recyclerViewAdapter

        _binding!!.textViewRegistroDeAsistenciaTitulo.setOnClickListener{
            Log.e("RegistroDeAsistenciaFragment", _viewModel.registroDeAsistencia.value.toString())
        }

        _binding!!.editTextRegistroDeAsistenciaVolantero.setOnItemClickListener { parent, view, position, id ->
            val volanteroSeleccionado = parent.adapter.getItem(position) as String
            // Llama a tu función aquí, pasando el volantero seleccionado como parámetro
            val filteredList = _viewModel.registroDeAsistencia.value?.filter { it["nombreCompleto"] == volanteroSeleccionado }
            val registroAsistenciaFiltrado = filteredList?.get(0)?.get("registroAsistencia") as MutableList<Map<String, Any>>
            val registrosProcesados = registroAsistenciaFiltrado.map{
                Asistencia(
                    it["fecha"].toString(),
                    it["ingresoJornada"].toString(),
                    it["salidaJornada"].toString(),
                )
            }
            recyclerViewAdapter.submitList(registrosProcesados)
            Log.e("volantero elejido", registroAsistenciaFiltrado.toString())
            Log.e("volantero elejido", volanteroSeleccionado)
        }

        return _binding!!.root
    }
}