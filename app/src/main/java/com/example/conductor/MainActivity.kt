package com.example.conductor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.utils.Constants
import com.example.conductor.utils.Constants.firebaseAuth
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), MenuProvider{

    private lateinit var binding: ActivityMainBinding
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var firebaseUser: String
    // The entry point to the Fused Location Provider.
    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("MainActivity", "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        definingDrawableMenu()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)
        //val a = LoaderManager.getInstance(this)
        startingPermissionCheck()
        checkDeviceLocationSettings()
        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            logout()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout = binding.drawerLayout
        NavigationUI.navigateUp(navController,drawerLayout)
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.overflow_menu,menu )
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId){
            R.id.modoClaro -> {
                showToast()
            }
            R.id.modoOscuro ->{
                showToast()
            }
            R.id.acercaDe -> {
                showToast()
            }
            R.id.navigation_administrar_cuentas -> {
                return NavigationUI.onNavDestinationSelected(
                    menuItem,
                    navController)
            }
        }
        return false

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == Constants.REQUEST_TURN_DEVICE_LOCATION_ON){
            if(resultCode == Activity.RESULT_CANCELED){
                checkDeviceLocationSettings()
            }
        }
    }

    private fun definingDrawableMenu(){
        firebaseUser = firebaseAuth.currentUser!!.email.toString()
        if( firebaseUser != "1@1.1"){
            binding.navView.menu.findItem(R.id.navigation_administrar_cuentas).isVisible = false
        }
    }

    private fun startingPermissionCheck() {
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if(!isPermissionGranted){
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()){ isGranted ->
                when{
                    !isGranted -> {
                        Snackbar.make(
                            binding.root,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
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
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

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
                        Constants.REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d("asd", "Error geting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
    }

    private fun showToast(){
        Toast.makeText(
                this,
                "Esta funcionalidad se implementará en un futuro.",
                Toast.LENGTH_LONG).show()
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        this.finish()
        startActivity(Intent(this, AuthenticationActivity::class.java))
    }

}