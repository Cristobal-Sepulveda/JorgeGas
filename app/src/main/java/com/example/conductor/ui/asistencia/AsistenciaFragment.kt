package com.example.conductor.ui.asistencia

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.adapter.AsistenciaIndividualAdapter
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import com.example.conductor.utils.lanzarAlertaConConfirmacionYFuncionEnConsecuenciaEnMainThread
import com.example.conductor.utils.showToastInMainThreadWithStringResource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AsistenciaFragment: BaseFragment() {
    private var _binding: FragmentAsistenciaBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentAsistenciaBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        val adapter = AsistenciaIndividualAdapter(AsistenciaIndividualAdapter.OnClickListener { })

        _binding!!.recyclerviewAsistenciaListadoDeAsistencia.adapter = adapter

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        _viewModel.desplegarAsistenciaEnRecyclerView()

        _viewModel.domainAsistenciaEnScreen.observe(requireActivity()) {
            it?.let {
                adapter.submitList(it)
            }
        }

        _binding!!.buttonAsistenciaEntrada.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    registrarEntradaDeJornada()
                }
            }
        }

        _binding!!.buttonAsistenciaSalida.setOnClickListener {
            lanzarAlertaConConfirmacionYFuncionEnConsecuenciaEnMainThread(requireActivity(), R.string.atencion, R.string.esta_seguro_que_desea_finalizar_su_jornada,) {
                lifecycleScope.launch(Dispatchers.IO){
                    _viewModel.registrarSalidaDeJornada()
                }
            }
        }
        return _binding!!.root
    }

    @SuppressLint("MissingPermission")
    private fun registrarEntradaDeJornada() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                _viewModel.registrarIngresoDeJornada(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "No se pudo obtener la ubicación $lastKnownLocation", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("AsistenciaFragment", task.result.toString())
                    Toast.makeText(requireContext(), "No se pudo obtener la ubicación"+ task.result.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(requireContext(), "No se pudo obtener la ubicación ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("AsistenciaFragment", e.message, e)
        }
    }

}