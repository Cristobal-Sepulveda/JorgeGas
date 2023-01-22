package com.example.conductor.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.example.conductor.utils.Constants.cameraDefaultZoom
import com.example.conductor.utils.Constants.defaultLocation
import com.example.conductor.utils.polygonsColor
import com.example.conductor.utils.polygonsList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.time.LocalDate


class MapFragment : BaseFragment(), OnMapReadyCallback{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private lateinit var map: GoogleMap
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var iniciandoSnapshotListener: ListenerRegistration

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        _binding!!.buttonMapaReiniciarActividad.setOnClickListener {
            val intent = Intent(requireActivity(), requireActivity().javaClass)
            startActivity(intent)
            requireActivity().finish()
        }

        return _binding!!.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
        markingPolygons()
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                iniciarSnapshotListenerDelRegistroTrayectoVolanteros()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detenerSnapshotListenerDelRegistroTrayectoVolanteros()
    }

    private fun markingPolygons(){
        for((index,polygon) in polygonsList.withIndex()){
            val polygonOptions = PolygonOptions()
            polygonOptions.addAll(polygon)
            polygonOptions.fillColor(polygonsColor[index])
            polygonOptions.strokeColor(Color.argb(50,255,255,255))
            map.addPolygon(polygonOptions)
        }
    }

    private fun iniciarSnapshotListenerDelRegistroTrayectoVolanteros() {
        val docRef = cloudDB.collection("RegistroTrayectoVolanteros")
        iniciandoSnapshotListener = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(
                    "AppRepository",
                    "obtenerSnapshotDelRegistroTrayectoVolanteros: Listen failed.",
                    e
                )
                return@addSnapshotListener
            }
            if (snapshot != null && !snapshot.isEmpty) {
                for (documentChange in snapshot.documentChanges) {
                    when (documentChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val a = documentChange.document.data["registroJornada"] as List<Map<String, List<GeoPoint>>>
                            for (element in a) {
                                if(element["fecha"].toString() == LocalDate.now().toString()){
                                    val e = element["registroLatLngs"]?.get(0) as GeoPoint
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(e.latitude, e.longitude))
                                            .title("Volantero")
                                    )
                                    Log.i("Firestore","${e.latitude} ${e.longitude}")
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            val a = documentChange.document.data["registroJornada"] as List<Map<String, List<GeoPoint>>>
                            for (element in a) {
                                if(element["fecha"].toString() == LocalDate.now().toString()){
                                    val e = element["registroLatLngs"]?.get(0) as GeoPoint
                                    map.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(e.latitude, e.longitude))
                                            .title("Volantero")
                                    )
                                    Log.i("Firestore","Un usuario ha añadido un nuevo GeoPoint: ${e.latitude} ${e.longitude}")
                                }
                            }
                            Log.i(
                                "Firestore",
                                "Un Usuario a registrado un nuevo GeoPoint: ${documentChange.document.data}"
                            )
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.i(
                                "Firestore",
                                "Uno de los usuarios del registro ha sido eliminado: ${documentChange.document.data}"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun detenerSnapshotListenerDelRegistroTrayectoVolanteros(){
        iniciandoSnapshotListener.remove()
    }

    private fun startingPermissionCheck(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(isPermissionGranted){
            try{
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(defaultLocation.latitude, defaultLocation.longitude)
                    , cameraDefaultZoom.toFloat())
                )
                map.uiSettings.isMyLocationButtonEnabled = true
            }catch(e: SecurityException){
                _binding!!.map.isGone = true
                _binding!!.imageviewMapaSinPermisos.isGone = false
                _binding!!.buttonMapaReiniciarActividad.isGone = false

            }
        }else {
            _binding!!.map.isGone = true
            _binding!!.imageviewMapaSinPermisos.isGone = false
            _binding!!.buttonMapaReiniciarActividad.isGone = false
        }
    }
}


