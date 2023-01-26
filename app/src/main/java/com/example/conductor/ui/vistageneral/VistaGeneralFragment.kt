package com.example.conductor.ui.vistageneral

import android.content.*
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import com.example.conductor.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.EXTRA_LOCATION
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.LocationService
import com.example.conductor.utils.SharedPreferenceUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate

class VistaGeneralFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentVistaGeneralBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private var locationServiceBound = false
    // Listens for location broadcasts from LocationService.
    private inner class LocationServiceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(EXTRA_LOCATION)
            if (location != null) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        try {
                            val registroTrayectoVolanterosUsuario = cloudDB
                                .collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
                                .get().await()
                            val dataDocumento = registroTrayectoVolanterosUsuario.data
                            val fechaDeHoy = LocalDate.now().toString()
                            if (dataDocumento != null) {
                                val registroJornada =
                                    dataDocumento["registroJornada"] as ArrayList<Map<*, *>>
                                for (registroDeUnDia in registroJornada) {
                                    if (registroDeUnDia["fecha"] == fechaDeHoy) {
                                        val geoPoints = registroDeUnDia["registroLatLngs"] as ArrayList<GeoPoint>
                                        geoPoints.add(
                                            GeoPoint(
                                                location.latitude,
                                                location.longitude
                                            )
                                        )
                                        val nuevoGeoPoint = mapOf(
                                            "registroJornada" to registroJornada,
                                            "estaActivo" to true
                                        )
                                        cloudDB.collection("RegistroTrayectoVolanteros")
                                            .document(firebaseAuth.currentUser!!.uid)
                                            .update(nuevoGeoPoint)
                                        return@withContext
                                    }
                                }
                                registroJornada.add(
                                    mapOf(
                                        "fecha" to fechaDeHoy,
                                        "registroLatLngs" to arrayListOf(
                                            GeoPoint(location.latitude, location.longitude)
                                        )
                                    )
                                )
                                val nuevoRegistro = mapOf(
                                    "registroJornada" to registroJornada,
                                    "estaActivo" to true
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
                                                    "registroLatLngs" to arrayListOf(
                                                        GeoPoint(
                                                            location.latitude,
                                                            location.longitude
                                                        )
                                                    )
                                                )
                                            ),
                                            "estaActivo" to true
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
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key),
            Context.MODE_PRIVATE)
        enCasoDeErrorActualizar()
        preguntarSiUsuarioEsVolantero()
        _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setOnClickListener {
            iniciarODetenerLocationService()
        }

        return _binding!!.root
    }

    private fun enCasoDeErrorActualizar() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val enabled = sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                if(!enabled){
                    if(!_viewModel.modificarEstadoVolantero(false)){
                        Snackbar.make(requireView(), "Su cuenta presenta problemas en el registro de trayecto. Comunique esta situación a su superior inmediatamente.", Snackbar.LENGTH_INDEFINITE).show()
                    }

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        updateButtonState(sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false))
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        requireActivity().bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            locationServiceBroadcastReceiver,
            IntentFilter(
                ACTION_LOCATION_BROADCAST)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(locationServiceBroadcastReceiver)
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }
        SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        iniciarODetenerLocationService()
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences!!.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    private fun iniciarODetenerLocationService() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val enabled =
                    sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                if (enabled) {
                    if (_viewModel.modificarEstadoVolantero(false)) {
                        locationService?.unsubscribeToLocationUpdates()
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "El servicio no será desactivado debido a que no se ha podido configurar al usuario como inactivo en la nube. Intentelo Nuevamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (_viewModel.modificarEstadoVolantero(true)) {
                        locationService?.subscribeToLocationUpdates()
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "El servicio no será activado debido a que no se ha podido configurar al usuario como activo en la nube. Intentelo Nuevamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun preguntarSiUsuarioEsVolantero() {
        lifecycleScope.launch {
            when(_viewModel.obtenerRolDelUsuarioActual()){
                "Volantero" -> _binding!!.buttonVistaGeneralRegistroJornadaVolantero.visibility = View.VISIBLE
                "Error" -> Toast.makeText(requireActivity(), "Error: No se pudo obtener el rol del usuario. Cierre la app y vuelva a intentarlo. Si esto no funciona, revise su internet\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _binding!!.buttonVistaGeneralRegistroJornadaVolantero.text = getString(R.string.detener)
            _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setBackgroundColor(Color.argb(100, 255, 0, 0))
        } else {
            _binding!!.buttonVistaGeneralRegistroJornadaVolantero.text = getString(R.string.iniciar)
            _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setBackgroundColor(Color.argb(100, 0, 255, 0))
        }
    }

}
