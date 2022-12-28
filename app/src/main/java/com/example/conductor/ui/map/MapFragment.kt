package com.example.conductor.ui.map

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject


class MapFragment() : BaseFragment(), OnMapReadyCallback {

    //>>>>>>>>>>>>>>>>>>>>>>>>>>some constants and variables >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //Use Koin to get the view model of the MapViewModel
    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private lateinit var map: GoogleMap
    private val DEFAULT_ZOOM = 15
    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var locationPermissionGranted = false
    private val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
    private val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34
    private val LOCATION_PERMISSION_INDEX = 0
    private val BACKGROUND_LOCATION_PERMISSION_INDEX = 1

    // The entry point to the Fused Location Provider.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val root: View = binding.root
        // Specify the current activity as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates

        //Adding  the map setup implementation

        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkPermissionsAndGetDeviceLocation()
        setMapStyle(map)
/*        onFieldSelected()
        markingFields()*/
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }
            ft.detach(this).attach(this).commit()
        }
    }

    @ExperimentalStdlibApi
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.show()

        /*popup.setOnMenuItemClickListener { menuItem: MenuItem ->
        }*/
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this.requireContext(),
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

    private fun checkPermissionsAndGetDeviceLocation() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            locationPermissionGranted = true
            getDeviceLocation()
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)) {
            Snackbar.make(
                binding.root,
                "Location services must be enabled to use the app",
                Snackbar.LENGTH_LONG
            ).setAction(R.string.settings) {
                    startActivityForResult(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package",
                            "com.example.android.onematchproject",
                            null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },1001)
                }.show()
        }else{
            val ft: FragmentTransaction = requireFragmentManager().beginTransaction()
            if (Build.VERSION.SDK_INT >= 26) {
                ft.setReorderingAllowed(false)
            }
            ft.detach(this).attach(this).commit()
        }
    }

    private fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                Log.i("getDeviceLocation", "$locationPermissionGranted")
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
                                , DEFAULT_ZOOM.toFloat())
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
                                    , DEFAULT_ZOOM.toFloat())
                            )
                            map.addMarker(MarkerOptions().
                            position(defaultLocation).
                            title("Marker in default location"))
                            map.uiSettings?.isMyLocationButtonEnabled = false
                        }
                    }
                    else {
                        Log.i("getDeviceLocation", "getting location task wasn't successfully")
                        // zoom to the default location after taking his permission
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(defaultLocation.latitude, defaultLocation.longitude)
                                , DEFAULT_ZOOM.toFloat())
                        )
                        map.addMarker(MarkerOptions()
                            .position(defaultLocation)
                            .title("Marker in default location"))
                        map.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }else{
                Log.i("getDeviceLocation", "getting location task wasn't successfully")
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(defaultLocation.latitude,
                            defaultLocation.longitude
                        ), DEFAULT_ZOOM.toFloat()))
                map.addMarker(MarkerOptions().
                position(defaultLocation).
                title("Marker in default location"))
                map.uiSettings?.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @TargetApi(29)
    fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(requireActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                            requireActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            }else{
                true
            }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved())
            return
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = when {
            runningQOrLater -> {
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }
        requestPermissions(
            permissionsArray,
            resultCode
        )
    }

    /**
     * This method is for get an drawable and draw an icon to put on the GoogleMaps. This is done
     * in this way because to mark an item on GoogleMaps, the .icon needs a BitmapDescriptor? &
     * the drawable is an Int.
     */
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(
            0,
            0,
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight
        )
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * This method is used in onMapReady() and is unchain before we get the field's info from the LOCAL_DATABASE.
     * IF WE DONT HAVE DATA IN THE DB, we GET THE DATA FROM CLOUDFIRESTORE and save it in LOCAL_DATABASE.
     * This method reduce the consults that the user does to the cloud database.
     *
     * For more info, watch how works the method getFields() and write Launched in LOGCAT.i
     */
/*    private fun markingFields(){
        Log.i("Launched", "markingFields")
        val fields = canchas_maipu
        Log.i("Launched", "markingFields: $fields")
        var i = 0
        while(i < fields.size){
            val field = fields[i]
            val latLng = LatLng(field.latitude, field.longitude)
            map.addMarker(MarkerOptions()
                .position(latLng)
                .title(field.name as String?)
                .icon(bitmapDescriptorFromVector(requireContext(), R.drawable.ic_baseline_sports_soccer))
            )
            i++
        }
    }*/

/*    private fun onFieldSelected() {
        map.setOnMarkerClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(MapFragmentDirections.actionNavMapToNavSingleField())
            true
        }
    }*/
}


