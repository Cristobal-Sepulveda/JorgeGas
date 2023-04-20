package com.example.conductor.ui.asistencia

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.AsistenciaIndividualAdapter
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AsistenciaFragment: BaseFragment() {
    private var _binding: FragmentAsistenciaBinding? = null
    override val _viewModel: AsistenciaViewModel by inject()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentAsistenciaBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        val adapter = AsistenciaIndividualAdapter(AsistenciaIndividualAdapter.OnClickListener { })

        _binding!!.recyclerviewAsistenciaListadoDeAsistencia.adapter = adapter

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        _viewModel.desplegarAsistenciaEnRecyclerView(requireContext())

        _viewModel.domainAsistenciaEnScreen.observe(requireActivity()) {
            it?.let {
                adapter.submitList(it)
            }
        }

        _binding!!.buttonAsistenciaEntrada.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    getDeviceLocation()
                }
            }
        }

        _binding!!.buttonAsistenciaSalida.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    lanzarAlerta()
                }
            }
        }

        return _binding!!.root
    }
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                _viewModel.registrarIngresoDeJornada(
                                    requireContext(),
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                )
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "No se pudo obtener la ubicación",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.i("getDeviceLocation", "locationPermissionGranted is false")
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun lanzarAlerta(){
        Handler(Looper.getMainLooper()).post {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Atención")
            builder.setMessage("¿Estas seguro que deseas finalizar tu jornada? Si lo haces, tu trayecto del día será borrado del celular y será enviado a la nube.")
            builder.setPositiveButton("OK") { dialog, which ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        _viewModel.registrarSalidaDeJornada(requireContext())
                    }
                }
            }
            builder.setNegativeButton("Cancelar") { dialog, which ->
                // Do something when Cancel button is clicked
            }
            builder.show()
        }
    }
}