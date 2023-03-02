package com.example.conductor.ui.registrovolanteros

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentRegistroVolanterosBinding
import com.example.conductor.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.util.*

class RegistroVolanterosFragment: BaseFragment(), OnMapReadyCallback {
    override val _viewModel: RegistroVolanterosViewModel by inject()
    private var _binding: FragmentRegistroVolanterosBinding? = null
    private lateinit var map: GoogleMap

    private var datePickerDialog: DatePickerDialog? = null
    private var selectedDate: String? = null

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRegistroVolanterosBinding.inflate(inflater, container, false)
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_registroVolantero_googleMaps) as? SupportMapFragment)?.getMapAsync(this)

        val currentDate = LocalDate.now().toString()
        _viewModel.setSelectedDate(currentDate)

        _binding!!.editTextRegistroVolanterosFecha.setOnClickListener{
            abrirCalendario(Calendar.getInstance())
        }

        _viewModel.selectedDate.observe(viewLifecycleOwner) {
            _binding!!.editTextRegistroVolanterosFecha.text = Editable.Factory.getInstance().newEditable(it)
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.obtenerTodoElRegistroTrayectoVolanteros()
                }
            }
        }

        return _binding!!.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapStyle(map)
        moverCamaraADefaultLocation()
    }

    private fun setMapStyle(map: GoogleMap){
        try{
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.map_style
                )
            )
            if(!success){
                Log.e("MapFragment", "Error al cargar el estilo del mapa")
            }
        }catch(e:Exception){
            Log.e("MapFragment", "Error al cargar el estilo del mapa", e)
        }
    }

    private fun moverCamaraADefaultLocation() {
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(Constants.defaultLocation.latitude,
                    Constants.defaultLocation.longitude),
                Constants.cameraDefaultZoom.toFloat()
            )
        )
    }

    private fun abrirCalendario(today: Calendar) {
        Log.d("asd", "abrirCalendario:")
        datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            if (month < 9) {
                selectedDate = "$year-0${month + 1}-$dayOfMonth"
                if(dayOfMonth<10){
                    selectedDate = "$year-0${month + 1}-0$dayOfMonth"
                }
                _viewModel.setSelectedDate(selectedDate!!)
                return@DatePickerDialog
            } else {
                selectedDate = "$year-${month + 1}-$dayOfMonth"
                if(dayOfMonth<10){
                    selectedDate = "$year-${month + 1}-0$dayOfMonth"
                }
                _viewModel.setSelectedDate(selectedDate!!)
                return@DatePickerDialog
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePickerDialog?.show()
    }
}