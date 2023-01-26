package com.example.conductor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.ui.vistageneral.VistaGeneralFragment
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import com.example.conductor.utils.Constants
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.inject

class MainActivity : AppCompatActivity(), MenuProvider{

    private lateinit var binding: ActivityMainBinding
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private val cloudDB = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.navView
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
        vistaGeneralDrawableMenuYBottomNavigationView()

        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            logout()
            true
        }
    }

    override fun onStart() {
        super.onStart()
        checkingPermissionsSettings()
        checkingDeviceLocationSettings()
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
                checkingDeviceLocationSettings()
            }
        }
    }

    private fun vistaGeneralDrawableMenuYBottomNavigationView(){
        try{
            lifecycleScope.launch{
                withContext(Dispatchers.Main) {
                    val userInValid = cloudDB.collection("Usuarios")
                        .whereEqualTo("usuario", firebaseAuth.currentUser!!.email.toString()).get()
                        .await()
                    Log.i("MainActivity", "${userInValid.documents[0].get("rol")}")
                    if(userInValid.documents[0].get("rol") != "Administrador") {
                        binding.navView.menu.findItem(R.id.navigation_administrar_cuentas).isVisible = false
                    }
                    if(userInValid.documents[0].get("rol") == "Volantero"){
                        binding.fragmentBaseInterface.bottomNavigationView.visibility = View.GONE
                    }
                }
            }
        }catch(e:Exception){
            binding.navView.menu.findItem(R.id.navigation_administrar_cuentas).isVisible = false
            binding.fragmentBaseInterface.bottomNavigationView.visibility = View.GONE
            e.message?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_INDEFINITE).show() }
        }
    }

    private fun checkingPermissionsSettings(resolve:Boolean = true) {
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

    private fun checkingDeviceLocationSettings(resolve:Boolean = true){
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
                30000).apply{
                setMinUpdateDistanceMeters(100f)
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            val locationSettingsRequest = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setNeedBle(true)
                .build()

            val clientLocationSettings = LocationServices
                .getSettingsClient(this)
                .checkLocationSettings(locationSettingsRequest)

            clientLocationSettings.addOnSuccessListener{  }
            clientLocationSettings.addOnFailureListener{
                if(it is ResolvableApiException && resolve){
                    try {
                        it.startResolutionForResult(this, Constants.REQUEST_TURN_DEVICE_LOCATION_ON)
                    }catch (sendEx: IntentSender.SendIntentException){
                        Log.d("MainActivity", "Error getting location settings resolution: " +
                                "${sendEx.message}")
                    }
                }
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