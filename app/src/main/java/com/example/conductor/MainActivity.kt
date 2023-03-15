package com.example.conductor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
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
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.ui.map.MapFragment
import com.example.conductor.utils.Constants
import com.example.conductor.utils.Constants.REQUEST_CAMERA_PERMISSION
import com.example.conductor.utils.Constants.REQUEST_POST_NOTIFICATIONS_PERMISSION
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity(), MenuProvider {

    private lateinit var binding: ActivityMainBinding
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private val cloudDB = FirebaseFirestore.getInstance()
    private val dataSource: AppDataSource by inject()
    private var disableBackButton = false
    private lateinit var rootView: View
    private var imageBitmap: Bitmap? = null

    private val requestTakePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageBitmap = result.data?.extras?.get("data") as? Bitmap
            val foto = parseandoImagenParaSubirlaAFirestore(imageBitmap!!)
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val intentoDeGuardarEnFirestore = dataSource.actualizarFotoDePerfilEnFirestoreYRoom(foto, this@MainActivity)
                    if (intentoDeGuardarEnFirestore) {
                        Log.i("intentoDeGuardarEnFirestore", "true")
                            withContext(Dispatchers.Main) {
                                decodeAndSetImageWithPhoto(foto)
                            }
                    }
                }
            }
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
        rootView = binding.root

        pintandoSideBarMenuYBottomAppBarSegunElPerfilDelUsuario()

        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    launchLogoutFlow()
                }
            }
            true
        }
        binding.navView.getHeaderView(0).findViewById<CircleImageView>(R.id.circleImageView_drawerNavHeader_iconoTomarFoto).setOnClickListener {
            dispatchTakePictureIntent()
        }
    }

    override fun onStart() {
        super.onStart()
        checkingPermissionsSettings()
        checkingDeviceLocationSettings()
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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.map)
        if (fragment is  MapFragment && disableBackButton) {
            // Do nothing to disable the back button
        } else {
            super.onBackPressed()
        }
    }

    private fun pintandoSideBarMenuYBottomAppBarSegunElPerfilDelUsuario() {
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                val user = dataSource.obtenerUsuariosDesdeSqlite()

                if(user.isEmpty()){
                    logout()
                    return@withContext
                }

                ponerElNombreRolYFotoEnElDrawableMenu(user)

                if (user.first().rol.isNotEmpty() && user.first().rol != "Administrador") {
                    binding.navView.menu.findItem(R.id.navigation_gestion_de_volanteros).isVisible = false
                    binding.navView.menu.findItem(R.id.navigation_registro_trayecto_volanteros).isVisible = false
                }

                if (user.first().rol.isNotEmpty() && user.first().rol == "Volantero") {
                    binding.fragmentBaseInterface.bottomNavigationView.visibility = View.GONE
                }
            }
        }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(this.packageManager)?.also {
                requestTakePicture.launch(takePictureIntent)
            }
        }
    }

    private fun ponerElNombreRolYFotoEnElDrawableMenu(user: List<UsuarioDBO>) {
        binding.navView.getHeaderView(0)
            .findViewById<TextView>(R.id.textView_drawerNavHeader_nombreUsuario).text =
            user.first().nombre + " " + user.first().apellidos
        binding.navView.getHeaderView(0)
            .findViewById<TextView>(R.id.textView_drawerNavHeader_rol).text = user.first().rol

        decodeAndSetImageWithUsuarioDBO(user.first())

    }
    private fun decodeAndSetImageWithUsuarioDBO(user: UsuarioDBO){
        val circleImageView = binding.navView.getHeaderView(0)
            .findViewById<CircleImageView>(R.id.circleImageView_drawerNavHeader_fotoPerfil)

        circleImageView.invalidate()

        if (user.fotoPerfil.last().toString() == "=" || (user.fotoPerfil.first().toString() == "/" && user.fotoPerfil[1].toString() == "9")) {
            val decodedString = Base64.decode(user.fotoPerfil, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            circleImageView.setImageBitmap(decodedByte)
        } else {
            val aux2 = user.fotoPerfil.indexOf("=") + 1
            val aux3 = user.fotoPerfil.substring(0, aux2)
            val decodedString = Base64.decode(aux3, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            circleImageView.setImageBitmap(decodedByte)
        }
    }

    private fun decodeAndSetImageWithPhoto(fotoPerfil: String){
        val circleImageView = binding.navView.getHeaderView(0)
            .findViewById<CircleImageView>(R.id.circleImageView_drawerNavHeader_fotoPerfil)

        circleImageView.invalidate()

        if (fotoPerfil.last().toString() == "=" || (fotoPerfil.first().toString() == "/" && fotoPerfil[1].toString() == "9")) {
            val decodedString = Base64.decode(fotoPerfil, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            circleImageView.setImageBitmap(decodedByte)
        } else {
            val aux2 = fotoPerfil.indexOf("=") + 1
            val aux3 = fotoPerfil.substring(0, aux2)
            val decodedString = Base64.decode(aux3, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            circleImageView.setImageBitmap(decodedByte)
        }
    }

    private fun checkingPermissionsSettings(resolve: Boolean = true) {
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                for (result in results) {
                    if (!result.value) {
                        Snackbar.make(
                            binding.root,
                            R.string.permission_denied_explanation,
                            Snackbar.LENGTH_INDEFINITE
                        ).setAction(R.string.settings) {
                            startActivityForResult(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", packageName, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }, 1001)
                        }.show()
                        break
                    }
                }
            }
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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

                val user = dataSource.obtenerUsuariosDesdeSqlite()

                if(user.isEmpty()){
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
                    return@withContext
                }

                if (user.first().rol.isNotEmpty() && user.first().rol == "Volantero") {

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

    private fun parseandoImagenParaSubirlaAFirestore(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return Base64.encodeToString(data, Base64.NO_PADDING)
    }

    fun setDisableBackButton(disable: Boolean) {
        disableBackButton = disable
    }
}
