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
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.ForegroundOnlyLocationService
import com.example.conductor.utils.SharedPreferenceUtil
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
    private var foregroundOnlyLocationServiceBound = false
    // Provides location updates for while-in-use feature.
    private var foregroundOnlyLocationService: ForegroundOnlyLocationService? = null
    // Listens for location broadcasts from ForegroundOnlyLocationService.
    private lateinit var foregroundOnlyBroadcastReceiver: ForegroundOnlyBroadcastReceiver
    private lateinit var sharedPreferences: SharedPreferences
    // Monitors connection to the while-in-use service.
    private val foregroundOnlyServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as ForegroundOnlyLocationService.LocalBinder
            foregroundOnlyLocationService = binder.service
            foregroundOnlyLocationServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            foregroundOnlyLocationService = null
            foregroundOnlyLocationServiceBound = false
        }
    }

    /* Esta clase recibe el aviso de que se ha obtenido una nueva LatLng */
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(ForegroundOnlyLocationService.EXTRA_LOCATION)
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        foregroundOnlyBroadcastReceiver = ForegroundOnlyBroadcastReceiver()

        lifecycleScope.launch{
            if(_viewModel.obtenerRolDelUsuarioActual() == "Volantero") {
                _binding!!.buttonVistaGeneralRegistroJornadaVolantero.visibility = View.VISIBLE
            }
        }


        _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
                lifecycleScope.launch{
                    withContext(Dispatchers.IO){
                        _viewModel.modificarEstadoVolantero(false)
                    }
                }
            } else {
                foregroundOnlyLocationService?.subscribeToLocationUpdates()
                lifecycleScope.launch{
                    withContext(Dispatchers.IO){
                        _viewModel.modificarEstadoVolantero(true)
                    }
                }
            }
        }

        return _binding!!.root
    }

    override fun onStart() {
        super.onStart()
        updateButtonState(
            sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
        )
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val serviceIntent = Intent(requireActivity(), ForegroundOnlyLocationService::class.java)
        requireActivity().bindService(serviceIntent, foregroundOnlyServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(
            foregroundOnlyBroadcastReceiver,
            IntentFilter(
                ForegroundOnlyLocationService.ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
        )
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                _viewModel.modificarEstadoVolantero(false)
            }
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
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
