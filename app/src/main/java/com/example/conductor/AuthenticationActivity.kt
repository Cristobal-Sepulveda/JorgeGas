package com.example.conductor

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.example.conductor.databinding.ActivityAuthenticationBinding
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val cloudDB = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        lifecycleScope.launch {
            withContext(Dispatchers.Default){
                hayUsuarioLogeado()
            }
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
            val userInValid = cloudDB.collection("Usuarios")
                .document(user.uid).get().await().get("deshabilitada")
            if(!(userInValid as Boolean)) {
                val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                finish()
                startActivity(intent)
            }else{
                runOnUiThread {
                    Toast.makeText(this@AuthenticationActivity,
                        getString(R.string.cuenta_deshabilitada),Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    /** Give users the option to sign in / register with their email or Google account.
     * If users choose to register with their email, they will need to create a password as well.*/
    private fun launchSignInFlow() {
        val email = binding.edittextEmail.text.toString()
        val password = binding.edittextPassword.text.toString()
        if(!TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        try{
                            lifecycleScope.launch{
                                withContext(Dispatchers.IO){
                                    val userInValid = cloudDB.collection("Usuarios")
                                        .whereEqualTo("usuario",email).get().await()
                                    if(userInValid.documents[0].get("deshabilitada") as Boolean){
                                        runOnUiThread {
                                            Toast.makeText(this@AuthenticationActivity,
                                                getString(R.string.cuenta_deshabilitada),Toast.LENGTH_SHORT).show()
                                        }
                                        return@withContext
                                    }else{
                                        val intent = Intent(this@AuthenticationActivity, MainActivity::class.java)
                                        finish()
                                        startActivity(intent)
                                    }
                                }
                            }
                        }catch(e:Exception){
                            Toast.makeText(this@AuthenticationActivity,
                                e.message,Toast.LENGTH_SHORT).show()
                        }
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
}

