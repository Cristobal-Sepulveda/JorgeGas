package com.example.conductor.ui.map

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.conductor.R
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.example.conductor.utils.*
import com.example.conductor.utils.Constants.ACTION_MAP_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.cameraDefaultZoom
import com.example.conductor.utils.Constants.defaultLocation
import com.example.conductor.utils.SharedPreferenceUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.LocalTime
import java.time.Duration

class MapFragment : BaseFragment(), OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var iniciandoSnapshotListener: ListenerRegistration
    private var volanterosActivosAMarcarEnElMapa: HashMap<String,Marker> = HashMap()
    private var rastroDeLosVolanterosAMarcarEnElMapa: HashMap<String, Polyline> = HashMap()
    private var listadoDeHorasDeRegistroDeNuevosGeopoints: HashMap<String,ArrayList<String>> = HashMap()

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private var locationServiceBound = false
    private var lastKnownLocation: Location? = null
    private var backPressedCallback: OnBackPressedCallback? = null
    // Listens for location broadcasts from LocationService.

    private inner class LocationServiceBroadcastReceiverMap : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_MAP_LOCATION_BROADCAST) {
                val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
                if (location != null) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            try {
                                /*obtengo el registro del usuario en google cloud, con el objetivo
                                * de registrar la nueva localizacion*/
                                val registroTrayectoVolanterosUsuario = cloudDB
                                    .collection("RegistroTrayectoVolanteros")
                                    .document(Constants.firebaseAuth.currentUser!!.uid)
                                    .get().await()

                                val dataDocumento = registroTrayectoVolanterosUsuario.data
                                val fechaDeHoy = LocalDate.now().toString()
                                /*Si el documento existe...*/
                                if (dataDocumento != null) {
                                    val registroJornada =
                                        dataDocumento["registroJornada"] as ArrayList<Map<String, *>>
                                    for (registroDeUnDia in registroJornada) {
                                        if (registroDeUnDia["fecha"] == fechaDeHoy) {
                                            val registroLatLngs =
                                                registroDeUnDia["registroLatLngs"] as Map<*, *>
                                            val horasRegistradas =
                                                registroLatLngs["horasConRegistro"] as ArrayList<String>
                                            horasRegistradas.add(LocalTime.now().toString())
                                            val geoPointRegistrados =
                                                registroLatLngs["geopoints"] as ArrayList<GeoPoint>
                                            geoPointRegistrados.add(
                                                GeoPoint(
                                                    location.latitude,
                                                    location.longitude
                                                )
                                            )
                                            val documentActualizado = mapOf(
                                                "registroJornada" to registroJornada,
                                            )

                                            cloudDB.collection("RegistroTrayectoVolanteros")
                                                .document(Constants.firebaseAuth.currentUser!!.uid)
                                                .update(documentActualizado)
                                            return@withContext
                                        }
                                    }
                                    registroJornada.add(
                                        mapOf(
                                            "fecha" to fechaDeHoy,
                                            "registroLatLngs" to mapOf(
                                                "horasConRegistro" to arrayListOf(
                                                    LocalTime.now().toString()
                                                ),
                                                "geopoints" to arrayListOf(
                                                    GeoPoint(
                                                        location.latitude,
                                                        location.longitude
                                                    )
                                                )
                                            )
                                        )
                                    )
                                    val nuevoRegistro = mapOf(
                                        "registroJornada" to registroJornada,
                                    )
                                    cloudDB.collection("RegistroTrayectoVolanteros")
                                        .document(Constants.firebaseAuth.currentUser!!.uid)
                                        .update(nuevoRegistro)
                                } else {
                                    cloudDB.collection("RegistroTrayectoVolanteros")
                                        .document(Constants.firebaseAuth.currentUser!!.uid)
                                        .set(
                                            mapOf(
                                                "registroJornada" to arrayListOf(
                                                    mapOf(
                                                        "fecha" to fechaDeHoy,
                                                        "registroLatLngs" to mapOf(
                                                            "horasConRegistro" to arrayListOf(
                                                                LocalTime.now().toString()
                                                            ),
                                                            "geopoints" to arrayListOf(
                                                                GeoPoint(
                                                                    location.latitude,
                                                                    location.longitude
                                                                )
                                                            )
                                                        )
                                                    )
                                                ),
                                                "estaActivo" to true,
                                                "nombreCompleto" to _viewModel.usuarioDesdeSqlite,
                                                "rol" to "Administrador"
                                            )
                                        )
                                }
                            }catch (e: Exception){
                                Handler(Looper.getMainLooper()).post{
                                    Toast.makeText(
                                        requireActivity(),
                                        "No se pudo guardar la ubicación: $e",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private var locationServiceBroadcastReceiverMap = LocationServiceBroadcastReceiverMap()

    /////////////////////////////////////////////////////////////////////////
    // Provides location updates for while-in-use feature.
    private var locationService: LocationService? = null

    // Monitors connection to the while-in-use service.
    private val locationServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as LocationService.LocalBinder
            locationService = binder.service
            locationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationService = null
            locationServiceBound = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        setHasOptionsMenu(true)
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        //Obteniendo sharedPreferences y poniendo un listener a cualquier cambio en esta key
        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        //bindeando el servicio al fragment y registrando el broadcast receiver
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)

        requireActivity().bindService(
            serviceIntent,
            locationServiceConnection,
            Context.BIND_AUTO_CREATE
        )
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(
                locationServiceBroadcastReceiverMap,
                IntentFilter(Constants.ACTION_MAP_LOCATION_BROADCAST)
            )


        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                _viewModel.obtenerUsuariosDesdeSqlite()
            }
        }

        _binding!!.fabVistaOriginal.setOnClickListener{
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(defaultLocation.latitude, defaultLocation.longitude)
                , cameraDefaultZoom.toFloat())
            map.animateCamera(cameraUpdate, 300, null)
        }

        _binding!!.fabMapActivarLocalizacion.setOnClickListener{
            iniciarODetenerLocationService()
        }

        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        //esto detiene el snapshot listener del RegistroTrayectoVolanteros de la cloudDB
        iniciandoSnapshotListener.remove()
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(locationServiceBroadcastReceiverMap)
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }
        SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
        locationService?.unsubscribeToLocationUpdatesMapFragment()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
        setMapStyle(map)
        markingPolygons()

        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                iniciarSnapshotListenerDelRegistroTrayectoVolanteros()
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.clear()
        requireActivity().menuInflater.inflate(R.menu.map_menu, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_mapMenu_estadoActual -> {
                val action = MapFragmentDirections.actionNavigationMapToEstadoActualFragment()
                findNavController().navigate(action)
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(
                sharedPreferences!!.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
                )
            )
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _binding!!.fabMapActivarLocalizacion.setImageResource(R.drawable.baseline_stop_24)
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = false
            }
            // Disable the ActionBar
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(false)
            backPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do nothing to disable the back button
                }
            }

            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback!!)
        } else {
            _binding!!.fabMapActivarLocalizacion.setImageResource(R.drawable.baseline_satellite_alt_24)
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
            for (i in 0 until bottomNavigationView.menu.size()) {
                bottomNavigationView.menu.getItem(i).isEnabled = true
            }

            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            backPressedCallback?.remove()
        }
    }

    private fun startingPermissionCheck(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if(isPermissionGranted){
            try{
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()){ task ->
                    lastKnownLocation = task.result
                    Log.i("getDeviceLocation", task.result?.longitude.toString())
                    if (lastKnownLocation != null) {
                        //zoom to the user location after taking his permission
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    defaultLocation.latitude, defaultLocation.longitude
                                ),
                                cameraDefaultZoom.toFloat()
                            )
                        )
                    }else{
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    defaultLocation.latitude, defaultLocation.longitude
                                ),
                                cameraDefaultZoom.toFloat()
                            )
                        )
                        Toast.makeText(requireActivity(), "No se pudo obtener la ubicación actual", Toast.LENGTH_LONG).show()
                    }
                }

            }catch(e: SecurityException){
                _binding!!.map.isGone = true
                _binding!!.imageviewMapaSinPermisos.isGone = false

            }
        }else {
            _binding!!.map.isGone = true
            _binding!!.imageviewMapaSinPermisos.isGone = false
        }
    }

    private fun markingPolygons(){
        for((index,polygon) in polygonsList.withIndex()){
            val newPolygon = PolygonOptions()
            .addAll(polygon)
            .fillColor(polygonsColor[index])
            .strokeColor(Color.argb(50,255,255,255))
            map.addPolygon(newPolygon)
        }
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

    private fun iniciarSnapshotListenerDelRegistroTrayectoVolanteros() {
        val colRef = cloudDB.collection("RegistroTrayectoVolanteros")
        iniciandoSnapshotListener = colRef.addSnapshotListener { snapshot, FirebaseFirestoreException ->
            if (FirebaseFirestoreException != null) {
                _viewModel.showToast.value = "Error: " + FirebaseFirestoreException.localizedMessage
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                for (documentChange in snapshot.documentChanges) {
                    when (documentChange.type) {

                        DocumentChange.Type.ADDED -> {
                            val registroJornada = documentChange.document.data["registroJornada"] as List<Map<String, Map<String ,List<GeoPoint>>>>
                            val estaActivo = documentChange.document.data["estaActivo"] as Boolean

                            for (element in registroJornada) {
                                if(element["fecha"].toString() == LocalDate.now().toString() && estaActivo){
                                    Log.i("DocumentChange", "ADDED: ${documentChange.document.data["nombreCompleto"]} ${element["fecha"]}")
                                    val listadoDeGeoPoints = element["registroLatLngs"]!!["geopoints"]!!
                                    listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id] =
                                        element["registroLatLngs"]!!["horasConRegistro"]!! as ArrayList<String>
                                    val polyLineOptions = PolylineOptions()

                                    val geoPoints = if (listadoDeGeoPoints.size <= 4) {
                                        listadoDeGeoPoints
                                    } else {
                                        listadoDeGeoPoints.subList(listadoDeGeoPoints.size - 4, listadoDeGeoPoints.size)
                                    }

                                    geoPoints.forEach{ geoPoint ->
                                        polyLineOptions.add(LatLng(geoPoint.latitude,geoPoint.longitude))
                                    }

                                    if (listadoDeGeoPoints.size > 4) {
                                        val sublist = listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.subList(
                                            listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.size - 4,
                                            listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.size
                                        )
                                        listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id] =
                                            ArrayList(sublist)
                                    }

                                    var tiempoEnRecorrerTramo = 0f
                                    var distanceRecorrida = 0
                                    geoPoints.forEachIndexed{ i, latLng ->
                                        if(i == geoPoints.size-1){
                                            return@forEachIndexed
                                        }
                                        val latLng1 = Location("")
                                        latLng1.latitude = latLng.latitude
                                        latLng1.longitude = latLng.longitude
                                        val latLng2 = Location("")
                                        latLng2.latitude = geoPoints[i+1].latitude
                                        latLng2.longitude = geoPoints[i+1].longitude

                                        val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                                        val tiempoLatLng1 = LocalTime.parse(listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!![i])
                                        val tiempoLatLng2 = LocalTime.parse(listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!![i+1])
                                        val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()
                                        tiempoEnRecorrerTramo += timeBetweenLatLngs
                                        distanceRecorrida += distanceBetweenLatLngs
                                    }

                                    val rangoMayor = (tiempoEnRecorrerTramo/1000 * 0.75).toInt()
                                    val rangoMenor = (tiempoEnRecorrerTramo/1000 * 0.3).toInt()
                                    val rangoMaximoHumano = rangoMayor*4
                                    // Set the color based on the distance
                                    val color = when (distanceRecorrida) {
                                        in 0..rangoMenor -> Color.RED
                                        in rangoMenor..rangoMayor -> Color.YELLOW
                                        in rangoMayor..rangoMaximoHumano -> Color.GREEN
                                        else -> Color.BLUE
                                    }

                                    // Set the color and width of the polyline
                                    polyLineOptions.color(color)
                                    polyLineOptions.width(10f)

                                    Log.i("ADDED", "$polyLineOptions")
                                    val newPolyline = map.addPolyline(polyLineOptions)
                                    rastroDeLosVolanterosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, newPolyline)

                                    val nuevoVolanteroGeoPoint = element["registroLatLngs"]!!["geopoints"]!!.last()

                                    val markerIcon = when (color) {
                                        Color.RED -> R.drawable.ic_marker_volantero_red
                                        Color.YELLOW -> R.drawable.ic_marker_volantero_yellow
                                        Color.GREEN -> R.drawable.ic_marker_volantero_green
                                        else -> R.drawable.ic_marker_volantero_blue
                                    }

                                    when(documentChange.document.data["rol"]){
                                        "Administrador" -> {
                                            val marker = map.addMarker(MarkerOptions()
                                                .position(LatLng(nuevoVolanteroGeoPoint.latitude,nuevoVolanteroGeoPoint.longitude))
                                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.supervisor_1)))
                                                .title(documentChange.document.data["nombreCompleto"].toString())
                                            )
                                            volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                        }
                                        else -> {
                                            val marker = map.addMarker(MarkerOptions()
                                                .position(LatLng(nuevoVolanteroGeoPoint.latitude,nuevoVolanteroGeoPoint.longitude))
                                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(markerIcon)))
                                                .title(documentChange.document.data["nombreCompleto"].toString())
                                            )

                                            volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                        }
                                    }
                                }
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            val registroJornada = documentChange.document.data["registroJornada"] as List<Map<String, Map<String ,List<GeoPoint>>>>
                            val estaActivo = documentChange.document.data["estaActivo"] as Boolean

                            if(!estaActivo){
                                for(mapIdMarker in volanterosActivosAMarcarEnElMapa){
                                    if(mapIdMarker.key == documentChange.document.id) {
                                        mapIdMarker.value.remove()
                                    }
                                }
                                volanterosActivosAMarcarEnElMapa.remove(documentChange.document.id)
                                return@addSnapshotListener
                            }

                            for (element in registroJornada) {
                                if(element["fecha"].toString() == LocalDate.now().toString()){
                                    for(mapIdMarker in volanterosActivosAMarcarEnElMapa){
                                        if(mapIdMarker.key == documentChange.document.id)
                                          mapIdMarker.value.remove()
                                    }
                                    for(polyLine in rastroDeLosVolanterosAMarcarEnElMapa){
                                        if(polyLine.key == documentChange.document.id){
                                            polyLine.value.remove()
                                        }
                                    }

                                    volanterosActivosAMarcarEnElMapa.remove(documentChange.document.id)
                                    rastroDeLosVolanterosAMarcarEnElMapa.remove(documentChange.document.id)

                                    val listadoDeGeoPoints = element["registroLatLngs"]!!["geopoints"]!!
                                    val polyLineOptions = PolylineOptions()
                                    val geoPoints = if (listadoDeGeoPoints.size <= 4) {
                                        listadoDeGeoPoints
                                    } else {
                                        listadoDeGeoPoints.subList(listadoDeGeoPoints.size - 4, listadoDeGeoPoints.size)
                                    }
                                    geoPoints.forEach{ geoPoint ->
                                        polyLineOptions.add(LatLng(geoPoint.latitude,geoPoint.longitude))
                                    }

                                    listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id] =
                                        element["registroLatLngs"]!!["horasConRegistro"]!! as ArrayList<String>
                                    if (listadoDeGeoPoints.size > 4) {
                                        val sublist = listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.subList(
                                            listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.size - 4,
                                            listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!!.size
                                        )
                                        listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id] =
                                            ArrayList(sublist)
                                    }

                                    var tiempoEnRecorrerTramo = 0f
                                    var distanceRecorrida = 0
                                    geoPoints.forEachIndexed{ i, latLng ->
                                        if(i == geoPoints.size-1){
                                            return@forEachIndexed
                                        }
                                        val latLng1 = Location("")
                                        latLng1.latitude = latLng.latitude
                                        latLng1.longitude = latLng.longitude
                                        val latLng2 = Location("")
                                        latLng2.latitude = geoPoints[i+1].latitude
                                        latLng2.longitude = geoPoints[i+1].longitude
                                        val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                                        val tiempoLatLng1 = LocalTime.parse(listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!![i])
                                        val tiempoLatLng2 = LocalTime.parse(listadoDeHorasDeRegistroDeNuevosGeopoints[documentChange.document.id]!![i+1])
                                        val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()
                                        tiempoEnRecorrerTramo += timeBetweenLatLngs
                                        distanceRecorrida += distanceBetweenLatLngs
                                    }

                                    val rangoMayor = (tiempoEnRecorrerTramo/1000 * 0.75).toInt()
                                    val rangoMenor = (tiempoEnRecorrerTramo/1000 * 0.3).toInt()
                                    val rangoMaximoHumano = rangoMayor*4

                                    Log.i("MapFragment", "Rango mayor: $rangoMayor")
                                    Log.i("MapFragment", "Rango menor: $rangoMenor")
                                    Log.i("MapFragment", "Rango maximo humano: $rangoMaximoHumano")
                                    Log.i("MapFragment", "Distancia recorrida: $distanceRecorrida")
                                    Log.i("MapFragment", "Tiempo en recorrer tramo: $tiempoEnRecorrerTramo")

                                    // Set the color based on the distance
                                    val color = when (distanceRecorrida) {
                                        in 0..rangoMenor -> Color.RED
                                        in rangoMenor..rangoMayor -> Color.YELLOW
                                        in rangoMayor..rangoMaximoHumano -> Color.GREEN
                                        else -> Color.BLUE
                                    }

                                    // Set the color and width of the polyline
                                    polyLineOptions.color(color)
                                    polyLineOptions.width(10f)

                                    Log.i("MODIFIED", "$polyLineOptions")
                                    val newPolyline = map.addPolyline(polyLineOptions)
                                    rastroDeLosVolanterosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, newPolyline)

                                    val nuevoVolanteroGeoPoint = element["registroLatLngs"]!!["geopoints"]!!.last()
                                    val markerIcon = when (color) {
                                        Color.RED -> R.drawable.ic_marker_volantero_red
                                        Color.YELLOW -> R.drawable.ic_marker_volantero_yellow
                                        Color.GREEN -> R.drawable.ic_marker_volantero_green
                                        else -> R.drawable.ic_marker_volantero_blue
                                    }

                                    when(documentChange.document.data["rol"]){
                                        "Administrador" -> {
                                            val marker = map.addMarker(MarkerOptions()
                                                .position(LatLng(nuevoVolanteroGeoPoint.latitude,nuevoVolanteroGeoPoint.longitude))
                                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.supervisor_1)))
                                                .title(documentChange.document.data["nombreCompleto"].toString())
                                            )
                                            volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                        }
                                        else -> {
                                            val marker = map.addMarker(MarkerOptions()
                                                .position(LatLng(nuevoVolanteroGeoPoint.latitude,nuevoVolanteroGeoPoint.longitude))
                                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(markerIcon)))
                                                .title(documentChange.document.data["nombreCompleto"].toString())
                                            )
                                            volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                        }
                                    }
                                    Log.i("MODIFIED","La ubicación de ${documentChange.document.data["nombreCompleto"]} ha sido actualizada")
                                }
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.i("DocumentChange", "Removed")
                            for(mapIdMarker in volanterosActivosAMarcarEnElMapa){
                                if(mapIdMarker.key == documentChange.document.id)
                                    mapIdMarker.value.remove()
                            }
                            try{
                                volanterosActivosAMarcarEnElMapa.remove(documentChange.document.id)
                            }catch (e: Exception){
                                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun iniciarODetenerLocationService() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val enabled =
                    sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                Log.i("MapFragment", "Enabled: $enabled")
                if (enabled) {
                    if (_viewModel.editarEstadoVolantero(false)) {
                        locationService?.unsubscribeToLocationUpdatesMapFragment()
                        SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
                        Snackbar.make(_binding!!.root, "El servicio de localización ha sido detenido.", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val snackbar = Snackbar.make(
                            _binding!!.root,
                            "El servicio no será desactivado debido a que no se ha podido configurar al usuario como inactivo en la nube. Intentelo Nuevamente.",
                            Snackbar.LENGTH_INDEFINITE
                        )
                        snackbar.setAction("Aceptar") {
                            snackbar.dismiss()
                        }
                        snackbar.show()
                    }
                } else {
                    if (_viewModel.editarEstadoVolantero(true)) {
                        locationService?.subscribeToLocationUpdatesMapFragment()
                        SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), true)
                        Log.i("MapFragment", "se inicio el servicio de localizacion")
                    } else {
                        val snackbar = Snackbar.make(
                            _binding!!.root,
                            "El servicio no será desactivado debido a que no se ha podido configurar al usuario como activo en la nube. Intentelo Nuevamente.",
                            Snackbar.LENGTH_INDEFINITE
                        )
                        snackbar.setAction("Aceptar") {
                            snackbar.dismiss()
                        }
                        snackbar.show()
                    }
                }
            }
        }
    }

    private fun getBitmap(svgResource: Int): Bitmap {
        val svg = AppCompatResources.getDrawable(requireActivity(), svgResource)
        val bitmap = Bitmap.createBitmap(svg!!.intrinsicWidth, svg.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.setBounds(0, 0, canvas.width, canvas.height)
        svg.draw(canvas)
        return bitmap
    }

    private fun pintarPolyline(){

    }
}


