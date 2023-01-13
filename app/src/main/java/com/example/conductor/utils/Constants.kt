package com.example.conductor.utils
import android.graphics.Color
import com.google.firebase.auth.FirebaseAuth

object Constants {

    const val TAG = "AuthenticationActivity"
    const val SIGN_IN_RESULT_CODE = 1001
    const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
    val firebaseAuth = FirebaseAuth.getInstance()
}

var polygonsColor: ArrayList<ArrayList<Color>> =

