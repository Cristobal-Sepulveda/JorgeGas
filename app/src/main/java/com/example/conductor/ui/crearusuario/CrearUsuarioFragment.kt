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
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
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
    private val _viewModel: AdministrarCuentasViewModel by inject()
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
        Log.i("CrearUsuarioFragment", "onCreateView")
        return _binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.removeUsuariosInRecyclerView()
        _viewModel.displayUsuariosInRecyclerView()
        Log.i("CrearUsuarioFragment", "onDestroy")
    }

    private suspend fun canICreateANewAccountValidator() {
        val nombre = _binding!!.editTextDataUsuarioNombre.text.toString()
        val aPaterno = _binding!!.editTextDataUsuarioAPaterno.text.toString()
        val aMaterno = _binding!!.editTextDataUsuarioAMaterno.text.toString()
        val rol = _binding!!.editTextDataUsuarioRol.text.toString()
        val email = _binding!!.editTextDataUsuarioUsuario.text.toString()
        val password = _binding!!.editTextDataUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextDataUsuarioConfirmarPassword.text.toString()

        if (nombre.isEmpty() || aPaterno.isEmpty() ||
            aMaterno.isEmpty() || rol.isEmpty() ||
            email.isEmpty() || password.isEmpty() ||
            password2.isEmpty()
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
                false,
                rol
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