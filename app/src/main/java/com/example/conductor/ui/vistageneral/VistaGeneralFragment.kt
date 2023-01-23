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
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )
            if (location != null) {
                try{
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                             val aux = cloudDB.collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid )
                                .get().await()
                            val data = aux.data
                            val fechaDeHoy = LocalDate.now().toString()
                            if (data != null) {
                                val registroJornada =
                                    data["registroJornada"] as ArrayList<Map<*, *>>
                                Log.i("asd", "registroJornada: $registroJornada")
                                for (mapa in registroJornada) {
                                    if (mapa["fecha"] == fechaDeHoy) {
                                        val geoPoints =
                                            mapa["registroLatLngs"] as ArrayList<GeoPoint>
                                        geoPoints.add(
                                            GeoPoint(
                                                location.latitude,
                                                location.longitude
                                            )
                                        )
                                        cloudDB.collection("RegistroTrayectoVolanteros")
                                            .document(firebaseAuth.currentUser!!.uid)
                                            .update("registroJornada", registroJornada)
                                        Log.i("asd", "se intentara guardar una latlng")
                                        return@withContext
                                    }
                                }
                                registroJornada.add(
                                    mapOf(
                                        "fecha" to fechaDeHoy,
                                        "registroLatLngs" to arrayListOf(
                                            GeoPoint(
                                                location.latitude,
                                                location.longitude
                                            )
                                        )
                                    )
                                )
                                cloudDB.collection("RegistroTrayectoVolanteros")
                                    .document(firebaseAuth.currentUser!!.uid)
                                    .update("registroJornada", registroJornada)
                                Log.i("asd", "se añadio una nueva fecha al registroLatLngs del usuario.")
                            }else{
                                try{
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
                                                )
                                            )
                                        )
                                    Log.i("asd", "se creo un nuevo registroLatLngs para el usuario.")
                                }catch(e:Exception){
                                    Log.i("asd", "error al crear un nuevo registroLatLngs para el usuario.")
                                }
                            }
                        }
                    }
                }catch(e: Exception){
                    Log.i("asd","Error al guardar la ubicación: ${e.message}")
                    Toast.makeText(requireActivity(), "No se pudo guardar la ubicación: $e", Toast.LENGTH_SHORT).show()
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
            } else {
                foregroundOnlyLocationService?.subscribeToLocationUpdates()
                //_viewModel.modificarEstadoVolantero(true)
            }
        }

        return _binding!!.root
    }



    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences!!.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

/*    override fun onPause() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(
            foregroundOnlyBroadcastReceiver
        )
        super.onPause()
    }

    override fun onStop() {
        if (foregroundOnlyLocationServiceBound) {
            requireActivity().unbindService(foregroundOnlyServiceConnection)
            foregroundOnlyLocationServiceBound = false
        }
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }*/

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
        super.onDestroy()

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
