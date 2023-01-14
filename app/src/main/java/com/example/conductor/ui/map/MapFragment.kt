package com.example.conductor.ui.map

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.example.conductor.utils.polygonsColor
import com.example.conductor.utils.polygonsList
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject


class MapFragment : BaseFragment(), OnMapReadyCallback{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // The entry point to the Fused Location Provider.
    private var locationPermissionGranted = false
    private val defaultLocation = LatLng(-33.6256, -70.5841)
    private val cameraDefaultZoom = 13
    private var lastKnownLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted ->
        when{
            isGranted -> {
                locationPermissionGranted = true
                getDeviceLocation()
            }
            else -> {
                Snackbar.make(
                    _binding!!.root,
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_LONG
                ).setAction(R.string.settings) {
                    startActivityForResult(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package",
                            "com.example.conductor",
                            null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },1001)
                }.show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // Specify the current fragment as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates
        _binding!!.lifecycleOwner = this

        //Adding  the map setup implementation
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        _binding!!.buttonReiniciarMapa.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentManager?.beginTransaction()?.detach(this)?.commitNow();
                fragmentManager?.beginTransaction()?.attach(this)?.commitNow();
            }else{
                fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit();
            }
        }

        return _binding!!.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        //setMapStyle(map)
        markingPolygons()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding!!.buttonReiniciarMapa.isGone = true
    }

    private fun markingPolygons(){
        for((index,polygon) in polygonsList.withIndex()){
            val polygonOptions = PolygonOptions()
            polygonOptions.addAll(polygon)
            polygonOptions.fillColor(polygonsColor[index])
            polygonOptions.strokeColor(Color.argb(100,255,255,255))
            map.addPolygon(polygonOptions)
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("SelectLocationFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("SelectLocationFragment", "Can't find style. Error: ", e)
        }
    }

    private  fun sendAlert(){
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.perm_request_rationale_title)
            .setMessage(R.string.perm_request_rationale)
            .setPositiveButton(R.string.request_perm_again) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.dismiss){_, _ ->
                sendAlert()
            }
            .create()
            .show()
    }

    private fun enableMyLocation(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(isPermissionGranted){
            locationPermissionGranted = true
            getDeviceLocation()
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
/*                val permissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )*/
                //requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                sendAlert()
            }else{
                sendAlert()
                //requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getDeviceLocation() {
        try {
            Log.i("getDeviceLocation", "$locationPermissionGranted")
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        Log.i("getDeviceLocation", task.result?.longitude.toString())

                        if (lastKnownLocation != null) {
                            //zoom to the user location after taking his permission
                            Log.i("getDeviceLocation", "moving camera to user location")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                                , cameraDefaultZoom.toFloat())
                            )
                            map.addMarker(MarkerOptions()
                                .position(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                                .title("Marker in your actual location")
                            )
                        }else{
                            //zoom to defaultLocation after taking his permission
                            Log.i("getDeviceLocation", "moving camera to default location")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(defaultLocation.latitude, defaultLocation.longitude)
                                , cameraDefaultZoom.toFloat())
                            )
                            map.addMarker(MarkerOptions().
                            position(defaultLocation).
                            title("Marker in default location"))
                            map.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                    else {
                        Log.i("getDeviceLocation", "getting location task wasn't successfully")
                        // zoom to the default location after taking his permission
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(defaultLocation.latitude, defaultLocation.longitude)
                            , cameraDefaultZoom.toFloat())
                        )
                        map.addMarker(MarkerOptions()
                            .position(defaultLocation)
                            .title("Marker in default location"))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }else{
                Log.i("getDeviceLocation", "getting location task wasn't successfully")
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(defaultLocation.latitude,
                            defaultLocation.longitude
                        ), cameraDefaultZoom.toFloat()))
                map.addMarker(MarkerOptions().
                position(defaultLocation).
                title("Marker in default location"))
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

}


