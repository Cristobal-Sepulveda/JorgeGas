package com.example.conductor.ui.editarusuario

import android.R
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.conductor.ui.estadoactual.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.example.conductor.utils.Constants
import com.example.conductor.utils.NavigationCommand
import com.example.conductor.databinding.FragmentEditarUsuarioBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class EditarUsuarioFragment : BaseFragment() {

    private var _binding: FragmentEditarUsuarioBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditarUsuarioBinding.inflate(inflater, container, false)
        val bundle = EditarUsuarioFragmentArgs.fromBundle(requireArguments()).usuarioDetails
        cargarPlanillaConLosDatosDelUsuarioClickeado(bundle)


        _binding!!.textViewEditarUsuarioVolver.setOnClickListener {
            _viewModel.navigationCommand.value = NavigationCommand.To(
                EditarUsuarioFragmentDirections
                    .actionNavigationEditarUsuarioToNavigationAdministrarCuentas())
        }

        _binding!!.buttonEditarUsuarioConfirmar.setOnClickListener {
            canIEditTheUserValidator(bundle)
        }

        _binding!!.circleImageViewEditarUsuarioIconoTomarFotoUsuario.setOnClickListener {
            dispatchTakePictureIntent()
        }

        return _binding!!.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _viewModel.cleanUsuarioDetails()
        _viewModel.removeUsuariosInRecyclerView()
        _viewModel.displayUsuariosInRecyclerView()
        Log.i("EditarUsuarioFragment", "onDestroy")
    }

    private fun canIEditTheUserValidator(bundle: Usuario?) {
        val email = bundle!!.usuario
        val passwordRespaldo = bundle.password
        val fotoPerfil = bundle.fotoPerfil

        val nombre = _binding!!.editTextEditarUsuarioNombre.text.toString()
        val apellidos = _binding!!.editTextEditarUsuarioApellidos.text.toString()
        val telefono = _binding!!.editTextEditarUsuarioTelefono.text.toString()
        val password = _binding!!.editTextEditarUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextEditarUsuarioConfirmarPassword.text.toString()
        val rol = _binding!!.editTextEditarUsuarioRol.text.toString()
        val deshabilitada = false
        val sesionActiva = false

        val usuario = Usuario(bundle.id, fotoPerfil, nombre, apellidos, telefono, email, password,deshabilitada, sesionActiva, rol)

        if (nombre.isEmpty() || apellidos.isEmpty() || telefono.isEmpty() ||
            rol.isEmpty() || password.isEmpty() || password2.isEmpty()
        ) {
            Snackbar.make(
                _binding!!.root,
                "Debes completar todos los campos antes de crear una cuenta.",
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
            Log.i("asd","$usuario")
            _viewModel.editarUsuarioEnFirestore(usuario)
            if(password != passwordRespaldo){
                cambiarClaveEnFirebaseAuth(email,password)
            }
            Snackbar.make(
                _binding!!.root,
                "La cuenta ha sido editada con éxito.",
                Snackbar.LENGTH_SHORT
            ).show()
        }catch(e:Exception){
            Snackbar.make(
                _binding!!.root,
                "${e.message}",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun cambiarClaveEnFirebaseAuth(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result
                    if (signInMethods!!.signInMethods!!.isNotEmpty()) {
                        val user = auth.currentUser
                        if (user != null) {
                            user.updatePassword(password)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        // Password change was successful
                                    } else {
                                        // Password change failed
                                        Snackbar.make(
                                            _binding!!.root,
                                            "El cambio de clave fallo!",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }
                        } else {
                            Snackbar.make(
                                _binding!!.root,
                                "El usuario no existe}",
                                Snackbar.LENGTH_LONG
                            ).show()
                            // User not found
                        }
                    } else {
                        Snackbar.make(
                            _binding!!.root,
                            "No sign-in methods for this email address",
                            Snackbar.LENGTH_LONG
                        ).show()
                        // No sign-in methods for this email address
                    }
                } else {
                    Snackbar.make(
                        _binding!!.root,
                        "Fetching sign-in methods failed",
                        Snackbar.LENGTH_LONG
                    ).show()
                    // Fetching sign-in methods failed
                }
            }

    }

    private fun cargarPlanillaConLosDatosDelUsuarioClickeado(bundle: Usuario){
        _binding!!.textviewEditarUsuarioMail.text = bundle.usuario
        _binding!!.editTextEditarUsuarioNombre.setText(bundle.nombre)
        _binding!!.editTextEditarUsuarioApellidos.setText(bundle.apellidos)
        _binding!!.editTextEditarUsuarioTelefono.setText(bundle.telefono)
        _binding!!.editTextEditarUsuarioPassword.setText(bundle.password)
        _binding!!.editTextEditarUsuarioConfirmarPassword.setText(bundle.password)
        val roles = listOf("Administrador","Conductor","Peoneta","Secretaria", "Supervisor Volantero", "Volantero" )
        val adapter = ArrayAdapter(requireActivity(), R.layout.simple_spinner_dropdown_item,roles)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        _binding!!.editTextEditarUsuarioRol.setText(bundle.rol)
        _binding!!.editTextEditarUsuarioRol.setAdapter(adapter)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                startActivityForResult(takePictureIntent, Constants.REQUEST_TAKE_PHOTO)
            }
        }
    }
}