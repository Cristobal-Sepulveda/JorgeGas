package com.example.conductor

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val cloudDB = FirebaseFirestore.getInstance()
    private val dataSource: AppDataSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen()

        runBlocking {
            hayUsuarioLogeado()
        }

        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication)


        binding.loginButton.setOnClickListener {
            launchSignInFlow()
        }


    }




    private suspend fun hayUsuarioLogeado() {
        val user = firebaseAuth.currentUser


        if (user != null) {
            try {
                val userInValid = cloudDB.collection("Usuarios")
                    .document(user.uid).get().await().get("deshabilitada")

                if (!(userInValid as Boolean)) {
                    val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                } else {
                    controlDeError(message = R.string.login_error_cuenta_deshabilitada)
                }
            } catch (e: Exception) {
                controlDeError(exception = e)
            }
        }
    }





    private fun launchSignInFlow() {

        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        //aquí chequeo si hay internet
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
            val email = binding.edittextEmail.text.toString()
            val password = binding.edittextPassword.text.toString()
            preIntentarLogin(email, password)
        } else {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
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

                val chequeoDeCredenciales = firebaseAuth.signInWithEmailAndPassword(email, password)

                chequeoDeCredenciales.addOnSuccessListener {

                    val operation = cloudDB
                        .collection("Usuarios")
                        .whereEqualTo("usuario", email)
                        .get()

                    operation.addOnSuccessListener { result ->
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                val inputMethodManager =
                                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                inputMethodManager.hideSoftInputFromWindow(
                                    currentFocus?.windowToken,
                                    0
                                )
                                iniciandoLogin(result, email, password)
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



    private suspend fun iniciandoLogin(result: QuerySnapshot, email: String, password: String) {

        if (controlDeErrorDeInputsMonoSesionYUsuarioDeshabilitado(result,email,password)){
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

                    finish()
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            guardandoDocumentoDelUsuario(result)
                        }
                    }
                    startActivity(intent)
                }

                ultimoControlDeError.addOnFailureListener{
                    controlDeError(exception = it)
                }
            }
        }

    }













    private fun controlDeErrorDeInputsMonoSesionYUsuarioDeshabilitado(result: QuerySnapshot, email: String, password: String): Boolean {
        //primer error controlado: Chequear si el ambos input tienen valores, se dejo aqui por limpieza del codigo
        if(email =="" || password ==""){
            controlDeError(message = R.string.login_error_campos_vacios )
            return true
        }
        //segundo error controlado: Chequear si el usuario tiene sesión iniciada en otro celular
        if (result.documents[0].get("sesionActiva") as Boolean) {
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
            rol = "${result.documents[0].get("rol")}")

        dataSource.guardarUsuarioEnSqlite(usuarioASqlite)
    }

    private fun controlDeError(exception: Exception? = null,  message: Int? = null) {
        if(exception == null){
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                Snackbar.make(
                    findViewById(R.id.container),
                    message!!,
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }else{
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                Snackbar.make(
                    findViewById(R.id.container),
                    "Error: ${exception.message}",
                    Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }
    }



}

