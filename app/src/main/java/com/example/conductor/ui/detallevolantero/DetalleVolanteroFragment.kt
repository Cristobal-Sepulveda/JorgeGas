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
import com.example.conductor.utils.Constants
import com.example.conductor.databinding.FragmentDetalleVolanteroBinding
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
import com.google.maps.GeoApiContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private var tiemposEntreLatLngDeInteresInicialYFinal = mutableListOf<String>()
    private lateinit var geoApiContext: GeoApiContext
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var bundle: Usuario
    private lateinit var listadoDeHorasDeRegistrodeNuevosGeopoints: ArrayList<String>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVolanteroBinding.inflate(inflater, container, false)
        bundle = DetalleVolanteroFragmentArgs.fromBundle(requireArguments()).usuarioDetails
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_detalleVolantero_googleMaps)
                as? SupportMapFragment)?.getMapAsync(this)

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

        _binding!!.buttonDetalleVolanteroCalendario.setOnClickListener {
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

    @SuppressLint("SuspiciousIndentation")
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
        _binding!!.textViewDetalleVolanteroFechaSeleccionadaAlerta.setText(R.string.no_hay_registros_con_esa_fecha)
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
        _binding!!.textViewDetalleVolanteroNombre.text = "${bundle.nombre} ${bundle.apellidos}"
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
        tiemposEntreLatLngDeInteresInicialYFinal.clear()

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
                listadoDeHorasDeRegistrodeNuevosGeopoints = registroLatLngsDelDiaSeleccionado!!["horasConRegistro"] as ArrayList<String>
                val geopointsRegistradosDelDia = registroLatLngsDelDiaSeleccionado["geopoints"] as ArrayList<GeoPoint>

                /** Aquí recorro el array de horas de registro de geopoints y valido si
                 la hora de registro en ciclo es menor a la hora seleccionada en el slide
                 con el objetivo de capturar, en cuanto se de el caso en el if, el index del
                 geopoint que se encuentra mas proximo, y mayor, a la hora seleccionada en el slide
                 con el objetivo de recorrer el array de geopoints desde el index en cuestión hasta el
                 ultimo elemento del array para, finalmente, pintarlos en el mapa, cuando se requiera.
                 */
                for (horaRegistrada in listadoDeHorasDeRegistrodeNuevosGeopoints) {
                    if (validarSiPintarGeopointsSegunSuHoraDeRegistro(
                            horaRegistrada,
                            hours,
                            minutes
                        )
                    ) {
                        //index
                        Log.i("DetalleVolanteroFragment", "horaregistrada: $horaRegistrada")
                        var i = listadoDeHorasDeRegistrodeNuevosGeopoints.indexOf(horaRegistrada)

                        while (i < listadoDeHorasDeRegistrodeNuevosGeopoints.size) {
                            /** esta es la condicion de salida, si la hora de registro en ciclo es mayor a la
                             *  hora seleccionada en el slide, se rompe el ciclo. finalmente
                             *  se obtienen como 8 horas nada mas, por lo que se obtienen de a 8, 16,24 etc
                             *  *  el ultimo no sera multiplo de 8 1/10 de las veces*/
                            if (listadoDeHorasDeRegistrodeNuevosGeopoints[i].substring(0, 2)
                                    .toFloat() == hours.toFloat() &&
                                listadoDeHorasDeRegistrodeNuevosGeopoints[i].substring(3, 5)
                                    .toFloat() > minutes.toFloat()
                            ) {
                                break
                            }
                            val latitud = geopointsRegistradosDelDia[i].latitude
                            val longitud = geopointsRegistradosDelDia[i].longitude
                            val latLng = LatLng(latitud, longitud)
                            latLngsDeInteres.add(latLng)
                            tiemposEntreLatLngDeInteresInicialYFinal.add(listadoDeHorasDeRegistrodeNuevosGeopoints[i])
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
        lifecycleScope.launch{
            withContext(Dispatchers.Main) {
                try {
                    Log.i("DetalleVolanteroFragment", "pintarPolyline: ${latLngsDeInteres.size}")
                    Log.i("DetalleVolanteroFragment", "${latLngsDeInteres.first()}")
                    Log.i("DetalleVolanteroFragment", "${latLngsDeInteres.last()}")

                    /* Distance Matrix Api Require mainscope to work*/
                    polylineOptions = PolylineOptions().width(10f)
                    var distanceRecorrida = 0
                    var topeParaDibujar = 0
                    val listAux = mutableListOf<LatLng?>()

                    latLngsDeInteres.forEachIndexed { i, latLng ->
                        if (i == latLngsDeInteres.size - 1) {
                            return@forEachIndexed
                        }
                        val latLng1 = Location("")
                        latLng1.latitude = latLng?.latitude ?: 0.0
                        latLng1.longitude = latLng?.longitude ?: 0.0
                        val latLng2 = Location("")
                        latLng2.latitude = latLngsDeInteres[i + 1]?.latitude ?: 0.0
                        latLng2.longitude = latLngsDeInteres[i + 1]?.longitude ?: 0.0
                        val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                        println(distanceBetweenLatLngs)
                        distanceRecorrida += distanceBetweenLatLngs
                        topeParaDibujar++
                        listAux.add(latLng)

                        if (topeParaDibujar == 23) {
                            // Set the color based on the distance
                            val color = when (distanceRecorrida) {
                                in 0..16 -> Color.RED
                                in 16..20 -> Color.YELLOW
                                else -> Color.GREEN
                            }
                            polylineOptions.addAll(listAux).color(color)
                            map.addPolyline(polylineOptions)
                            polylineOptions = PolylineOptions().width(10f)
                            Log.i("DetalleVolanteroFragment", "pintarPolyline: $distanceRecorrida")
                            distanceRecorrida = 0
                            topeParaDibujar = 0
                            listAux.clear()
                        }
                    }
                } catch (e: Exception) {
                    Snackbar.make(_binding!!.root, "$e", Snackbar.LENGTH_LONG).show()
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