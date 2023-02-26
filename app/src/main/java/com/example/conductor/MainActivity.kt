package com.example.conductor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
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
import com.example.conductor.utils.Constants
import com.example.conductor.utils.Constants.REQUEST_CAMERA_PERMISSION
import com.example.conductor.utils.Constants.REQUEST_POST_NOTIFICATIONS_PERMISSION
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity(), MenuProvider {

    private lateinit var binding: ActivityMainBinding
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var userInValid: QuerySnapshot
    private val cloudDB = FirebaseFirestore.getInstance()
    private val dataSource: AppDataSource by inject()

    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

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
        pintandoSideBarMenuYBottomAppBarSegunElPerfilDelUsuario()

        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    launchLogoutFlow()
                }
            }
            true
        }
    }

    override fun onStart() {
        super.onStart()
        checkingPermissionsSettings()
        checkingDeviceLocationSettings()
    }

    override fun onDestroy() {
        runBlocking {
            dataSource.eliminarUsuariosEnSqlite()
        }
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout = binding.drawerLayout
        NavigationUI.navigateUp(navController, drawerLayout)
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.overflow_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.modoClaro -> {
                Toast.makeText(
                    this,
                    "Esta funcionalidad se implementará en un futuro.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.modoOscuro -> {
                Toast.makeText(
                    this,
                    "Esta funcionalidad se implementará en un futuro.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.acercaDe -> {
                Toast.makeText(
                    this,
                    "Esta funcionalidad se implementará en un futuro.",
                    Toast.LENGTH_LONG
                ).show()
            }
            R.id.navigation_administrar_cuentas -> {
                return NavigationUI.onNavDestinationSelected(
                    menuItem,
                    navController
                )
            }
        }
        return false

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_TURN_DEVICE_LOCATION_ON) {
            if (resultCode == Activity.RESULT_CANCELED) {
                checkingDeviceLocationSettings()
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                checkingPermissionsSettings()
            }
        }
        if (requestCode == REQUEST_POST_NOTIFICATIONS_PERMISSION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                checkingPermissionsSettings()
            }
        }
    }

    private fun pintandoSideBarMenuYBottomAppBarSegunElPerfilDelUsuario() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val user = dataSource.obtenerUsuariosDesdeSqlite()[0]

                if (user.rol != "Administrador") {
                    binding.navView.menu.findItem(R.id.navigation_gestion_de_volanteros).isVisible =
                        false
                }

                if (user.rol == "Volantero") {
                    binding.fragmentBaseInterface.bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }

    private fun checkingPermissionsSettings(resolve: Boolean = true) {


        val isPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        val isPostNotificationsPermissionGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            }




        if (!isPermissionGranted) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                when {
                    !isGranted -> {
                        Snackbar.make(
                            binding.root,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.settings) {
                            startActivityForResult(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts(
                                    "package",
                                    "com.example.conductor",
                                    null
                                )
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }, 1001)
                        }.show()
                    }
                }
            }
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }





        if (!isCameraPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }





        if (!isPostNotificationsPermissionGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }


    }


    private fun checkingDeviceLocationSettings(resolve: Boolean = true) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                30000
            ).apply {
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

            clientLocationSettings.addOnSuccessListener { }
            clientLocationSettings.addOnFailureListener {
                if (it is ResolvableApiException && resolve) {
                    try {
                        it.startResolutionForResult(this, Constants.REQUEST_TURN_DEVICE_LOCATION_ON)
                    } catch (sendEx: IntentSender.SendIntentException) {
                        Log.d(
                            "MainActivity", "Error getting location settings resolution: " +
                                    "${sendEx.message}"
                        )
                    }
                }
            }
        }
    }


    private suspend fun launchLogoutFlow() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        //aquí chequeo si hay internet
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
            logout()
        } else {
            Snackbar.make(
                binding.root,
                R.string.no_hay_internet,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }


    private suspend fun logout() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                var continueLogout = true

                val user = dataSource.obtenerUsuariosDesdeSqlite()[0]

                if (user.rol == "Volantero") {

                    val registroTrayectoVolanterosUsuario = cloudDB
                        .collection("RegistroTrayectoVolanteros")
                        .document(firebaseAuth.currentUser!!.uid)
                        .get()



                    registroTrayectoVolanterosUsuario.addOnSuccessListener { document ->
                        if (document.exists() && document.data!!["estaActivo"] as Boolean) {
                            val docRef = cloudDB.collection("RegistroTrayectoVolanteros")
                                .document(firebaseAuth.currentUser!!.uid)
                                .update("estaActivo", false)

                            docRef.addOnFailureListener {
                                continueLogout = false
                                Snackbar.make(
                                    binding.root,
                                    it.message.toString(),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            }
                        }
                    }

                    registroTrayectoVolanterosUsuario.addOnFailureListener {
                        continueLogout = false
                        Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }
                }


                if (continueLogout) {
                    val docRef = cloudDB.collection("Usuarios")
                        .document(firebaseAuth.currentUser!!.uid)
                        .update("sesionActiva", false)

                    docRef.addOnFailureListener() {
                        continueLogout = false
                        Snackbar.make(binding.root, it.message.toString(), Snackbar.LENGTH_LONG)
                            .show()
                    }

                    docRef.addOnSuccessListener {
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                dataSource.eliminarUsuariosEnSqlite()
                                FirebaseAuth.getInstance().signOut()
                                this@MainActivity.finish()
                                startActivity(
                                    Intent(
                                        this@MainActivity,
                                        AuthenticationActivity::class.java
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
