package com.example.conductor.utils
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth

object Constants {
    const val REQUEST_POST_NOTIFICATIONS_PERMISSION = 27
    const val REQUEST_CAMERA_PERMISSION = 28
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    //esta ubicación esta en san francisco con placer
    val defaultLocation = LatLng(-33.47536870666403, -70.64367761577908)
    const val cameraDefaultZoom = 10.7
    val firebaseAuth = FirebaseAuth.getInstance()
    private const val PACKAGE_NAME = "com.example.conductor"
    const val REQUEST_TAKE_PHOTO = 10
    internal const val ACTION_LOCATION_BROADCAST = "$PACKAGE_NAME.action.LOCATION_BROADCAST"
    const val ACTION_MAP_LOCATION_BROADCAST = "$PACKAGE_NAME.action.MAP_LOCATION_BROADCAST"
    internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    const val NOTIFICATION_CHANNEL_ID = "jorge_gas_empresa"
    const val backend_url = "http://192.168.1.56:8080"
    //const val backend_url = "https://ahora-si-2-dkc7p57skq-tl.a.run.app"
}



