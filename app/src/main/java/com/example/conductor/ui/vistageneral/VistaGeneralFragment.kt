package com.example.conductor.ui.vistageneral

import android.content.*
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasFragmentDirections
import com.example.conductor.utils.Constants.ACTION_LOCATION_BROADCAST
import com.example.conductor.utils.Constants.EXTRA_LOCATION
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.LocationService
import com.example.conductor.utils.NavigationCommand
import com.example.conductor.utils.SharedPreferenceUtil
import com.example.conductor.utils.notificationGenerator
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

class VistaGeneralFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

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
                                    dataDocumento["registroJornada"] as ArrayList<Map<String,*>>
                                for (registroDeUnDia in registroJornada) {
                                    if (registroDeUnDia["fecha"] == fechaDeHoy) {
                                        val registroLatLngs = registroDeUnDia["registroLatLngs"] as Map<*,*>
                                        val horasRegistradas = registroLatLngs["horasConRegistro"] as ArrayList<String>
                                        horasRegistradas.add(LocalTime.now().toString())
                                        val geoPointRegistrados = registroLatLngs["geopoints"] as ArrayList<GeoPoint>
                                        geoPointRegistrados.add(GeoPoint(location.latitude, location.longitude))
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
                                            "horasConRegistro" to arrayListOf(LocalTime.now().toString()),
                                            "geopoints" to arrayListOf(GeoPoint(location.latitude,
                                                location.longitude))
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
                                                            "horasConRegistro" to arrayListOf(LocalTime.now().toString()),
                                                            "geopoints" to arrayListOf(GeoPoint(location.latitude,
                                                                location.longitude))
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        //binding...
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)
        //Obteniendo sharedPreferences y poniendo un listener a cualquier cambio en esta key
        sharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_file_key),
            Context.MODE_PRIVATE)
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        //bindeando el servicio al fragment y registrando el broadcast receiver
        val serviceIntent = Intent(requireActivity(), LocationService::class.java)
        requireActivity().bindService(serviceIntent, locationServiceConnection, Context.BIND_AUTO_CREATE)
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(locationServiceBroadcastReceiver, IntentFilter(ACTION_LOCATION_BROADCAST))
        //configurando UI según el rol del usuario
        configurandoUISegunRolDelUsuario()

        _binding!!.buttonVistaGeneralRegistroJornadaVolantero.setOnClickListener {
            iniciarODetenerLocationService()
        }

        _binding!!.imageViewVistaGeneralBotonVolantero.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(VistaGeneralFragmentDirections
                    .actionNavigationVistaGeneralToNavigationGestionDeVolanteros())
        }

        return _binding!!.root
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(locationServiceBroadcastReceiver)
        if (locationServiceBound) {
            requireActivity().unbindService(locationServiceConnection)
            locationServiceBound = false
        }
        locationService?.unsubscribeToLocationUpdates()
        if(!sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false) && _viewModel.usuarioEstaActivo) {
            runBlocking {
                withContext(Dispatchers.IO) {
                    try{
                        _viewModel.editarEstadoVolantero(false)
                    }catch(e:Exception){
                        Log.i("sendo error", "sendo error")
                    }
                }
            }
        }
        SharedPreferenceUtil.saveLocationTrackingPref(requireActivity(), false)
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

    private fun configurandoUISegunRolDelUsuario() {
        lifecycleScope.launch {
            when(_viewModel.obtenerRolDelUsuarioActual()){
                "Volantero" -> {
                    _binding!!.buttonVistaGeneralRegistroJornadaVolantero.visibility = View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonVolantero.visibility= View.GONE
                    _binding!!.imageViewVistaGeneralBotonChoferes.visibility= View.GONE
                    _binding!!.imageViewVistaGeneralBotonCallCenter.visibility= View.GONE
                    val isServiceEnabled = sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                    updateButtonState(isServiceEnabled)
                    if(!isServiceEnabled){
                        if(!_viewModel.editarEstadoVolantero(false)){
                            Snackbar.make(requireView(), "Su cuenta presenta problemas de internet para acceder al registro de trayecto. Comunique esta situación a su superior inmediatamente.", Snackbar.LENGTH_INDEFINITE).show()
                        }
                    }
                }
                "Administrador" -> {
                    _binding!!.buttonVistaGeneralRegistroJornadaVolantero.visibility = View.GONE
                    _binding!!.imageViewVistaGeneralBotonVolantero.visibility= View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonChoferes.visibility= View.VISIBLE
                    _binding!!.imageViewVistaGeneralBotonCallCenter.visibility= View.VISIBLE
                }
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

    private fun iniciarODetenerLocationService() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val enabled =
                    sharedPreferences.getBoolean(SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
                if (enabled) {
                    if (_viewModel.editarEstadoVolantero(false)) {
                        locationService?.unsubscribeToLocationUpdates()
                        notificationGenerator(requireActivity(),"El servicio de localización ha sido detenido.")
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "El servicio no será desactivado debido a que no se ha podido configurar al usuario como inactivo en la nube. Intentelo Nuevamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    if (_viewModel.editarEstadoVolantero(true)) {
                        _viewModel.obtenerUsuariosDesdeSqlite()
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



}
