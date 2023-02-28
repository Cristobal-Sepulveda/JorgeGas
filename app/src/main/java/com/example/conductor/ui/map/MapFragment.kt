package com.example.conductor.ui.map

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.example.conductor.utils.*
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.LocalTime


class MapFragment : BaseFragment(), OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var iniciandoSnapshotListener: ListenerRegistration
    private var volanterosActivosAMarcarEnElMapa: HashMap<String,Marker> = HashMap()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences
    private var locationServiceBound = false
    private var lastKnownLocation: Location? = null
    // Listens for location broadcasts from LocationService.
    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /*aqui obtengo la localizacion*/
            val location = intent.getParcelableExtra<Location>(Constants.EXTRA_LOCATION)
            /*si la ubicacion no es nula*/
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
                                            "estaActivo" to true,
                                            "nombreCompleto" to _viewModel.usuarioDesdeSqlite,
                                            "registroJornada" to registroJornada,
                                            "rol" to "Administrador"
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
                                    "estaActivo" to true,
                                    "nombreCompleto" to _viewModel.usuarioDesdeSqlite,
                                    "rol" to "Administrador"
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
                        } catch (e: Exception) {
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

    private var locationServiceBroadcastReceiver = LocationServiceBroadcastReceiver()

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
                locationServiceBroadcastReceiver,
                IntentFilter(Constants.ACTION_LOCATION_BROADCAST)
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

    override fun onDestroyView() {
        super.onDestroyView()
        //esto detiene el snapshot listener del RegistroTrayectoVolanteros de la cloudDB
        iniciandoSnapshotListener.remove()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
/*        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(
                sharedPreferences!!.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false
                )
            )
        }*/
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
                        map.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    )
                                )
                                .title("Marker in your actual location")
                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.supervisor_1)))
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
        val docRef = cloudDB.collection("RegistroTrayectoVolanteros")
        iniciandoSnapshotListener = docRef.addSnapshotListener { snapshot, FirebaseFirestoreException ->
            if (FirebaseFirestoreException != null) {
                _viewModel.showToast.value = "Error: " + FirebaseFirestoreException.localizedMessage
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                for (documentChange in snapshot.documentChanges) {
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.i("DocumentChange", "ADDED")
                            val listOfGeopoints = documentChange.document.data["registroJornada"] as List<Map<String, Map<String ,List<GeoPoint>>>>
                            Log.i("DocumentChange", "ADDED: $listOfGeopoints")
                            val estaActivo = documentChange.document.data["estaActivo"] as Boolean
                            for (element in listOfGeopoints) {
                                if(element["fecha"].toString() == LocalDate.now().toString() && estaActivo){
                                    val nuevoVolanteroGeopoint = element["registroLatLngs"]!!["geopoints"]!!.last()
                                    Log.i("DocumentChange", "ADDED: $nuevoVolanteroGeopoint")
                                    if(documentChange.document.data["rol"] == "Administrador"){
                                        val marker = map.addMarker(MarkerOptions()
                                            .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.supervisor_1)))
                                            .title(documentChange.document.data["nombreCompleto"].toString())
                                        )
                                        volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                    }else{
                                        val marker = map.addMarker(MarkerOptions()
                                            .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_marker_volantero)))
                                            .title(documentChange.document.data["nombreCompleto"].toString())
                                        )
                                        volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                    }
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.i("DocumentChange", "MODIFIED")
                            val listOfGeopoints = documentChange.document.data["registroJornada"] as List<Map<String, Map<String ,List<GeoPoint>>>>
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
                            for (element in listOfGeopoints) {
                                if(element["fecha"].toString() == LocalDate.now().toString() && estaActivo){
                                    for(mapIdMarker in volanterosActivosAMarcarEnElMapa){
                                        if(mapIdMarker.key == documentChange.document.id)
                                          mapIdMarker.value.remove()
                                    }
                                    val nuevoVolanteroGeopoint = element["registroLatLngs"]!!["geopoints"]!!.last()
                                    volanterosActivosAMarcarEnElMapa.remove(documentChange.document.id)
                                    if(documentChange.document.data["rol"] == "Administrador"){
                                        val marker = map.addMarker(MarkerOptions()
                                            .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.supervisor_1)))
                                            .title(documentChange.document.data["nombreCompleto"].toString())
                                        )
                                        volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                    }else{
                                        val marker = map.addMarker(MarkerOptions()
                                            .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                            .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_marker_volantero)))
                                            .title(documentChange.document.data["nombreCompleto"].toString())
                                        )
                                        volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
                                    }
                                    Log.i("Firestore","La ubicación de un usuario ha sido actualizada")
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
                if (!enabled) {
                    if (_viewModel.editarEstadoVolantero(false)) {
                        locationService?.unsubscribeToLocationUpdates()
                        notificationGenerator(
                            requireActivity(),
                            "El servicio de localización ha sido detenido."
                        )
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
                        locationService?.subscribeToLocationUpdates()
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
}


