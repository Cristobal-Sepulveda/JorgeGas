package com.example.conductor.ui.vistageneral

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.example.conductor.BuildConfig
import com.example.conductor.R
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.data.data_objects.dbo.LatLngYHoraActualDBO
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import com.example.conductor.ui.detallebonosderesponsabilidad.DetalleBonosDeResponsabilidadDialogFragment
import com.example.conductor.utils.*
import com.example.conductor.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.EXTRA_LOCATION
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.SharedPreferenceUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.koin.android.ext.android.inject
import java.io.IOException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class VistaGeneralFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener,
    OnMapReadyCallback {

    private var _binding: FragmentVistaGeneralBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private var locationServiceBound = false
    private lateinit var iniciandoSnapshotListener: ListenerRegistration
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private var latLngsDeInteres = mutableListOf<LatLng?>()
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var listadoDeHorasDeRegistrodeNuevosGeopoints: ArrayList<String>
    private lateinit var marker: Marker
    // Listens for location broadcasts from LocationService.
    private inner class LocationServiceBroadcastReceiverVistaGeneral : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ACTION_LOCATION_BROADCAST) {
                return
            }
            /*aqui obtengo la localizacion*/
            val location = intent.getParcelableExtra<Location>(EXTRA_LOCATION) ?: return

            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        val phoneCurrentHour = LocalTime.now().toString()
                        Log.i("Localizacion recibida", phoneCurrentHour)
                        _viewModel.guardarLatLngYHoraActualEnRoom(
                            LatLngYHoraActualDBO(latitude,longitude,phoneCurrentHour)
                        )

                        val fechaDeHoy = LocalDate.now().toString()
                        /*obtengo el registro del usuario en google cloud, con el objetivo
                        * de registrar la nueva localizacion*/
                        val registroTrayectoVolanterosUsuario = cloudDB
                            .collection("RegistroTrayectoVolanteros")
                            .document(firebaseAuth.currentUser!!.uid)
                            .get().await()

                        val dataDocumento = registroTrayectoVolanterosUsuario.data

                        /*Si el documento existe...*/
                        if (dataDocumento != null) {
                            val registroJornada =
                                dataDocumento["registroJornada"] as ArrayList<Map<String, *>>

                            /* Aqui recorro el documento y busco si hay algun registro que tenga
                            * la fecha de hoy, si lo hay registro allí*/
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
                                        "nombreCompleto" to _viewModel.usuarioDesdeSqlite,
                                        "registroJornada" to registroJornada,
                                        "rol" to "Volantero"
                                    )
                                    if(dataDocumento["estaActivo"] == false){
                                        documentActualizado.plus("estaActivo" to true)
                                    }

                                    cloudDB.collection("RegistroTrayectoVolanteros")
                                        .document(firebaseAuth.currentUser!!.uid)
                                        .update(documentActualizado)
                                    return@withContext
                                }
                            }
                            /* Como ya recorri el documento y no encontre un registro con fecha de hoy
                               * creo uno*/
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
                            )
                            cloudDB.collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
                                .update(nuevoRegistro)
                        }
                        else {
                            cloudDB.collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
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
                                        "rol" to "Volantero"
                                    )
                                )
                        }
                    } catch (e: Exception) {
                        Log.i("Error", e.toString())
                    }
                }
            }
        }
    }
    private var locationServiceBroadcastReceiverVistaGeneral = LocationServiceBroadcastReceiverVistaGeneral()
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        //Obteniendo sharedPreferences y poniendo un listener a cualquier cambio en esta key
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        //bindeando el servicio al fragment y registrando el broadcast receiver
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        requireActivity().bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            locationServiceBroadcastReceiverVistaGeneral,
            IntentFilter(ACTION_LOCATION_BROADCAST)
        )

        //configurando UI según el rol del usuario
        configurandoUISegunRolDelUsuario()

        /*Administrador*/
        /* ClickListener's Administrador*/
        /*Fold and unfold cards*/
        _binding!!.imageViewVistaGeneralVolanterosFlechaBaja.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralVolanterosLinearLayoutReducido.visibility = View.GONE
            _binding!!.linearLayoutVistaGeneralVolanterosLinearLayoutAmpliado.visibility =
                View.VISIBLE
        }
        _binding!!.imageViewVistaGeneralVolanterosFlechaArriba.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralVolanterosLinearLayoutReducido.visibility =
                View.VISIBLE
            _binding!!.linearLayoutVistaGeneralVolanterosLinearLayoutAmpliado.visibility = View.GONE
        }
        _binding!!.imageViewVistaGeneralChoferesFlechaBaja.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralChoferesLinearLayoutReducido.visibility = View.GONE
            _binding!!.linearLayoutVistaGeneralChoferesLinearLayoutAmpliado.visibility =
                View.VISIBLE
        }
        _binding!!.imageViewVistaGeneralChoferesFlechaArriba.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralChoferesLinearLayoutReducido.visibility =
                View.VISIBLE
            _binding!!.linearLayoutVistaGeneralChoferesLinearLayoutAmpliado.visibility = View.GONE
        }
        _binding!!.imageViewVistaGeneralCallCenterFlechaBaja.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralCallCenterLinearLayoutReducido.visibility = View.GONE
            _binding!!.linearLayoutVistaGeneralCallCenterLinearLayoutAmpliado.visibility =
                View.VISIBLE
        }
        _binding!!.imageViewVistaGeneralCallCenterFlechaArriba.setOnClickListener {
            _binding!!.linearLayoutVistaGeneralCallCenterLinearLayoutReducido.visibility =
                View.VISIBLE
            _binding!!.linearLayoutVistaGeneralCallCenterLinearLayoutAmpliado.visibility = View.GONE
        }
        /* Volantero card options*/
        _binding!!.textViewVistaGeneralGestionDeVolanterosInforme.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    VistaGeneralFragmentDirections
                        .actionNavigationVistaGeneralToNavigationGestionDeVolanteros()
                )
        }
        _binding!!.textViewVistaGeneralRegistroDeVolanterosInforme.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    VistaGeneralFragmentDirections
                        .actionNavigationVistaGeneralToNavigationRegistroTrayectoVolanteros()
                )
        }
        _binding!!.textViewVistaGeneralGestionDeMaterial.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    VistaGeneralFragmentDirections
                        .actionNavigationVistaGeneralToNavigationGestionDeMaterial()
                )
        }
        /***********/



        /*Volantero*/
        /* ClickListener's Volanteros*/
        _binding!!.fabVistaGeneralVolanteroIniciarODetenerTrasmision.setOnClickListener {
            iniciarODetenerLocationService()
        }
        _binding!!.fabVistaGeneralSinMaterial.setOnClickListener {
            lanzarAlertaConConfirmacionYFuncionEnConsecuenciaEnMainThread(
                requireContext(),
                R.string.atencion,
                R.string.alerta_pidiendo_material
            ) {
                lifecycleScope.launch(Dispatchers.IO) {
                    _viewModel.avisarQueQuedeSinMaterial()
                }
            }
        }
        _binding!!.textViewVistaGeneralUiVolanteroVerDetalleBonos.setOnClickListener{
            val showModal = DetalleBonosDeResponsabilidadDialogFragment()
            showModal.show(requireActivity().supportFragmentManager, "DetalleBonosDeResponsabilidad")
        }

        /*Observers*/
        _viewModel.diasTrabajados.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralUiVolanteroDiasTrabajados.text = it
        }
        _viewModel.sueldoAcumulado.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralUiVolanteroSueldo.text =
                String.format(Locale("es", "CL"), "\$%,d", it.toInt())
        }

        _viewModel.distanciaTotalRecorrida.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralKilometros.text = it
        }

        _viewModel.tiempoTotalRecorridoVerde.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralVerde.text = it
        }
        _viewModel.tiempoTotalRecorridoAmarillo.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralAmarillo.text = it
        }
        _viewModel.tiempoTotalRecorridoRojo.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralRojo.text = it
        }
        _viewModel.tiempoTotalRecorridoAzul.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralAzul.text = it
        }
        _viewModel.tiempoTotalRecorridoRosado.observe(viewLifecycleOwner){
            _binding!!.textViewVistaGeneralRosado.text = it
        }
        /****************/


        /*ClickListener's CallCenter*/
        _binding!!.includeVistaGeneralUiCallCenter
            .buttonVistaGeneralUiCallCenterBuscarPorDireccion.setOnClickListener {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        convertirTextoAListaDeSugerenciasDeDireccionesAsync()
                    }
                }
            }
        _binding!!.includeVistaGeneralUiCallCenter
            .buttonVistaGeneralUiCallCenterConfirmarDireccion.setOnClickListener{
                callCenterValidarYCargarDatosAntesDeNavegar()
            }
        _binding!!.includeVistaGeneralUiCallCenter
            .buttonVistaGeneralUiCallCenterMedioDePago.setOnClickListener{
            abrirMenuMedioDePago(it)
        }
        /*Function that helps to keep the form data if the user go to the next windows and then
        comes back*/
        cargarFormularioSiYaTeniaDatos()
        /****************************/

        return _binding!!.root
    }
    override fun onDestroy() {
        if (::iniciandoSnapshotListener.isInitialized) {
            iniciandoSnapshotListener.remove()
        }
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(locationServiceBroadcastReceiverVistaGeneral)
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }
        locationService?.unsubscribeToLocationUpdatesVistaGeneralFragment()
        if(::sharedPreferences.isInitialized){
            if (!sharedPreferences.getBoolean(
                    SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                    false
                ) && _viewModel.usuarioEstaActivo
            ) {
                runBlocking {
                    withContext(Dispatchers.IO) {
                        try {
                            _viewModel.editarEstadoVolantero(false)
                        } catch (e: Exception) {
                            Log.i("sendo error", "sendo error")
                        }
                    }
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }
        super.onDestroy()
    }
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
        lifecycleScope.launch {
            when(_viewModel.obtenerRolDelUsuarioActual()) {
                "Volantero" -> {
                    map.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    setMapStyle(map)
                }
                "Call Center"->{
                    map.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
            }
        }
    }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences!!.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    /*General functions*/
    private fun startingPermissionCheck() {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (isPermissionGranted) {
            try {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    lastKnownLocation = task.result
                    Log.i("getDeviceLocation", task.result?.longitude.toString())
                    if (lastKnownLocation != null) {
                        //zoom to the user location after taking his permission
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lastKnownLocation!!.latitude, lastKnownLocation!!.longitude
                                ),
                                18f
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
                                .icon(getBitmap(R.drawable.ic_marker_volantero_green)?.let {
                                    BitmapDescriptorFactory.fromBitmap(
                                        it
                                    )
                                })
                        )
                    } else {
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    Constants.defaultLocation.latitude,
                                    Constants.defaultLocation.longitude
                                ),
                                Constants.cameraDefaultZoom.toFloat()
                            )
                        )
                        Toast.makeText(
                            requireActivity(),
                            "No se pudo obtener la ubicación actual",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: SecurityException) {
                _binding!!.fragmentContainerViewVistaGeneralMapa.isGone = true
                _binding!!.imageViewVistaGeneralMapaSinPermisos.isGone = false

            }
        }else{
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        Constants.defaultLocation.latitude,
                        Constants.defaultLocation.longitude
                    ),
                    Constants.cameraDefaultZoom.toFloat()
                )
            )
        }
    }
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(), R.raw.map_style
                )
            )
            if (!success) {
                Log.e("MapFragment", "Error al cargar el estilo del mapa")
            }
        } catch (e: Exception) {
            Log.e("MapFragment", "Error al cargar el estilo del mapa", e)
        }
    }
    private fun configurandoUISegunRolDelUsuario() {
        lifecycleScope.launch {
            when (_viewModel.obtenerRolDelUsuarioActual()) {
                "Volantero" -> {
                    rellenarDiasTrabajadosYSueldoDelVolantero(firebaseAuth.currentUser!!.uid)
                    (childFragmentManager.findFragmentById(R.id.fragmentContainerView_vistaGeneral_mapa) as? SupportMapFragment)
                        ?.getMapAsync(this@VistaGeneralFragment)
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                    _binding!!.constraintLayoutVistaGeneralUiVolanteros.visibility = View.VISIBLE
                    val isServiceEnabled = sharedPreferences.getBoolean(
                        SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                        false
                    )
                    updateButtonState(isServiceEnabled)
                    if (!isServiceEnabled) {
                        if (!_viewModel.editarEstadoVolantero(false)) {
                            Snackbar.make(
                                requireView(),
                                "Su cuenta presenta problemas de internet para acceder al registro de trayecto. Comunique esta situación a su superior inmediatamente.",
                                Snackbar.LENGTH_INDEFINITE
                            ).show()
                        }
                    }

                }

                "Administrador" -> {
                    _binding!!.nestedScrollViewVistaGeneralUiAdministradores.visibility = View.VISIBLE
                }

                "Call Center" ->{
                    (childFragmentManager.findFragmentById(R.id.fragmentContainerView_vistaGeneral_mapaCallCenter) as? SupportMapFragment)
                        ?.getMapAsync(this@VistaGeneralFragment)
                    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
                    _binding!!.includeVistaGeneralUiCallCenter.constraintLayoutVistaGeneralUiCallCenter.visibility = View.VISIBLE
                    // Initialize Places.
                    if (!Places.isInitialized()) {
                        Places.initialize(requireContext(), BuildConfig.PLACES_API_KEY)
                    }
                }

                "Error" -> Toast.makeText(
                    requireActivity(),
                    "Error: No se pudo obtener el rol del usuario. Cierre la app y vuelva a intentarlo. Si esto no funciona, revise su internet\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    /********************/

    /*VolanteroUi Functions*/
    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _viewModel.editarBotonVolantero(true)
        } else {
            _viewModel.editarBotonVolantero(false)
        }
    }
    private fun iniciarSnapshotListenerDelDocumentoDelUsuarioEnRegistroTrayectoVolanteros(){
        val docRef = cloudDB
            .collection("RegistroTrayectoVolanteros")
            .document(firebaseAuth.currentUser!!.uid)

        iniciandoSnapshotListener = docRef.addSnapshotListener{ snapshot, FirebaseFirestoreException ->
            if (FirebaseFirestoreException != null) {
                Snackbar.make(_binding!!.root,
                    "Error: $FirebaseFirestoreException",
                    Snackbar.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if(data!!["registroJornada"] == null){
                    return@addSnapshotListener
                }
                val registroJornada = data["registroJornada"] as ArrayList<Map<String, *>> ?: return@addSnapshotListener
                val fechaDeHoy = LocalDate.now().toString()
                registroJornada.forEach{
                    if(it["fecha"] == fechaDeHoy){
                        val registroLatLngs = it["registroLatLngs"] as Map<*,*>
                        listadoDeHorasDeRegistrodeNuevosGeopoints = registroLatLngs["horasConRegistro"] as ArrayList<String>
                        pintarPolyline(registroLatLngs["geopoints"] as ArrayList<GeoPoint>)
                    }
                }
            }else{
                Log.i("VistaGeneralFragment", "Recibi un update xq el documento se elimino o no existe")
            }
        }
    }
    private fun iniciarODetenerLocationService() {
        lifecycleScope.launch(Dispatchers.IO) {
            if(_viewModel.obtenerEnvioRegistroDeTrayecto().isNotEmpty()){
                if(sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)){
                    if (_viewModel.editarEstadoVolantero(false)) {
                        locationService?.let{
                            it.unsubscribeToLocationUpdatesVistaGeneralFragment()
                            lifecycleScope.launch(Dispatchers.Main){ map.clear() }
                            val snapshotListenerIniciado = ::iniciandoSnapshotListener.isInitialized
                            if (snapshotListenerIniciado) iniciandoSnapshotListener.remove()
                            Handler(Looper.getMainLooper()).post {
                                Snackbar.make(
                                    _binding!!.root,
                                    R.string.trasmision_detenida,
                                    Snackbar.LENGTH_INDEFINITE
                                ).setAction("Ok", {}).show()
                            }
                        }
                    }
                }else{
                    if (_viewModel.editarEstadoVolantero(true)) {
                        locationService?.let{
                            it.subscribeToLocationUpdatesVistaGeneralFragment()
                            _viewModel.obtenerUsuariosDesdeSqlite()
                            iniciarSnapshotListenerDelDocumentoDelUsuarioEnRegistroTrayectoVolanteros()
                            showToastInMainThreadWithStringResource(requireContext(),
                                R.string.trasmision_iniciada)
                        }
                    }
                }
            }else{
                showToastInMainThreadWithStringResource(requireContext(), R.string.trasmision_no_asist)
            }
        }
    }
    private fun pintarPolyline(geopoints: ArrayList<GeoPoint>){
        map.clear()
        latLngsDeInteres.clear()
        for(geopoint in geopoints){
            val latLng = LatLng(geopoint.latitude, geopoint.longitude)
            latLngsDeInteres.add(latLng)
        }
        polylineOptions = PolylineOptions().width(15f)

        var distanceTotalRecorrida = 0
        var distanceRecorrida = 0
        var topeParaDibujar = 0
        var tiempoEnRecorrerTramo = 0f
        val listAux = mutableListOf<LatLng?>()
        var tiempoEnRojo = 0f
        var tiempoEnAmarillo = 0f
        var tiempoEnVerde = 0f
        var tiempoEnAzul = 0f
        var tiempoEnRosado = 0f

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
            val tiempoLatLng1 = LocalTime.parse(listadoDeHorasDeRegistrodeNuevosGeopoints[i])
            val tiempoLatLng2 = LocalTime.parse(listadoDeHorasDeRegistrodeNuevosGeopoints[i+1])

            val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
            val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()

            distanceTotalRecorrida += distanceBetweenLatLngs
            distanceRecorrida += distanceBetweenLatLngs
            tiempoEnRecorrerTramo += timeBetweenLatLngs
            topeParaDibujar++
            listAux.add(latLng)
            if (topeParaDibujar == 23) {
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
                polylineOptions.addAll(listAux).color(color)
                map.addPolyline(polylineOptions)

                when(color){
                    Color.RED -> {
                        tiempoEnRojo += tiempoEnRecorrerTramo/1000
                        reDibujarMarker(listAux, R.drawable.ic_marker_volantero_red)
                    }
                    Color.YELLOW -> {
                        tiempoEnAmarillo += tiempoEnRecorrerTramo/1000
                        reDibujarMarker(listAux, R.drawable.ic_marker_volantero_yellow)
                    }
                    Color.GREEN -> {
                        tiempoEnVerde += tiempoEnRecorrerTramo/1000
                        reDibujarMarker(listAux, R.drawable.ic_marker_volantero_green)
                    }
                    Color.BLUE -> {
                        tiempoEnAzul += tiempoEnRecorrerTramo/1000
                        reDibujarMarker(listAux, R.drawable.ic_marker_volantero_blue)
                    }
                }

                polylineOptions = PolylineOptions().width(10f)
                distanceRecorrida = 0
                tiempoEnRecorrerTramo = 0f
                topeParaDibujar = 0
                listAux.clear()
            }
        }

        _viewModel.editarDistanciaTotalRecorrida(distanceTotalRecorrida)
        _viewModel.editarTiempoTotalRecorrido(tiempoEnRojo, "rojo")
        _viewModel.editarTiempoTotalRecorrido(tiempoEnAmarillo, "amarillo")
        _viewModel.editarTiempoTotalRecorrido(tiempoEnVerde, "verde")
        _viewModel.editarTiempoTotalRecorrido(tiempoEnAzul, "azul")
        _viewModel.editarTiempoTotalRecorrido(tiempoEnRosado, "rosado")
    }
    private fun reDibujarMarker(listAux: MutableList<LatLng?>, icono:Int) {
        if (::marker.isInitialized) {
            marker.remove()
        }
        marker = map.addMarker(
            MarkerOptions()
                .position(listAux.last()!!)
                .icon(getBitmap(icono)?.let { BitmapDescriptorFactory.fromBitmap(it) })
        )!!
    }
    private fun getBitmap(svgResource: Int): Bitmap? {
        val svg = AppCompatResources.getDrawable(requireActivity(), svgResource)?: return null
        val bitmap =
            Bitmap.createBitmap(svg.intrinsicWidth, svg.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.setBounds(0, 0, canvas.width, canvas.height)
        svg.draw(canvas)
        return bitmap
    }

    private fun rellenarDiasTrabajadosYSueldoDelVolantero(id: String){
        lifecycleScope.launch(Dispatchers.IO){
            _viewModel.obtenerDiasTrabajadosYSueldoDelVolantero(id)
        }
    }
    /************************/

    /*CallCenterUi Functions*/
    private fun convertirTextoAListaDeSugerenciasDeDireccionesAsync() {

        val editText = _binding!!.includeVistaGeneralUiCallCenter.autoCompleteTextViewVistaGeneralUiCallCenterBuscarPorDireccion
        val query = editText.text.toString()
        val inputMethodManager = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)

        if (query.isEmpty()) {
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(requireActivity(), R.string.ingrese_direccion_o_telefono_antes_de_buscar, Toast.LENGTH_SHORT).show()
            }
        }

        val placesClient = Places.createClient(requireActivity())
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setTypeFilter(TypeFilter.ADDRESS)
            .setSessionToken(token)
            .setQuery(query)
            .setCountries(listOf("CL"))
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                val listOfDirecctions = mutableListOf<String>()
                val predictionList = response.autocompletePredictions
                predictionList.forEach{ listOfDirecctions.add(it.getFullText(null).toString()) }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOfDirecctions)

                _binding!!.includeVistaGeneralUiCallCenter.apply {
                    val aux = this
                    aux.autoCompleteTextViewVistaGeneralUiCallCenterBuscarPorDireccion.apply{
                        setAdapter(adapter)
                        showDropDown()
                        setOnItemClickListener { parent, view, position, id ->
                            val selectedAddress = parent.getItemAtPosition(position).toString()
                            val latLng = convertirDireccionALatLng(selectedAddress)
                            latLng?.let {
                                _viewModel.geoPointPedidoDelCliente = GeoPoint(latLng.latitude, latLng.longitude)
                                map.clear()
                                map.addMarker(MarkerOptions().position(it))
                                map.moveCamera(CameraUpdateFactory.newLatLng(it))
                                map.animateCamera(CameraUpdateFactory.zoomTo(18f))
                                aux.apply{
                                    this.autoCompleteTextViewVistaGeneralUiCallCenterDepto.isEnabled = true
                                    this.autoCompleteTextViewVistaGeneralUiCallCenterBlock.isEnabled = true
                                    this.autoCompleteTextViewVistaGeneralUiCallCenterTelefono.isEnabled = true
                                    this.buttonVistaGeneralUiCallCenterMedioDePago.isEnabled = true
                                    this.textInputEditTextVistaGeneralUiCallCenterComentarios.isEnabled = true
                                }
                                _binding!!.includeVistaGeneralUiCallCenter.buttonVistaGeneralUiCallCenterConfirmarDireccion.apply{
                                    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this.context, R.color.orange))
                                    isEnabled = true
                                    setTextColor(Color.WHITE)
                                }


                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(requireActivity(), exception.message, Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun convertirDireccionALatLng(direccion: String): LatLng? {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocationName(direccion, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val latitude = addressList[0].latitude
                val longitude = addressList[0].longitude
                return LatLng(latitude, longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
    private fun abrirMenuMedioDePago(it: View?) {
        val popupMenu = PopupMenu(requireContext(), it)
        popupMenu.menuInflater.inflate(R.menu.menu_medio_de_pago, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.efectivo -> {
                    _binding!!.includeVistaGeneralUiCallCenter.buttonVistaGeneralUiCallCenterMedioDePago.text =
                        menuItem.title
                    true
                }
                R.id.debito -> {
                    _binding!!.includeVistaGeneralUiCallCenter.buttonVistaGeneralUiCallCenterMedioDePago.text =
                        menuItem.title
                    true
                }
                R.id.credito -> {
                    _binding!!.includeVistaGeneralUiCallCenter.buttonVistaGeneralUiCallCenterMedioDePago.text =
                        menuItem.title
                    true
                }
                R.id.vale -> {
                    _binding!!.includeVistaGeneralUiCallCenter.buttonVistaGeneralUiCallCenterMedioDePago.text =
                        menuItem.title
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun callCenterValidarYCargarDatosAntesDeNavegar() {
        val nombreCompleto = _binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterNombreCompleto.text.toString()
        val direccion = _binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterBuscarPorDireccion.text.toString()
        val departamento = _binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterDepto.text.toString()
        val block = _binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterBlock.text.toString()
        val telefono = _binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterTelefono.text.toString()
        val medioDePago = _binding!!.includeVistaGeneralUiCallCenter
            .buttonVistaGeneralUiCallCenterMedioDePago.text.toString()
        val comentarios = _binding!!.includeVistaGeneralUiCallCenter
            .textInputEditTextVistaGeneralUiCallCenterComentarios.text.toString()
        val cantidadDePalabras = nombreCompleto.split(" ")

        val inputMethodManager = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(_binding!!.includeVistaGeneralUiCallCenter
            .autoCompleteTextViewVistaGeneralUiCallCenterTelefono.windowToken, 0)

        if (nombreCompleto.isEmpty() || direccion.isEmpty() || telefono.isEmpty() || medioDePago == "Medio de pago") {
            Snackbar.make(
                _binding!!.root,
                R.string.snackbar_pedido_gas_faltan_datos,
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (cantidadDePalabras.size < 4) {
            Snackbar.make(
                _binding!!.root,
                R.string.tu_nombre_debe_contener_a_lo_menos_2_nombres_y_2_apellidos,
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        _viewModel.nombreDelCliente = nombreCompleto
        _viewModel.direccionDelCliente = direccion
        _viewModel.deptoDelCliente = departamento
        _viewModel.blockDelCliente = block
        _viewModel.telefonoDelCliente = telefono
        _viewModel.medioDePagoDelCliente = medioDePago
        _viewModel.comentarios = comentarios
        findNavController().navigate(VistaGeneralFragmentDirections.actionNavigationVistaGeneralToNavigationCantidadDeBalones())
    }
    private fun cargarFormularioSiYaTeniaDatos(){
        val nombreCompleto = _viewModel.nombreDelCliente
        val direccion = _viewModel.direccionDelCliente
        val departamento = _viewModel.deptoDelCliente
        val block = _viewModel.blockDelCliente
        val telefono = _viewModel.telefonoDelCliente
        val medioDePago = _viewModel.medioDePagoDelCliente
        val comentarios = _viewModel.comentarios

        if(nombreCompleto != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .autoCompleteTextViewVistaGeneralUiCallCenterNombreCompleto.setText(nombreCompleto)
        }
        if(direccion != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .autoCompleteTextViewVistaGeneralUiCallCenterBuscarPorDireccion.setText(direccion)
        }
        if(departamento != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .autoCompleteTextViewVistaGeneralUiCallCenterDepto.setText(departamento)
        }
        if(block != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .autoCompleteTextViewVistaGeneralUiCallCenterBlock.setText(block)
        }
        if(telefono != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .autoCompleteTextViewVistaGeneralUiCallCenterTelefono.setText(telefono)
        }
        if(medioDePago != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .buttonVistaGeneralUiCallCenterMedioDePago.text = medioDePago
        }
        if(comentarios != ""){
            _binding!!.includeVistaGeneralUiCallCenter
                .textInputEditTextVistaGeneralUiCallCenterComentarios.setText(comentarios)
        }

    }
    /************************/

}
