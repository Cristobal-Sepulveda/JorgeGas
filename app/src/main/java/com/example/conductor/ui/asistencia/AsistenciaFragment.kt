package com.example.conductor.ui.asistencia

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.adapter.AsistenciaAdapter
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.FragmentAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AsistenciaFragment: BaseFragment() {
    private var _binding: FragmentAsistenciaBinding? = null
    override val _viewModel: AsistenciaViewModel by inject()
    private val _appDataSource: AppDataSource by inject()
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var lastKnownLocation: Location? = null
    private var locationPermissionGranted = false
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentAsistenciaBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        val adapter = AsistenciaAdapter(AsistenciaAdapter.OnClickListener { _ -> })

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
                    _viewModel.registrarSalidaDeJornada(requireContext())
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
}