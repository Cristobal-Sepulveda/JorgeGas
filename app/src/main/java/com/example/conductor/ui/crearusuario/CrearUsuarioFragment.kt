package com.example.conductor.ui.crearusuario

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.example.conductor.base.BaseFragment
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

class CrearUsuarioFragment : BaseFragment() {

    private var _binding: FragmentCrearUsuarioBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearUsuarioBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val roles = listOf("Administrador","Conductor","Peoneta","Secretaria", "Volantero" )
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item,roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        _binding!!.editTextDataUsuarioRol.setAdapter(adapter)


        _binding!!.buttonDataUsuarioVolver.setOnClickListener {
/*            this.dismiss()*/
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

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.removeUsuariosInRecyclerView()
        _viewModel.displayUsuariosInRecyclerView()
        Log.i("CrearUsuarioFragment", "onDestroy")
    }

    private suspend fun canICreateANewAccountValidator() {
        val nombre = _binding!!.editTextDataUsuarioNombre.text.toString()
        val apellidos = _binding!!.editTextDataUsuarioApellidos.text.toString()
        val telefono = _binding!!.editTextDataUsuarioTelefono.text.toString()
        val email = _binding!!.editTextDataUsuarioUsuario.text.toString()
        val password = _binding!!.editTextDataUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextDataUsuarioConfirmarPassword.text.toString()
        val rol = "asd"

        if (nombre.isEmpty() || apellidos.isEmpty() ||
            email.isEmpty() || password.isEmpty() ||
            password2.isEmpty() || rol.isEmpty()
        ) {
            Snackbar.make(
                _binding!!.root,
                "Debes completar todos los campos antes de crear una cuenta.",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(
                _binding!!.root,
                "El email que ingresaste no es valido",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (password != password2) {
            Snackbar.make(
                _binding!!.root,
                "Las contraseñas no coincide.",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if (password.length < 6) {
            Snackbar.make(
                _binding!!.root,
                "La contraseña debe tener a lo menos 6 caracteres.",
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }
        if(apellidos.split(" ").size !=2){
            Snackbar.make(
                _binding!!.root,
                "Ingrese los 2 apellidos separados por un espacio",
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }
        try{
            val aux = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val usuario = Usuario(
                aux.user!!.uid,
                nombre,
                apellidos,
                telefono,
                email,
                password,
                false,
                rol
            )

            _viewModel.ingresarUsuarioAFirestore(usuario)
            Snackbar.make(
                _binding!!.root,
                "La cuenta ha sido creada con exito.",
                Snackbar.LENGTH_SHORT
            ).show()
        }catch(e:Exception){
            Log.i("asd","$e.message")

            Snackbar.make(
                _binding!!.root,
                "${e.message}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
}