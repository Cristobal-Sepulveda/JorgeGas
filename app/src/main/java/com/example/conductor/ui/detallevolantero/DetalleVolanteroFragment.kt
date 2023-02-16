package com.example.conductor.ui.detallevolantero

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.conductor.BuildConfig
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentDetalleVolanteroBinding
import com.example.conductor.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.FileInputStream
import java.util.*
import kotlin.collections.ArrayList

class DetalleVolanteroFragment: BaseFragment(), OnMapReadyCallback {

    override val _viewModel: DetalleVolanteroViewModel by inject()
    private var _binding: FragmentDetalleVolanteroBinding? = null
    private lateinit var map: GoogleMap
    private var datePickerDialog: DatePickerDialog? = null
    private var selectedDate: String? = null
    private var registroDelVolantero: Any? = null
    private var latLngsDeInteres = mutableListOf<LatLng?>()
    private lateinit var geoApiContext: GeoApiContext

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVolanteroBinding.inflate(inflater, container, false)
        val bundle = DetalleVolanteroFragmentArgs.fromBundle(requireArguments()).usuarioDetails
        val today = Calendar.getInstance()
        cargarDatosDelVolantero(bundle)
        obtenerGeoApiContext()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                registroDelVolantero = _viewModel.obtenerRegistroDelVolantero(bundle.id)
            }
        }

        _binding!!.sliderDetalleVolanteroTrayecto.addOnChangeListener { _, value, _ ->
            if (pintarGeopointsSiCorresponde(value)) {
                return@addOnChangeListener
            }
        }

        _binding!!.sliderDetalleVolanteroTrayecto.setLabelFormatter { value ->
            customizarSliderLabel(value)
        }

        _binding!!.imageViewDetalleVolanteroCalendario.setOnClickListener {
            abrirCalendario(today)
        }

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_detalleVolantero_googleMaps)
                as? SupportMapFragment)?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
    }

    private fun startingPermissionCheck() {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (isPermissionGranted) {
            try {
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            Constants.defaultLocation.latitude,
                            Constants.defaultLocation.longitude
                        ), Constants.cameraDefaultZoom.toFloat()
                    )
                )
            } catch (e: SecurityException) {
                _binding!!.fragmentContainerViewDetalleVolanteroGoogleMaps.isGone = true
                _binding!!.imageviewDetalleVolanteroMapaSinPermisos.isGone = false

            }
        } else {
            _binding!!.fragmentContainerViewDetalleVolanteroGoogleMaps.isGone = true
            _binding!!.imageviewDetalleVolanteroMapaSinPermisos.isGone = false
        }
    }

    private fun cargarDatosDelVolantero(bundle: Usuario) {
        val fotoPerfil = bundle.fotoPerfil
        if (fotoPerfil.last().toString() == "=" || (fotoPerfil.first()
                .toString() == "/" && fotoPerfil[1].toString() == "9")
        ) {
            val decodedString = Base64.decode(fotoPerfil, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            _binding!!.imageViewDetalleVolanteroFotoPerfil.setImageBitmap(decodedByte)
        } else {
            val aux2 = fotoPerfil.indexOf("=") + 1
            val aux3 = fotoPerfil.substring(0, aux2)
            val decodedString = Base64.decode(aux3, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            _binding!!.imageViewDetalleVolanteroFotoPerfil.setImageBitmap(decodedByte)
        }
    }

    private fun customizarSliderLabel(value: Float): String {
        val minutes = (value * 10).toInt() % 60
        val hours = 10 + (value * 10).toInt() / 60
        return String.format("%02d:%02d", hours, minutes)
    }

    private fun abrirCalendario(today: Calendar) {
        datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            if (month < 9) {
                selectedDate = "$year-0${month + 1}-$dayOfMonth"
                validarFechaYActivarSlider(selectedDate!!)
                return@DatePickerDialog
            } else {
                selectedDate = "$year-${month + 1}-$dayOfMonth"
                validarFechaYActivarSlider(selectedDate!!)
                return@DatePickerDialog
            }
        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePickerDialog?.show()
    }
    private fun obtenerGeoApiContext() {
        geoApiContext = GeoApiContext.Builder()
            .apiKey("AIzaSyAi8_l1Ql2fuW75bSjvTT_2ZMBmlo38wUs")
            .build()
    }

    private fun validarFechaYActivarSlider(selectedDate: String) {
        val registroDelVolanteroParseado = registroDelVolantero as DocumentSnapshot
        val registroJornada =
            registroDelVolanteroParseado.data!!["registroJornada"] as ArrayList<Map<String, Map<*, *>>>
        registroJornada.forEach {
            if (it["fecha"].toString() == selectedDate) {
                _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = true
                return
            }
        }
        _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = false
        Toast.makeText(requireActivity(), "No hay registro con esa fecha.", Toast.LENGTH_SHORT)
            .show()
    }

    private fun pintarGeopointsSiCorresponde(value: Float): Boolean {
        map.clear()
        latLngsDeInteres.clear()
        val minutes = (value * 10).toInt() % 60
        val hours = 10 + (value * 10).toInt() / 60
        val time = String.format("%02.0f:%02d", hours.toFloat(), minutes)
        Log.d("Slider", "Selected time: $time")
        val registroDelVolanteroParseado = registroDelVolantero as DocumentSnapshot
        val registroJornada =
            registroDelVolanteroParseado.data!!["registroJornada"] as ArrayList<Map<String, Map<*, *>>>
        println(selectedDate)
        for (registro in registroJornada) {
            if (registro["fecha"].toString() == selectedDate) {
                val registroLatLngsDelDia = registro["registroLatLngs"]
                val listadoHoraDeRegistroNuevosGeopoints =
                    registroLatLngsDelDia!!["horasConRegistro"] as ArrayList<String>
                val geopointsRegistradosDelDia =
                    registroLatLngsDelDia["geopoints"] as ArrayList<GeoPoint>
                for (horaRegistrada in listadoHoraDeRegistroNuevosGeopoints) {
                    if (validarSiPintarGeopointsSegunSuHoraDeRegistro(
                            horaRegistrada,
                            hours,
                            minutes
                        )
                    ) {
                        var i = listadoHoraDeRegistroNuevosGeopoints.indexOf(horaRegistrada)
                        while (i < listadoHoraDeRegistroNuevosGeopoints.size) {
                            if (listadoHoraDeRegistroNuevosGeopoints[i].substring(0, 2)
                                    .toFloat() == hours.toFloat() &&
                                listadoHoraDeRegistroNuevosGeopoints[i].substring(3, 5)
                                    .toFloat() > minutes.toFloat()
                            ) {
                                break
                            }
                            val latitud = geopointsRegistradosDelDia[i].latitude
                            val longitud = geopointsRegistradosDelDia[i].longitude
                            val latLng = LatLng(latitud, longitud)
                            latLngsDeInteres.add(latLng)
                            i++
                        }
                        lifecycleScope.launch{
                            withContext(Dispatchers.IO){
                                pintarPolyline()
                            }
                        }
                        break
                    }
                }
                return true
            }
        }
        return false
    }

    private fun pintarPolyline() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val request = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.WALKING)
                        .origin("-33.493105,-70.7479667")
                        .destination("-33.54347,-70.62007")
                        .optimizeWaypoints(true)
                        .await()
                    withContext(Dispatchers.Main){
                        if (request?.routes != null && request.routes.isNotEmpty()) {
                            val encodedPolyline = request.routes[0].overviewPolyline.encodedPath
                            // Decode the polyline to a list of LatLng points
                            val decodedPath = PolyUtil.decode(encodedPolyline)
                            // Create a PolylineOptions object and configure its appearance
                            val polylineOption = PolylineOptions()
                                .color(Color.BLUE)
                                .width(10f)
                            // Add the LatLng points to the PolylineOptions object
                            decodedPath.forEach { latLng ->
                                polylineOption.add(latLng)
                            }
                            // Add the Polyline to the map
                            map.addPolyline(polylineOption)
                            val polylineOptions = PolylineOptions()
                            polylineOptions.color(Color.RED)
                            polylineOptions.width(10f)
                            polylineOptions.addAll(latLngsDeInteres.filterNotNull())
                            map.addPolyline(polylineOptions)
                        } else {
                            Log.e("DIRECTIONS_API_ERROR", "Error: $request? es null")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DIRECTIONS_API_ERROR", "Error catch: ${e.message}")
                }
            }
        }
    }

    private fun validarSiPintarGeopointsSegunSuHoraDeRegistro(
        horaRegistrada: String,
        selectedHours: Int,
        selectedMinutes: Int
    ): Boolean {
        val horaRegistradaHours = horaRegistrada.substring(0, 2).toFloat()
        val horaRegistradaMinutes = horaRegistrada.substring(3, 5).toFloat()
        return selectedHours.toFloat() >= horaRegistradaHours && selectedMinutes.toFloat() >= horaRegistradaMinutes
    }

}