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
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.databinding.FragmentRegistroVolanterosBinding
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.filtroregistrovolanteros.FiltroRegistroVolanterosFragment
import com.example.conductor.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.Duration
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
    private var listOfIds = mutableListOf<String>()
    private lateinit var polylineOptions: PolylineOptions

    override fun onCreateView(inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRegistroVolanterosBinding.inflate(inflater, container, false)
        _binding!!.viewModel = _viewModel
        _binding!!.lifecycleOwner= this

        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_registroVolantero_googleMaps) as? SupportMapFragment)?.getMapAsync(this)



        _binding!!.editTextRegistroVolanterosFecha.setOnClickListener{
            abrirCalendario(Calendar.getInstance())
        }

        _binding!!.floatingActionButtonDetalleVolanteroCambiarTipoDeMapa.setOnClickListener{
            cambiarTipoDeMapa()
        }

        _binding!!.buttonRegistroVolanterosFiltro.setOnClickListener{
            if(listOfIds.isEmpty()){
                Snackbar.make(_binding!!.root, R.string.mensaje_filtro_volanteros, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val filtroRegistroVolanterosFragment = FiltroRegistroVolanterosFragment()
            filtroRegistroVolanterosFragment.show(childFragmentManager, "FiltroRegistroVolanterosFragment")
        }

        _viewModel.selectedDate.observe(viewLifecycleOwner) {
            _binding!!.editTextRegistroVolanterosFecha.text = Editable.Factory.getInstance().newEditable(it)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    solicitarRegistroYFiltrarloAntesDePintarlo(it)
                }
            }
        }

        _viewModel.selectedVolanteros.observe(viewLifecycleOwner){
            if(it.isEmpty()){
                return@observe
            }
            if(it.size != listOfIds.size){
                Log.i("RegistroVolanteros", "selectedVolanteros: $it")
                lifecycleScope.launch {
                    withContext(Dispatchers.Main) {
                        limpiarDataNecesaria()
                    }
                    withContext(Dispatchers.IO) {
                        solicitarRegistroYFiltrarloAntesDePintarlo(_viewModel.selectedDate.value)
                    }
                }
            }
        }

        return _binding!!.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _viewModel.selectedDate.removeObservers(viewLifecycleOwner)
        _viewModel.setSelectedDate("")
        _binding!!.editTextRegistroVolanterosFecha.setText("")
        _binding = null
    }

    private fun limpiarDataNecesaria(){
        listOfPolylines.clear()
        listOfHours.clear()
        listOfIds.clear()
        _binding!!.textViewRegistroVolanterosVerde.setText(R.string.hora)
        _binding!!.textViewRegistroVolanterosAmarillo.setText(R.string.hora)
        _binding!!.textViewRegistroVolanterosRojo.setText(R.string.hora)
        _binding!!.textViewRegistroVolanterosAzul.setText(R.string.hora)
        _binding!!.textViewRegistroVolanterosRosado.setText(R.string.hora)
        _binding!!.textViewRegistroVolanterosCaminado.setText(R.string.hora)
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
        _viewModel.limpiarSelectedVolanteros()
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

    private suspend fun solicitarRegistroYFiltrarloAntesDePintarlo(selectedDate: String?) {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                map.clear()
                limpiarDataNecesaria()
                _binding!!.editTextRegistroVolanterosFecha.setTextColor(Color.BLACK)
            }
        }

        _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.LOADING)

        val registroDiariosVolanteros = _viewModel.obtenerRegistroDiariosRoomDesdeFirestore()

        if (registroDiariosVolanteros.isNotEmpty()) {
            var hayRegistrosEnEsaFecha = false
            registroDiariosVolanteros.forEach { documentSnapshot ->
                val id = documentSnapshot.id
                val docParseado = documentSnapshot.data as Map<*, *>
                val registroJornada = docParseado["registroJornada"] as List<Map<*,*>>
                registroJornada.forEach { mapa ->
                    Log.e("RegistroVolanteros", "${mapa["fecha"]}")
                    if (mapa["fecha"] == selectedDate) {
                        hayRegistrosEnEsaFecha = true
                        val latLngs = mapa["latLngs"] as List<Map<*, *>>
                        val geoPoints: MutableList<GeoPoint> = mutableListOf()
                        val horasConRegistro: MutableList<String> = mutableListOf()

                        if(_viewModel.selectedVolanteros.value!!.isEmpty()){
                            latLngs.forEach{
                                val lat = it["lat"] as Double
                                val lng = it["lng"] as Double
                                val geoPoint = GeoPoint(lat, lng)
                                val hora = it["hora"] as String
                                geoPoints.add(geoPoint)
                                horasConRegistro.add(hora)
                            }
                        }else{
                            if(_viewModel.selectedVolanteros.value!!.contains(id)){
                                latLngs.forEach{
                                    val lat = it["lat"] as Double
                                    val lng = it["lng"] as Double
                                    val geoPoint = GeoPoint(lat, lng)
                                    val hora = it["hora"] as String
                                    geoPoints.add(geoPoint)
                                    horasConRegistro.add(hora)
                                }
                            }
                        }
                        listOfPolylines.add(geoPoints)
                        listOfHours.add(horasConRegistro)
                        listOfIds.add(id)
                    }
                }
            }
            if(!hayRegistrosEnEsaFecha){
                withContext(Dispatchers.Main) {
                    _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.DONE)
                    _binding!!.editTextRegistroVolanterosFecha.setTextColor(Color.RED)
                    Toast.makeText(requireContext(), "No hay registros en esa fecha", Toast.LENGTH_LONG).show()
                }
                return
            }
            _binding!!.editTextRegistroVolanterosFecha.setTextColor(Color.BLACK)
            _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.DONE)
            pintarPolylines()
        }
    }

    private fun pintarPolylines() {
        lifecycleScope.launch{
            withContext(Dispatchers.Main) {
                try {
                    Log.e("RegistroVolanteros", "pintarPolylines: ${listOfIds}")
                    _viewModel.setearSelectedVolanteros(listOfIds)
                    var totalDistanciaRecorrida = 0
                    var tiempoEnRojo = 0f
                    var tiempoEnAmarillo = 0f
                    var tiempoEnVerde = 0f
                    var tiempoEnAzul = 0f
                    val tiempoEnRosado = 0f

                    listOfPolylines.forEachIndexed{pivote, listOfGeopoints ->
                        var distanceRecorrida = 0
                        var tiempoEnRecorrerTramo = 0f
                        var topeParaDibujar = 0
                        val listAux = mutableListOf<LatLng?>()
                        // Reset the polyline options to a new instance
                        polylineOptions = PolylineOptions().width(10f)
                        listOfGeopoints.forEachIndexed loop@{i , geopoint ->
                            if (i == listOfGeopoints.size - 1) {
                                return@loop
                            }
                            val latLng1 = Location("")
                            latLng1.latitude = geopoint.latitude
                            latLng1.longitude = geopoint.longitude
                            val latLng2 = Location("")
                            latLng2.latitude = listOfGeopoints[i + 1].latitude
                            latLng2.longitude = listOfGeopoints[i + 1].longitude

                            val tiempoLatLng1 = LocalTime.parse(listOfHours[pivote][i])
                            val tiempoLatLng2 = LocalTime.parse(listOfHours[pivote][i+1])
                            val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                            val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()

                            distanceRecorrida += distanceBetweenLatLngs
                            totalDistanciaRecorrida += distanceBetweenLatLngs
                            tiempoEnRecorrerTramo += timeBetweenLatLngs
                            topeParaDibujar++
                            listAux.add(LatLng(geopoint.latitude, geopoint.longitude))

                            if (topeParaDibujar == 23 || i == listOfGeopoints.size - 2) {
                                val rangoMayor = (tiempoEnRecorrerTramo / 1000 * 0.75).toInt()
                                val rangoMenor = (tiempoEnRecorrerTramo / 1000 * 0.30).toInt()
                                val rangoMaximoHumano = rangoMayor * 4
                                Log.i("DetalleVolanteroFragment","distanceRecorrida: $distanceRecorrida")
                                Log.i("DetalleVolanteroFragment","tiempoEnRecorrerTramo: $tiempoEnRecorrerTramo")
                                // Set the color based on the distance
                                val color = when (distanceRecorrida) {
                                    in 0..rangoMenor -> Color.RED
                                    in rangoMenor..rangoMayor -> Color.YELLOW
                                    in rangoMayor..rangoMaximoHumano -> Color.GREEN
                                    else -> Color.BLUE
                                }

                                val polylineOptions = PolylineOptions().width(10f).addAll(listAux).color(color)
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

                                distanceRecorrida = 0
                                tiempoEnRecorrerTramo = 0f
                                topeParaDibujar = 0
                                listAux.clear()
                            }
                        }
                    }
                    _binding!!.textViewRegistroVolanterosVerde.text = convertSecondsToHMS(tiempoEnVerde)
                    _binding!!.textViewRegistroVolanterosAmarillo.text = convertSecondsToHMS(tiempoEnAmarillo)
                    _binding!!.textViewRegistroVolanterosRojo.text = convertSecondsToHMS(tiempoEnRojo)
                    _binding!!.textViewRegistroVolanterosAzul.text = convertSecondsToHMS(tiempoEnAzul)
                    _binding!!.textViewRegistroVolanterosRosado.text = convertSecondsToHMS(tiempoEnRosado)
                    _binding!!.textViewRegistroVolanterosCaminado.text = editarDistanciaTotalRecorrida(totalDistanciaRecorrida)
                    _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.DONE)
                    return@withContext
                } catch (e: Exception) {
                    Log.i("error", "$e")
                    _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.ERROR)
                    Snackbar.make(_binding!!.root, "$e", Snackbar.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun cambiarTipoDeMapa() {
        if (map.mapType == GoogleMap.MAP_TYPE_NORMAL) {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } else {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }


    private fun convertSecondsToHMS(seconds: Float): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun editarDistanciaTotalRecorrida(distancia: Int ): String {
        val distanciaKm = distancia / 1000.0
        val distanciaKmString = "%.2fkm".format(distanciaKm)
        return distanciaKmString
    }

}