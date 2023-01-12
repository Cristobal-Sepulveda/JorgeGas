package com.example.conductor.ui.crearusuario

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentCrearUsuarioBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class CrearUsuarioFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCrearUsuarioBinding? = null
    private val _viewModel: CrearUsuarioViewModel by inject()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearUsuarioBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        _binding!!.buttonDataUsuarioVolver.setOnClickListener {
            this.dismiss()
        }

        _binding!!.buttonDataUsuarioConfirmar.setOnClickListener {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    canICreateANewAccountValidator()
                }
            }
        }
        return _binding!!.root
    }

    private suspend fun canICreateANewAccountValidator() {
        val nombre = _binding!!.editTextDataUsuarioPassword.text.toString()
        val aPaterno = _binding!!.editTextDataUsuarioPassword.text.toString()
        val aMaterno = _binding!!.editTextDataUsuarioPassword.text.toString()
        val email = _binding!!.editTextDataUsuarioUsuario.text.toString()
        val password = _binding!!.editTextDataUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextDataUsuarioConfirmarPassword.text.toString()

        if (nombre.isEmpty() || aPaterno.isEmpty() ||
            aMaterno.isEmpty() || email.isEmpty() ||
            password.isEmpty() || password2.isEmpty()
        ) {
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "Debes completar todos los campos antes de crear una cuenta.",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "El email que ingresaste no es valido",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
            return
        }
        if (password != password2) {
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "Las contraseñas no coincide.",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
            return
        }
        if (password.length < 6) {
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "La contraseña debe tener a lo menos 6 caracteres.",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
            return
        }

        try{
            val aux = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val usuario = Usuario(
                aux.user!!.uid,
                nombre,
                aPaterno,
                aMaterno,
                email,
                password,
                false
            )
            _viewModel.ingresarUsuarioAFirestore(usuario)
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "La cuenta ha sido creada con exito.",
                    Snackbar.LENGTH_SHORT
                ).show()
            };

        }catch(e:Exception){
            Log.i("asd","$e.message")
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
        }
    }

}