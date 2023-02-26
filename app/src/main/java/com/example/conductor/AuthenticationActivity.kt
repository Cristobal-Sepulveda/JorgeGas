package com.example.conductor

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
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

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val cloudDB = FirebaseFirestore.getInstance()

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

    private suspend fun hayUsuarioLogeado(){
        val user = firebaseAuth.currentUser
        if (user!= null) {
            try{
                val userInValid = cloudDB.collection("Usuarios")
                    .document(user.uid).get().await().get("deshabilitada")


                if(!(userInValid as Boolean)) {
                    val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                    finish()
                    startActivity(intent)
                }else{
                    runOnUiThread {
                        Toast.makeText(this@AuthenticationActivity,
                            getString(R.string.login_error_cuenta_deshabilitada),Toast.LENGTH_SHORT).show()
                    }
                }
            }catch(e:Exception){
                runOnUiThread {
                    Toast.makeText(this@AuthenticationActivity,
                        e.message,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun launchSignInFlow() {
        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        //aquí chequeo si hay internet
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting) {
            val email = binding.edittextEmail.text.toString()
            val password = binding.edittextPassword.text.toString()
            intentarLogin(email, password)
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

    private fun intentarLogin(email: String, password: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val operation = cloudDB.collection("Usuarios")
                    .whereEqualTo("usuario", email).get()
                operation.addOnSuccessListener { result ->
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            iniciandoControlDeErroresYLogin(result, email, password)
                        }
                    }
                }
                operation.addOnFailureListener { exception ->
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        cerrarTeclado()
                        Snackbar.make(
                            findViewById(R.id.container),
                            "Error: $exception",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private suspend fun iniciandoControlDeErroresYLogin(result: QuerySnapshot, email: String, password: String) {

        if (chequeoDeInputsMonoSesionYDeshabilitado(result,email,password)){
            return
        }

        //Aquí se inicia el login propiamente tal
        try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()

            cloudDB.collection("Usuarios")
                .document(firebaseAuth.currentUser!!.uid)
                .update("sesionActiva", true)
                .await()

            val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)

            finish()

            startActivity(intent)
        }

        catch (e: Exception) {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                cerrarTeclado()
                Snackbar.make(
                    findViewById(R.id.container),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun chequeoDeInputsMonoSesionYDeshabilitado(result: QuerySnapshot, email: String, password: String): Boolean {
        //primer error controlado: Chequear si el ambos input tienen valores, se dejo aqui por limpieza del codigo
        if(email =="" || password ==""){
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                cerrarTeclado()
                Snackbar.make(
                    findViewById(R.id.container),
                    getString(R.string.login_error_campos_vacios),
                    Toast.LENGTH_LONG)
                    .show()
            }
            return true

        }
        //segundo error controlado: Chequear si el usuario tiene sesión iniciada en otro celular
        if (result.documents[0].get("sesionActiva") as Boolean) {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                cerrarTeclado()
                Snackbar.make(
                    findViewById(R.id.container),
                    getString(R.string.sesion_activa_existente),
                    Snackbar.LENGTH_LONG
                ).show()
            }
            return true
        }
        //tercer error controlado: Chequear si el usuario está deshabilitado
        if (result.documents[0].get("deshabilitada") as Boolean) {
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                cerrarTeclado()
                Snackbar.make(
                    findViewById(R.id.container),
                    getString(R.string.login_error_cuenta_deshabilitada),
                    Snackbar.LENGTH_INDEFINITE
                ).show()
            }
            return true
        }
        return false
    }

    private fun cerrarTeclado() {
        val inputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }


}

