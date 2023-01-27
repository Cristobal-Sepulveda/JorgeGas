package com.example.conductor.utils
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object Constants {

    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    //esta ubicación esta en san francisco con placer
    val defaultLocation = LatLng(-33.47536870666403, -70.64367761577908)
    const val cameraDefaultZoom = 10.7
    val firebaseAuth = FirebaseAuth.getInstance()
    private const val PACKAGE_NAME = "com.example.conductor"
    internal const val ACTION_LOCATION_BROADCAST = "$PACKAGE_NAME.action.LOCATION_BROADCAST"
    internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    const val NOTIFICATION_CHANNEL_ID = "jorge_gas_empresa"
}



