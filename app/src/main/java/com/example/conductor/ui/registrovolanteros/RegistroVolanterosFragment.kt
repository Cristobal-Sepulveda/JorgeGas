package com.example.conductor.ui.registrovolanteros

import android.app.DatePickerDialog
import android.graphics.Color
import android.location.Location
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
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

class RegistroVolanterosFragment: BaseFragment(), OnMapReadyCallback {
    override val _viewModel: RegistroVolanterosViewModel by inject()
    private var _binding: FragmentRegistroVolanterosBinding? = null
    private lateinit var map: GoogleMap

    private var datePickerDialog: DatePickerDialog? = null
    private var selectedDate: String? = null
    private var listOfPolylines = mutableListOf<List<GeoPoint>>()
    private var listOfHours = mutableListOf<List<String>>()
    private var recienInicioElFragmento = 0

    private lateinit var polylineOptions: PolylineOptions

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRegistroVolanterosBinding.inflate(inflater, container, false)
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_registroVolantero_googleMaps) as? SupportMapFragment)?.getMapAsync(this)

        _binding!!.editTextRegistroVolanterosFecha.setOnClickListener{
            abrirCalendario(Calendar.getInstance())
        }

        _viewModel.selectedDate.observe(viewLifecycleOwner) {
            _binding!!.editTextRegistroVolanterosFecha.text = Editable.Factory.getInstance().newEditable(it)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    solicitarRegistroYFiltrarloAntesDePintarlo(it)
                }
            }
        }

        return _binding!!.root
    }

    private suspend fun solicitarRegistroYFiltrarloAntesDePintarlo(it: String?) {
        val listadoObtenido =
            _viewModel.obtenerTodoElRegistroTrayectoVolanteros() as MutableList<Any>
        if (listadoObtenido.isNotEmpty()) {
            listadoObtenido.forEach { documento ->
                val docParseado = documento as HashMap<String, Any>
                val registroJornada =
                    docParseado["registroJornada"] as List<HashMap<String, Any>>

                registroJornada.forEach { mapa ->
                    if (mapa["fecha"] == it) {
                        val registroLatLngs = mapa["registroLatLngs"] as Map<*, *>
                        val geoPoints = registroLatLngs["geopoints"] as List<*>
                        val horasConRegistro =
                            registroLatLngs["horasConRegistro"] as List<*>
                        println(docParseado["nombreCompleto"])
                        println("${mapa["fecha"]}")
                        listOfPolylines.add(geoPoints as List<GeoPoint>)
                        listOfHours.add(horasConRegistro as List<String>)
                    }
                }
            }
            pintarPolylines()
        } else {
            Snackbar.make(
                _binding!!.root,
                "Error al cargar los datos. Intentelo nuevamente",
                Snackbar.LENGTH_SHORT
            ).show()
        }
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

    private fun pintarPolylines() {
        lifecycleScope.launch{
            withContext(Dispatchers.Main) {
                try {
                    map.clear()
                    polylineOptions = PolylineOptions().width(10f)
                    var distanceRecorrida = 0
                    var topeParaDibujar = 0
                    var tiempoEnRecorrerTramo = 0f
                    val listAux = mutableListOf<LatLng?>()
                    var tiempoEnRojo = 0f
                    var tiempoEnAmarillo = 0f
                    var tiempoEnVerde = 0f
                    var tiempoEnAzul = 0f

                    listOfPolylines.forEachIndexed{pivote, listOfGeopoints ->
                        Log.i("DetalleVolanteroFragment","${listOfPolylines.size}")
                        Log.i("DetalleVolanteroFragment","$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote$pivote")
                        listOfGeopoints.forEachIndexed loop@{i , geopoint ->
                            if (i == listOfGeopoints.size - 1) {
                                return@loop
                            }
                            val latLng1 = Location("")
                            latLng1.latitude = geopoint.latitude ?: 0.0
                            latLng1.longitude = geopoint.longitude ?: 0.0
                            val latLng2 = Location("")
                            latLng2.latitude = listOfGeopoints[i + 1].latitude
                            latLng2.longitude = listOfGeopoints[i + 1].longitude
                            val tiempoLatLng1 = LocalTime.parse(listOfHours[pivote][i])
                            val tiempoLatLng2 = LocalTime.parse(listOfHours[pivote][i+1])

                            val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                            val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()

                            Log.i("DetalleVolanteroFragment","$tiempoLatLng1 $tiempoLatLng2 $timeBetweenLatLngs $distanceBetweenLatLngs")
                            distanceRecorrida += distanceBetweenLatLngs
                            tiempoEnRecorrerTramo += timeBetweenLatLngs
                            topeParaDibujar++
                            listAux.add(LatLng(geopoint.latitude, geopoint.longitude))
                            if (topeParaDibujar == 23) {
                                val rangoMayor = (tiempoEnRecorrerTramo/1000 * 0.75).toInt()
                                val rangoMenor = (tiempoEnRecorrerTramo/1000 * 0.30).toInt()
                                val rangoMaximoHumano = rangoMayor*4
                                Log.i("DetalleVolanteroFragment","distanceRecorrida: $distanceRecorrida")
                                Log.i("DetalleVolanteroFragment","tiempoEnRecorrerTramo: $tiempoEnRecorrerTramo")
                                // Set the color based on the distance
                                val color = when (distanceRecorrida) {
                                    in 0..rangoMenor -> Color.RED
                                    in rangoMenor..rangoMayor -> Color.YELLOW
                                    in rangoMayor..rangoMaximoHumano -> Color.GREEN
                                    else -> Color.BLUE
                                }
                                polylineOptions.addAll(listAux).color(color)
                                map.addPolyline(polylineOptions)

                                when(color){
                                    Color.RED -> {
                                        tiempoEnRojo += tiempoEnRecorrerTramo/1000
                                    }
                                    Color.YELLOW -> {
                                        tiempoEnAmarillo += tiempoEnRecorrerTramo/1000
                                    }
                                    Color.GREEN -> {
                                        tiempoEnVerde += tiempoEnRecorrerTramo/1000
                                    }
                                    Color.BLUE -> {
                                        tiempoEnAzul += tiempoEnRecorrerTramo/1000
                                    }
                                }

                                polylineOptions = PolylineOptions().width(10f)
                                distanceRecorrida = 0
                                tiempoEnRecorrerTramo = 0f
                                topeParaDibujar = 0
                                listAux.clear()
                            }
                        }
                    }
                    return@withContext
                    /*_binding!!.textViewDetalleVolanteroRojo.text = convertSecondsToHMS(tiempoEnRojo)
                    _binding!!.textViewDetalleVolanteroAmarillo.text = convertSecondsToHMS(tiempoEnAmarillo)
                    _binding!!.textViewDetalleVolanteroVerde.text = convertSecondsToHMS(tiempoEnVerde)
                    _binding!!.textViewDetalleVolanteroAzul.text = convertSecondsToHMS(tiempoEnAzul)*/
                } catch (e: Exception) {
                    Log.i("error", "$e")
                    Snackbar.make(_binding!!.root, "$e", Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

}