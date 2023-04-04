package com.example.conductor

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.dbo.UsuarioDBO
import com.example.conductor.databinding.ActivityAuthenticationBinding
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val cloudDB = FirebaseFirestore.getInstance()
    val dataSource: AppDataSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)
        binding.imageviewLogoAbastible.visibility = View.VISIBLE
        binding.imageviewLogoJorgeGas.visibility = View.VISIBLE

        lifecycleScope.launch{
            withContext(Dispatchers.IO) {
                hayUsuarioLogeado(firebaseAuth.currentUser)
            }
        }

        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }

    }

    fun hayUsuarioLogeado(user: FirebaseUser?) : Boolean {
        aparecerYDesaparecerElementosAlIniciarLogin()

        if (user != null) {
            val userInvalid = cloudDB
                .collection("Usuarios")
                .document(user.uid).get()

            userInvalid.addOnSuccessListener{
                when(it.get("deshabilitada") as Boolean){
                    false -> {
                        val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                        finish()
                        startActivity(intent)
                    }
                    else -> {
                        Snackbar.make(binding.container,  R.string.login_error_cuenta_deshabilitada, Snackbar.LENGTH_LONG).show()
                        aparecerYDesaparecerElementosTrasNoLogin()
                    }
                }
            }

            userInvalid.addOnFailureListener{
                aparecerYDesaparecerElementosTrasNoLogin()
            }
            return true
        }else{
            aparecerYDesaparecerElementosTrasNoLogin()
            return false
        }
    }

    private fun launchSignInFlow() {
        runOnUiThread {
            aparecerYDesaparecerElementosAlIniciarLogin()
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.edittextPassword.windowToken, 0)
        }

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetworkInfo

        //aquí chequeo si hay internet
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
            val email = binding.edittextEmail.text.toString()
            val password = binding.edittextPassword.text.toString()
            preIntentarLogin(email, password)
        } else {
            runOnUiThread {
                aparecerYDesaparecerElementosTrasNoLogin()
                Snackbar.make(
                    findViewById(R.id.container),
                    R.string.no_hay_internet,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun preIntentarLogin(email: String, password: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                /*primer error controlado: Chequear si el ambos input tienen valores,
                 se dejo aqui por limpieza del codigo*/
                if(email =="" || password ==""){
                    controlDeError(message = R.string.login_error_campos_vacios )
                    return@withContext
                }

                val chequeoDeCredenciales = firebaseAuth.signInWithEmailAndPassword(email, password)

                chequeoDeCredenciales.addOnSuccessListener {
                    val operation = cloudDB
                        .collection("Usuarios")
                        .whereEqualTo("usuario", email)
                        .get()

                    operation.addOnSuccessListener { result ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                iniciandoLogin(result)
                            }
                        }
                    }

                    operation.addOnFailureListener {
                        controlDeError(it)
                    }

                }

                chequeoDeCredenciales.addOnFailureListener{
                    controlDeError(it)
                }
            }
        }
    }

    private suspend fun iniciandoLogin(result: QuerySnapshot) {

        if (controlDeErrorDeMonoSesionYUsuarioDeshabilitado(result)){
            return
        }

/*        if(solicitarTokenDeSesion(this) == "error"){
            runOnUiThread {
                aparecerYDesaparecerElementosTrasNoLogin()
            }
            return
        }*/
        if(!dataSource.guardandoTokenDeFCMEnFirestore()) {
            controlDeError(message = R.string.login_error_falla_en_fcm)
            return
        }

        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                val ultimoControlDeError = cloudDB.collection("Usuarios")
                    .document(firebaseAuth.currentUser!!.uid)
                    .update("sesionActiva", true)

                ultimoControlDeError.addOnSuccessListener {

                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                    }

                    val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            guardandoDocumentoDelUsuario(result)
                        }
                    }
                    finish()
                    startActivity(intent)
                }

                ultimoControlDeError.addOnFailureListener{
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            dataSource.eliminarTokenDeSesion()
                        }
                    }
                    controlDeError(exception = it)
                }
            }
        }

    }

    private fun controlDeErrorDeMonoSesionYUsuarioDeshabilitado(result: QuerySnapshot): Boolean {
        if(result.documents.isEmpty()){
            controlDeError(message = R.string.login_error_credenciales_incorrectas )
            return true
        }
        //segundo error controlado: Chequear si el usuario tiene sesión iniciada en otro celular
        if (result.documents.first().get("sesionActiva") as Boolean) {
            controlDeError(message = R.string.sesion_activa_existente )
            return true
        }
        //tercer error controlado: Chequear si el usuario está deshabilitado

        if (result.documents[0].get("deshabilitada") as Boolean) {
            controlDeError(message = R.string.login_error_cuenta_deshabilitada )
            return true
        }
        return false
    }

    private suspend fun guardandoDocumentoDelUsuario(result: QuerySnapshot) {
        val usuarioASqlite = UsuarioDBO(
            nombre = "${result.documents[0].get("nombre")}",
            apellidos = "${result.documents[0].get("apellidos")}",
            rol = "${result.documents[0].get("rol")}",
            fotoPerfil = "${result.documents[0].get("fotoPerfil")}",
            )

        dataSource.guardarUsuarioEnSqlite(usuarioASqlite)
    }

    private fun controlDeError(exception: Exception? = null,  message: Int? = null) {
        if(exception == null){
            runOnUiThread {
                aparecerYDesaparecerElementosTrasNoLogin()
                Snackbar.make(
                    findViewById(R.id.container),
                    message!!,
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }else{
            runOnUiThread {
                aparecerYDesaparecerElementosTrasNoLogin()
                Snackbar.make(
                    findViewById(R.id.container),
                    "Error: ${exception.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun aparecerYDesaparecerElementosTrasNoLogin() {
        binding.progressBar.visibility = View.GONE
        binding.imageviewLogoAbastible.visibility = View.VISIBLE
        binding.imageviewLogoJorgeGas.visibility = View.VISIBLE
        binding.edittextEmail.visibility = View.VISIBLE
        binding.edittextPassword.visibility = View.VISIBLE
        binding.loginButton.visibility = View.VISIBLE
    }

    private fun aparecerYDesaparecerElementosAlIniciarLogin() {
        binding.progressBar.visibility = View.VISIBLE
        binding.imageviewLogoAbastible.visibility = View.GONE
        binding.imageviewLogoJorgeGas.visibility = View.GONE
        binding.edittextEmail.visibility = View.GONE
        binding.edittextPassword.visibility = View.GONE
        binding.loginButton.visibility = View.GONE
    }

    private suspend fun solicitarTokenDeSesion(context: Context): String{
        return dataSource.solicitarTokenDeSesion(context)
    }
}

