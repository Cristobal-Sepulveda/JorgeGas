package com.example.conductor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.conductor.databinding.ActivityAuthenticationBinding
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

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
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    launchSignInFlow()
                }
            }
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

    /** Give users the option to sign in / register with their email or Google account.
     * If users choose to register with their email, they will need to create a password as well.*/
    private suspend fun launchSignInFlow() {
        val email = binding.edittextEmail.text.toString()
        val password = binding.edittextPassword.text.toString()
        if(email !="" && password!=""){
            try{
                binding.progressBar.visibility = View.VISIBLE
                lifecycleScope.launch{
                    withContext(Dispatchers.IO){
                        val userInValid = cloudDB.collection("Usuarios")
                            .whereEqualTo("usuario",email).get().await()

                        if(userInValid.documents[0].get("sesionActiva") as Boolean){
                            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                            runOnUiThread {
                                binding.progressBar.visibility = View.GONE
                                Snackbar.make(findViewById(R.id.container),
                                    getString(R.string.sesion_activa_existente),Toast.LENGTH_SHORT).show()
                            }
                            return@withContext
                        }

                        if(userInValid.documents[0].get("deshabilitada") as Boolean){
                            binding.progressBar.visibility = View.GONE
                            runOnUiThread {
                                Toast.makeText(this@AuthenticationActivity,
                                    getString(R.string.login_error_cuenta_deshabilitada),Toast.LENGTH_SHORT).show()
                            }
                            return@withContext
                        }else{
                            binding.progressBar.visibility = View.GONE
                            firebaseAuth.signInWithEmailAndPassword(email, password).await()
                            cloudDB.collection("Usuarios")
                                .document(firebaseAuth.currentUser!!.uid)
                                .update("sesionActiva", true).await()
                            val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                            finish()
                            startActivity(intent)
                        }
                    }
                }
            }catch(e:Exception){
                binding.progressBar.visibility = View.GONE
                if(e.message.toString().contains("network")){
                    runOnUiThread{
                        Toast.makeText(this@AuthenticationActivity, getString(R.string.login_error_no_internet),Toast.LENGTH_SHORT).show()
                    }
                }else{
                    runOnUiThread{
                        Toast.makeText(this@AuthenticationActivity, getString(R.string.login_error_credenciales_erroneas),Toast.LENGTH_SHORT).show()
                    }
                }

            }
        }else{
            runOnUiThread{
                Toast.makeText(this@AuthenticationActivity, "Por favor, complete todos los campos.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

