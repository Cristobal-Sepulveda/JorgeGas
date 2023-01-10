package com.example.conductor

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import com.example.conductor.utils.Constants.REQUEST_TURN_DEVICE_LOCATION_ON
import com.example.conductor.utils.Constants.SIGN_IN_RESULT_CODE
import com.example.conductor.utils.Constants.TAG
import com.example.conductor.databinding.ActivityAuthenticationBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.IdpResponse

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }

        hayUsuarioLogeado()
        checkDeviceLocationSettings()
    }

    private fun hayUsuarioLogeado(){
        if (firebaseAuth.currentUser!= null){
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }

    }
    /** Give users the option to sign in / register with their email or Google account.
    * If users choose to register with their email, they will need to create a password as well.*/
    private fun launchSignInFlow() {
        val email = binding.edittextEmail.text.toString()
        val password = binding.edittextPassword.text.toString()

        if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
            Log.i("login", "Iniciando login")
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            this,
                            "Usuario y/o contraseña incorrectos.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        /** Here im specifically listening for the sign-in request code to come back,
         * if its still the same, process the login, else, its because the login was unsuccessful*/
        if (requestCode == SIGN_IN_RESULT_CODE){
            //we start by getting the response from the resulting intent
            val response = IdpResponse.fromResultIntent(data)
            //then we check the resultCode to see what the result of the login was
            if(resultCode == Activity.RESULT_OK){
                //User successfully signed in
                //          TODO: If the user was authenticated, send him to RemindersActivity
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}")
                val firebaseAuth = FirebaseAuth.getInstance()
                if(firebaseAuth.currentUser != null) {
                    val intent = Intent(this, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                }
            }else{
                Log.i(TAG, "Sign in was unsuccessful ${response?.error?.errorCode}")
            }
        }
        if(requestCode == REQUEST_TURN_DEVICE_LOCATION_ON ){
            if(resultCode == Activity.RESULT_CANCELED){
                checkDeviceLocationSettings()
            }
        }
    }
//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    /**
     * Uses the Location Client to check the current state of location settings, and gives the user
     * the opportunity to turn on location services within our app.
     * */
    private fun checkDeviceLocationSettings(resolve:Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(this)
        val locationSettingsResponseTask =
                settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this,
                            REQUEST_TURN_DEVICE_LOCATION_ON)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("asd", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        binding.container,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
    }

}

