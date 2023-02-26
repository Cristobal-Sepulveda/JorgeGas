package com.example.conductor.ui.vistageneral

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.LocalTime

class VistaGeneralFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener,
    OnMapReadyCallback {

    private var _binding: FragmentVistaGeneralBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private var locationServiceBound = false

    // Listens for location broadcasts from LocationService.
    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /*aqui obtengo la localizacion*/
            val location = intent.getParcelableExtra<Location>(EXTRA_LOCATION)
            /*si la ubicacion no es nula*/
            if (location != null) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            /*obtengo el registro del usuario en google cloud, con el objetivo
                            * de registrar la nueva localizacion*/
                            val registroTrayectoVolanterosUsuario = cloudDB
                                .collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
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
                                        )

                                        cloudDB.collection("RegistroTrayectoVolanteros")
                                            .document(firebaseAuth.currentUser!!.uid)
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
                                    "nombreCompleto" to _viewModel.usuarioDesdeSqlite
                                )
                                cloudDB.collection("RegistroTrayectoVolanteros")
                                    .document(firebaseAuth.currentUser!!.uid)
                                    .update(nuevoRegistro)
                            } else {
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
                                            "nombreCompleto" to _viewModel.usuarioDesdeSqlite
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
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //binding...
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_vistaGeneral_mapa) as? SupportMapFragment)
            ?.getMapAsync(this)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
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
                IntentFilter(ACTION_LOCATION_BROADCAST)
            )
        //configurando UI según el rol del usuario
        configurandoUISegunRolDelUsuario()

        _binding!!.fabVistaGeneralRegistroJornadaVolantero.setOnClickListener {
            iniciarODetenerLocationService()
        }

        _binding!!.fabVistaGeneralSinMaterial.setOnClickListener{

        }

        _binding!!.imageViewVistaGeneralBotonVolantero.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    VistaGeneralFragmentDirections
                        .actionNavigationVistaGeneralToNavigationGestionDeVolanteros()
                )
        }

        return _binding!!.root
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity())
            .unregisterReceiver(locationServiceBroadcastReceiver)
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }
        locationService?.unsubscribeToLocationUpdates()
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
        super.onDestroy()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
        setMapStyle(map)
        map.mapType = GoogleMap.MAP_TYPE_SATELLITE
        /*lifecycleScope.launch{
            withContext(Dispatchers.IO){
                iniciarSnapshotListenerDelRegistroTrayectoVolanteros()
            }
        }*/
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
                                .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_marker_volantero)))
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
        } else {
            _binding!!.fragmentContainerViewVistaGeneralMapa.isGone = true
            _binding!!.imageViewVistaGeneralMapaSinPermisos.isGone = false
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

    private fun getBitmap(svgResource: Int): Bitmap {
        val svg = AppCompatResources.getDrawable(requireActivity(), svgResource)
        val bitmap =
            Bitmap.createBitmap(svg!!.intrinsicWidth, svg.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.setBounds(0, 0, canvas.width, canvas.height)
        svg.draw(canvas)
        return bitmap
    }

    private fun configurandoUISegunRolDelUsuario() {
        lifecycleScope.launch {
            when (_viewModel.obtenerRolDelUsuarioActual()) {
                "Volantero" -> {
                    _binding!!.fabVistaGeneralRegistroJornadaVolantero.visibility = View.VISIBLE
                    _binding!!.fabVistaGeneralSinMaterial.visibility = View.VISIBLE
                    _binding!!.fragmentContainerViewVistaGeneralMapa.visibility = View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonVolantero.visibility = View.GONE
                    _binding!!.imageViewVistaGeneralBotonChoferes.visibility = View.GONE
                    _binding!!.imageViewVistaGeneralBotonCallCenter.visibility = View.GONE
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
                    _binding!!.fabVistaGeneralRegistroJornadaVolantero.visibility = View.GONE
                    _binding!!.imageViewVistaGeneralBotonVolantero.visibility = View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonChoferes.visibility = View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonCallCenter.visibility = View.VISIBLE
                }
                "Error" -> Toast.makeText(
                    requireActivity(),
                    "Error: No se pudo obtener el rol del usuario. Cierre la app y vuelva a intentarlo. Si esto no funciona, revise su internet\"",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _viewModel.editarBotonVolantero(true)
            _binding!!.fabVistaGeneralRegistroJornadaVolantero.setBackgroundColor(
                Color.argb(
                    100,
                    255,
                    0,
                    0
                )
            )
        } else {
            _viewModel.editarBotonVolantero(false)
            _binding!!.fabVistaGeneralRegistroJornadaVolantero.setBackgroundColor(
                Color.argb(
                    100,
                    0,
                    255,
                    0
                )
            )
        }
    }

    private fun iniciarODetenerLocationService() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val enabled =
                    sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                if (enabled) {
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
                        _viewModel.obtenerUsuariosDesdeSqlite()
                        locationService?.subscribeToLocationUpdates()
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
}
