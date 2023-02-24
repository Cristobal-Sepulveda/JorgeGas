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
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.databinding.DrawerNavHeaderBinding
import com.example.conductor.ui.vistageneral.VistaGeneralFragment
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import com.example.conductor.utils.Constants
import com.example.conductor.utils.Constants.REQUEST_CAMERA_PERMISSION
import com.example.conductor.utils.Constants.REQUEST_POST_NOTIFICATIONS_PERMISSION
import com.example.conductor.utils.Constants.firebaseAuth
import com.example.conductor.utils.notificationGenerator
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
import org.koin.androidx.compose.inject

class MainActivity : AppCompatActivity(), MenuProvider{

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
        vistaGeneralDrawableMenuYBottomNavigationView()


/*        val asd = DrawerNavHeaderBinding.bind(binding.navView.getHeaderView(0))
        asd.textViewNombreUsuario.setOnClickListener {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    logout()
                }
            }
        }*/

        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    logout()
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
            dataSource.eliminarUsuarioEnSqlite()
        }
        super.onDestroy()
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
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(resultCode == Activity.RESULT_CANCELED){
                checkingPermissionsSettings()
            }
        }
        if(requestCode == REQUEST_POST_NOTIFICATIONS_PERMISSION){
            if(resultCode == Activity.RESULT_CANCELED){
                checkingPermissionsSettings()
            }
        }
    }

    private fun vistaGeneralDrawableMenuYBottomNavigationView(){
        try{
            lifecycleScope.launch{
                withContext(Dispatchers.Main) {
                    userInValid = cloudDB.collection("Usuarios")
                        .whereEqualTo("usuario", firebaseAuth.currentUser!!.email.toString()).get()
                        .await()
                    val usuarioASqlite = UsuarioDBO(
                        nombre = "${userInValid.documents[0].get("nombre")}",
                        apellidos = "${userInValid.documents[0].get("apellidos")}",
                        rol = "${userInValid.documents[0].get("rol")}")

                    dataSource.guardarUsuarioEnSqlite(usuarioASqlite)

                    Log.i("MainActivity", "${userInValid.documents[0].get("rol")}")
                    if(userInValid.documents[0].get("rol") != "Administrador" || userInValid.documents[0].get("rol") != "Supervisor Volantero") {
                        /*binding.navView.menu.findItem(R.id.navigation_administrar_cuentas).isVisible = false*/
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

        val isCameraPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        val isPostNotificationsPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }

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

        if(!isCameraPermissionGranted){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
        if(!isPostNotificationsPermissionGranted){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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

    private suspend fun logout(){
        try{
            if(userInValid.documents[0].get("rol") == "Volantero"){
                val registroTrayectoVolanterosUsuario = cloudDB
                    .collection("RegistroTrayectoVolanteros")
                    .document(firebaseAuth.currentUser!!.uid)
                    .get().await()
                if(registroTrayectoVolanterosUsuario.exists() &&registroTrayectoVolanterosUsuario.data!!["estaActivo"] as Boolean) {
                    cloudDB
                        .collection("RegistroTrayectoVolanteros")
                        .document(firebaseAuth.currentUser!!.uid)
                        .update("estaActivo", false)
                }
            }
            cloudDB.collection("Usuarios")
                .document(firebaseAuth.currentUser!!.uid)
                .update("sesionActiva", false).await()
            FirebaseAuth.getInstance().signOut()
            this.finish()
            startActivity(Intent(this, AuthenticationActivity::class.java))
        }catch(e:Exception){
            notificationGenerator(this, e.message.toString())
        }
    }

}