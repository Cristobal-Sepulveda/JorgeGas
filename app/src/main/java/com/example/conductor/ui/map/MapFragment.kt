package com.example.conductor.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    private val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var iniciandoSnapshotListener: ListenerRegistration
    private var volanterosActivosAMarcarEnElMapa: HashMap<String,Marker> = HashMap()
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        _binding!!.fabVistaOriginal.setOnClickListener{
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(defaultLocation.latitude, defaultLocation.longitude)
                , cameraDefaultZoom.toFloat())
            map.animateCamera(cameraUpdate, 300, null)
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
                                    val marker = map.addMarker(MarkerOptions()
                                        .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_marker_volantero)))
                                        .title(documentChange.document.data["nombreCompleto"].toString())
                                    )
                                    volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
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
                                    val marker = map.addMarker(MarkerOptions()
                                        .position(LatLng(nuevoVolanteroGeopoint.latitude,nuevoVolanteroGeopoint.longitude))
                                        .icon(BitmapDescriptorFactory.fromBitmap(getBitmap(R.drawable.ic_marker_volantero)))
                                        .title(documentChange.document.data["nombreCompleto"].toString())
                                    )
                                    volanterosActivosAMarcarEnElMapa.putIfAbsent(documentChange.document.id, marker!!)
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

    private fun getBitmap(svgResource: Int): Bitmap {
        val svg = AppCompatResources.getDrawable(requireActivity(), svgResource)
        val bitmap = Bitmap.createBitmap(svg!!.intrinsicWidth, svg.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        svg.setBounds(0, 0, canvas.width, canvas.height)
        svg.draw(canvas)
        return bitmap
    }
}


