package com.example.conductor.ui.vistageneral

import android.content.*
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.*
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import com.example.conductor.utils.Constants
import com.example.conductor.utils.ForegroundOnlyLocationService
import com.example.conductor.utils.SharedPreferenceUtil
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private inner class ForegroundOnlyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location = intent.getParcelableExtra<Location>(
                ForegroundOnlyLocationService.EXTRA_LOCATION
            )
            if (location != null) {
                try{
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            cloudDB.collection("RegistroTrayectoVolanteros")
                                .document(Constants.firebaseAuth.currentUser!!.uid).get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val data = documentSnapshot.data
                                    val fechaDeHoy = LocalDate.now().toString()
                                    val diaDeHoy = fechaDeHoy.subSequence(8, 10)
                                    if(data!=null){
                                        val latLngs = data["historicoLatLngs"] as Map<*,*>
                                        val arrayAEditar = latLngs[diaDeHoy] as ArrayList<GeoPoint>
                                        arrayAEditar.add(GeoPoint(location.latitude, location.longitude))
                                        cloudDB.collection("RegistroTrayectoVolanteros")
                                            .document(Constants.firebaseAuth.currentUser!!.uid)
                                            .update("historicoLatLngs.$diaDeHoy", arrayAEditar)
                                        /*if(data["ultimoDiaEnOperacion"] != fechaDeHoy) {
                                            cloudDB.collection("RegistroTrayectoVolanteros")
                                                .document(Constants.firebaseAuth.currentUser!!.uid)
                                                .update("historicoLatLngs.${diaDeHoy.toInt()}", emptyList<GeoPoint>())
                                            cloudDB.collection("RegistroTrayectoVolanteros")
                                                .document(Constants.firebaseAuth.currentUser!!.uid)
                                                .update("ultimoDiaEnOperacion", fechaDeHoy.toString())
                                        }*/
                                    }
                                }
                        }
                    }
                }catch(e: Exception){
                    Log.i("NuevaUtilidadFragment", "registrar la localización en la nube fallo.")
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
            visibilidadDelButtonVistaGeneralRegistroJornadaVolantero()
        }

        _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setOnClickListener {
            val enabled = sharedPreferences.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED,
                false)
            if (enabled) {
                foregroundOnlyLocationService?.unsubscribeToLocationUpdates()
            } else {
                foregroundOnlyLocationService?.subscribeToLocationUpdates()
                    ?: Log.d("VistaGeneralFragment", "Service Not Bound")
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

    private suspend fun visibilidadDelButtonVistaGeneralRegistroJornadaVolantero(){
        if(_viewModel.obtenerRolDelUsuarioActual() == "Volantero") {
            _binding!!.buttonVistaGeneralRegistroJornadaVolantero.visibility = View.VISIBLE
        }
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
