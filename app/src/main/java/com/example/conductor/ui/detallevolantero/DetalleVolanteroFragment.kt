package com.example.conductor.ui.detallevolantero

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
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
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.collections.ArrayList

class DetalleVolanteroFragment: BaseFragment(), OnMapReadyCallback {

    override val _viewModel: DetalleVolanteroViewModel by inject()
    private var _binding: FragmentDetalleVolanteroBinding? = null
    private lateinit var map: GoogleMap
    private var datePickerDialog: DatePickerDialog? = null
    private var selectedDate: String? = null
    private var registroDelVolanteroDocRef: Any? = null
    private var latLngsDeInteres = mutableListOf<LatLng?>()
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var bundle: Usuario
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVolanteroBinding.inflate(inflater, container, false)
        bundle = DetalleVolanteroFragmentArgs.fromBundle(requireArguments()).usuarioDetails
        val today = Calendar.getInstance()

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                registroDelVolanteroDocRef = _viewModel.obtenerRegistroDelVolantero(bundle.id)
            }
        }
        cargarDatosDelVolantero(bundle)
        obtenerGeoApiContext()

        _binding!!.sliderDetalleVolanteroTrayecto.addOnChangeListener { slider, value, _ ->
            val minutes = (value * 10).toInt() % 60
            val hours = 10 + (value * 10).toInt() / 60
            val selectedHourInSlide = String.format("%02.0f:%02d", hours.toFloat(), minutes)
            slider.setLabelFormatter{ return@setLabelFormatter selectedHourInSlide}
            if (iniciarValidacionesAntesDePintarPolyline(value)) {
                return@addOnChangeListener
            }
        }

        _binding!!.sliderDetalleVolanteroTrayecto.setLabelFormatter { value ->
            customizarSliderLabel(value)
        }

        _binding!!.imageViewDetalleVolanteroCalendario.setOnClickListener {
            abrirCalendario(today)
        }
        _binding!!.imageViewDetalleVolanteroRestar10minutos.setOnClickListener {
            sumarORestarValueDelSlider(-1f)
        }

        _binding!!.imageViewDetalleVolanteroSumar10minutos.setOnClickListener {
            sumarORestarValueDelSlider(1f)
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

    @Suppress("UNCHECKED_CAST")
    private fun validarFechaYActivarSlider(selectedDate: String) {
        if(registroDelVolanteroDocRef == null){
            _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = false
            _binding!!.textViewDetalleVolanteroFechaSeleccionadaAlerta.visibility = View.VISIBLE
            return
        }
        val registroDelVolanteroParseado = registroDelVolanteroDocRef as DocumentSnapshot
        val registroJornada =
            registroDelVolanteroParseado.data!!["registroJornada"] as ArrayList<Map<String, Map<*, *>>>
        registroJornada.forEach {
            if (it["fecha"].toString() == selectedDate) {
                _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = true
                _binding!!.sliderDetalleVolanteroTrayecto.labelBehavior = LabelFormatter.LABEL_VISIBLE
                _binding!!.textViewDetalleVolanteroFechaSeleccionadaAlerta.visibility = View.INVISIBLE
                _binding!!.sliderDetalleVolanteroTrayecto.trackActiveTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.orange
                    )
                )
                _binding!!.sliderDetalleVolanteroTrayecto.thumbStrokeColor = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.orange
                    )
                )
                _binding!!.textViewDetalleVolanteroFechaSeleccionadaValor.text = selectedDate
                _binding!!.textViewDetalleVolanteroFechaSeleccionadaValor.setTextColor(ContextCompat.getColor(requireActivity(), R.color.green))
                return
            }
        }
        _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = false
        _binding!!.textViewDetalleVolanteroFechaSeleccionadaAlerta.visibility = View.VISIBLE
        _binding!!.textViewDetalleVolanteroFechaSeleccionadaAlerta.setText(R.string.seleccione_una_fecha_valida)
        _binding!!.textViewDetalleVolanteroFechaSeleccionadaValor.text = selectedDate
        _binding!!.textViewDetalleVolanteroFechaSeleccionadaValor.setTextColor(ContextCompat.getColor(requireActivity(), R.color.red))
        _binding!!.sliderDetalleVolanteroTrayecto.trackActiveTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                R.color.lightGrey
            )
        )
        _binding!!.sliderDetalleVolanteroTrayecto.thumbStrokeColor = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireActivity(),
                R.color.lightGrey
            )
        )
            Toast.makeText(requireActivity(), "No hay registro con esa fecha.", Toast.LENGTH_SHORT)
            .show()
    }
    @SuppressLint("SetTextI18n")
    private fun cargarDatosDelVolantero(bundle: Usuario) {
        _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = false
        val fotoPerfil = bundle.fotoPerfil
        _binding!!.textViewDetalleVolanteroNombre.text = "Nombre: ${bundle.nombre} + ${bundle.apellidos}"
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1 // add 1 to get the correct month (0-based index)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val fechaDeHoy = when (month) {
            in 1..9 -> "$year-0$month-$day" // use string interpolation to create the selectedDate string
            else -> "$year-$month-$day"
        }

        _binding!!.textViewDetalleVolanteroFechaSeleccionadaValor.text = fechaDeHoy
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
            .apiKey(BuildConfig.DIRECTIONS_API_KEY)
            .build()
    }
    private fun sumarORestarValueDelSlider(num: Float) {
        if(!_binding!!.sliderDetalleVolanteroTrayecto.isEnabled) return
        val adjustedNum = if (num == -1f) -1f else 1f // adjust the value of num to be either -1 or 1
        val valorActualDelSlider = _binding!!.sliderDetalleVolanteroTrayecto.value
        when {
            adjustedNum == -1f && valorActualDelSlider == 0f -> return // prevent slider value from going below 0
            adjustedNum == 1f && valorActualDelSlider == 72f -> return // prevent slider value from going above 72
            else -> _binding!!.sliderDetalleVolanteroTrayecto.value = valorActualDelSlider + adjustedNum
        }
    }



    @Suppress("UNCHECKED_CAST")
    private fun iniciarValidacionesAntesDePintarPolyline(value: Float): Boolean {
        /** Parto limpiando tudo para pintar, re pintar o borrar segun el caso */
        map.clear()
        latLngsDeInteres.clear()

        /*Here i get de value selected in the slide and transform it to a valid hour */
        val minutes = (value * 10).toInt() % 60
        val hours = 10 + (value * 10).toInt() / 60
        val selectedHourInSlide = String.format("%02.0f:%02d", hours.toFloat(), minutes)
        Log.d("Slider", "Selected time: $selectedHourInSlide")

        /*
        Aquí obtengo el registro del volantero y lo parseo a un DocumentSnapshot para
        poder obtener su data. El motivo de este parseo es porque registroDelVolantero llega
        como Any y luego obtengo el registroJornada como ArrayList<Map<String, Map<*, *>>>
        donde string es fecha
        */
        val registroDelVolanteroParseado = registroDelVolanteroDocRef as DocumentSnapshot
        val registroJornada = registroDelVolanteroParseado.data!!["registroJornada"] as ArrayList<Map<String, Map<*, *>>>

        for (registro in registroJornada) {
            //aquí valido si el documento tiene registro de jornada en la fecha seleccionada en el calendario
            if (registro["fecha"].toString() == selectedDate) {
                //Aquí obtengo desde el map el listado de la hora de registro de un geopoints y, el listado de geopoints.
                val registroLatLngsDelDiaSeleccionado = registro["registroLatLngs"]
                val listadodeHorasDeRegistrodeNuevosGeopoints = registroLatLngsDelDiaSeleccionado!!["horasConRegistro"] as ArrayList<String>
                val geopointsRegistradosDelDia = registroLatLngsDelDiaSeleccionado["geopoints"] as ArrayList<GeoPoint>

                /** Aquí recorro el array de horas de registro de geopoints y valido si
                 la hora de registro en ciclo es menor a la hora seleccionada en el slide
                 con el objetivo de capturar, en cuanto se de el caso en el if, el index del
                 geopoint que se encuentra mas proximo, y mayor, a la hora seleccionada en el slide
                 con el objetivo de recorrer el array de geopoints desde el index en cuestión hasta el
                 ultimo elemento del array para, finalmente, pintarlos en el mapa, cuando se requiera.
                 */
                for (horaRegistrada in listadodeHorasDeRegistrodeNuevosGeopoints) {

                    if (validarSiPintarGeopointsSegunSuHoraDeRegistro(
                            horaRegistrada,
                            hours,
                            minutes
                        )
                    ) {
                        //index
                        var i = listadodeHorasDeRegistrodeNuevosGeopoints.indexOf(horaRegistrada)

                        while (i < listadodeHorasDeRegistrodeNuevosGeopoints.size) {
                            /** esta es la condicion de salida, si la hora de registro en ciclo es mayor a la
                             *  hora seleccionada en el slide, se rompe el ciclo. finalmente
                             *  se obtienen como 8 horas nada mas, por lo que se obtienen de a 8, 16,24 etc
                             *  *  el ultimo no sera multiplo de 8 1/10 de las veces*/
                            if (listadodeHorasDeRegistrodeNuevosGeopoints[i].substring(0, 2)
                                    .toFloat() == hours.toFloat() &&
                                listadodeHorasDeRegistrodeNuevosGeopoints[i].substring(3, 5)
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

                        /** una vez que tengo la lista a pintar, uso Directions y Distance APis para
                         pintar la ruta segun la regla de negocio */
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
                    val origin = latLngsDeInteres.first()
                    val destination = latLngsDeInteres.last()
                    val waypoints = latLngsDeInteres.subList(1, latLngsDeInteres.size)
                        .map { "${it?.latitude},${it?.longitude}" }
                        .toTypedArray()
                    val request = DirectionsApi.newRequest(geoApiContext)
                        .mode(TravelMode.WALKING)
                        .origin("${origin?.latitude},${origin?.longitude}") // Convert origin to "latitude,longitude" string
                        .destination("${destination?.latitude},${destination?.longitude}") // Convert destination to "latitude,longitude" string
                        .waypoints(*waypoints) // Convert waypoints to a list of "latitude,longitude" strings
                        .optimizeWaypoints(true)
                        .await()

                    /* Distance Matrix Api Require mainscope to work*/
                    withContext(Dispatchers.Main){
                        // Here i check if the request was successful
                        if (request?.routes != null && request.routes.isNotEmpty()) {
                            val encodedPolyline = request.routes[0].overviewPolyline.encodedPath
                            // Decode the polyline to a list of LatLng points
                            val decodedPath = PolyUtil.decode(encodedPolyline)
                            val closestPoints = mutableListOf<LatLng>()

                            // Find the closest point in the decoded path for each point in latLngsDeInteres
                            for (latLng in latLngsDeInteres) {
                                var closestIndex = 0
                                var closestDistance = Float.MAX_VALUE
                                decodedPath.forEachIndexed { i, pathLatLng ->
                                    val distance = FloatArray(1)
                                    Location.distanceBetween(
                                        latLng!!.latitude, latLng.longitude,
                                        pathLatLng.latitude, pathLatLng.longitude, distance
                                    )
                                    if (distance[0] < closestDistance) {
                                        closestIndex = i
                                        closestDistance = distance[0]
                                    }
                                }
                                closestPoints.add(decodedPath[closestIndex])
                            }
                            polylineOptions = PolylineOptions().width(10f)
                            var distanceRecorrida = 0
                            var indexGuardadoInicio = 0
                            var indexGuardadoFin = 0

                            decodedPath.forEachIndexed { i, latLng ->
                                // Create a PolylineOptions object and configure its appearance
                                if (i < decodedPath.size - 1) {
                                    val latLng1 = "${latLng?.latitude},${latLng?.longitude}"
                                    val latLng2 = "${decodedPath[i + 1]?.latitude},${decodedPath[i + 1]?.longitude}"
                                    val distanceMatrixResponse = _viewModel.obtenerDistanciaEntreLatLngs(latLng1, latLng2, BuildConfig.DISTANCE_MATRIX_API_KEY)
                                    val distance = distanceMatrixResponse.rows[0].elements[0].distance?.value
                                    println(distance)
                                    distanceRecorrida += distance!!
                                    // Set the color based on the distance
                                    if(closestPoints.contains(LatLng(latLng.latitude, latLng.longitude))){
                                        indexGuardadoInicio = indexGuardadoFin
                                        indexGuardadoFin = i
                                        val color = when (distanceRecorrida) {
                                            in 0..500 -> Color.RED
                                            in 500..900 ->Color.YELLOW
                                            else -> Color.GREEN
                                        }
                                        val listAux = decodedPath.subList(indexGuardadoInicio,indexGuardadoFin)
                                        polylineOptions.addAll(listAux).color(color)
                                        map.addPolyline(polylineOptions)
                                        polylineOptions = PolylineOptions().width(10f)
                                        distanceRecorrida = 0
                                    }
                                } else {
                                    polylineOptions.add(latLng)
                                    map.addPolyline(polylineOptions)
                                    polylineOptions = PolylineOptions().width(10f)
                                }
                            }
                            // Add the Polyline to the map
                        } else {
                            Snackbar.make(_binding!!.root, "Error: request es null", Snackbar.LENGTH_LONG).show()
                            Log.e("DIRECTIONS_API_ERROR", "Error: request es null")
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