package com.example.conductor.ui.crearusuario

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.ui.estadoactual.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentCrearUsuarioBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.example.conductor.utils.Constants.REQUEST_TAKE_PHOTO
import com.example.conductor.utils.NavigationCommand
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream

class CrearUsuarioFragment : BaseFragment() {

    private var _binding: FragmentCrearUsuarioBinding? = null
    override val _viewModel: AdministrarCuentasViewModel by inject()
    private lateinit var firebaseAuth: FirebaseAuth
    private var imageBitmap: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearUsuarioBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        val roles = listOf("Administrador","Conductor","Peoneta","Secretaria", "Supervisor Volantero", "Volantero" )
        val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item,roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        _binding!!.editTextDataUsuarioRol.setAdapter(adapter)


        _binding!!.buttonDataUsuarioVolver.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(
                    CrearUsuarioFragmentDirections
                        .actionNavigationDataUsuarioToNavigationAdministrarCuentas())
        }

        _binding!!.buttonDataUsuarioConfirmar.setOnClickListener {
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    canICreateANewAccountValidator()
                }
            }
        }
        _binding!!.circleImageViewCrearUsuarioIconoTomarFoto.setOnClickListener{
            dispatchTakePictureIntent()
        }

        return _binding!!.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.removeUsuariosInRecyclerView()
        _viewModel.displayUsuariosInRecyclerView()
        Log.i("CrearUsuarioFragment", "onDestroy")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            imageBitmap = data?.extras?.get("data") as Bitmap
            // Do something with the imageBitmap, such as saving it to a file or displaying it in an ImageView
        }else{
            Toast.makeText(requireActivity(),"Debes de tomar una foto para poder guardar un usuario", Toast.LENGTH_LONG).show()
        }
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireContext().packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
            }
        }
    }
    private fun parseandoImagenParaSubirlaAFirestore(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        return Base64.encodeToString(data, Base64.NO_PADDING)
    }

    private suspend fun canICreateANewAccountValidator() {
        val nombre = _binding!!.editTextDataUsuarioNombre.text.toString()
        val apellidos = _binding!!.editTextDataUsuarioApellidos.text.toString()
        val telefono = _binding!!.editTextDataUsuarioTelefono.text.toString()
        val email = _binding!!.editTextDataUsuarioUsuario.text.toString()
        val password = _binding!!.editTextDataUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextDataUsuarioConfirmarPassword.text.toString()
        val rol = _binding!!.editTextDataUsuarioRol.text.toString();

        if (validarInputsYFoto(nombre, apellidos, email, password, password2, rol)) return

        val actualUserEmail = firebaseAuth.currentUser!!.email
        var actualUserPassword = ""
        _viewModel.todosLosUsuarios.forEach {
            if(it.usuario == firebaseAuth.currentUser!!.email){
                actualUserPassword = it.password
            }
        }

        val creandoUsuarioEnFirebaseAuth = firebaseAuth.createUserWithEmailAndPassword(email, password)

        creandoUsuarioEnFirebaseAuth.addOnSuccessListener{
            val foto = parseandoImagenParaSubirlaAFirestore(imageBitmap!!)
            val usuario = Usuario(
                it.user!!.uid,
                foto,
                nombre,
                apellidos,
                telefono,
                email,
                password,
                deshabilitada = false,
                sesionActiva = false,
                rol = rol
            )

            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    val creandoUsuarioEnFirestore = _viewModel.ingresarUsuarioAFirestore(usuario)
                    if(creandoUsuarioEnFirestore){
                        firebaseAuth.signOut()
                        firebaseAuth.signInWithEmailAndPassword(actualUserEmail!!, actualUserPassword).await()
                        Snackbar.make(
                            _binding!!.root,
                            "La cuenta ha sido creada con exito.",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }else{
                        Snackbar.make(_binding!!.root,
                            getString(R.string.error_al_crear_cuenta_firestore),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }

        }

        creandoUsuarioEnFirebaseAuth.addOnFailureListener {
            Snackbar.make(_binding!!.root,
                getString(R.string.error_al_crear_cuenta_fireauth),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun validarInputsYFoto(
        nombre: String,
        apellidos: String,
        email: String,
        password: String,
        password2: String,
        rol: String
    ): Boolean {
        if (imageBitmap == null) {
            Snackbar.make(
                _binding!!.root,
                "Debes de tomar una foto para poder guardar un usuario",
                Snackbar.LENGTH_SHORT
            ).show()
            return true
        }
        if (nombre.isEmpty() || apellidos.isEmpty() ||
            email.isEmpty() || password.isEmpty() ||
            password2.isEmpty() || rol.isEmpty()
        ) {
            Snackbar.make(
                _binding!!.root,
                "Debes completar todos los campos antes de crear una cuenta.",
                Snackbar.LENGTH_SHORT
            ).show()
            return true
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Snackbar.make(
                _binding!!.root,
                "El email que ingresaste no es valido",
                Snackbar.LENGTH_SHORT
            ).show()
            return true
        }
        if (password != password2) {
            Snackbar.make(
                _binding!!.root,
                "Las contraseñas no coincide.",
                Snackbar.LENGTH_SHORT
            ).show()
            return true
        }
        if (password.length < 6) {
            Snackbar.make(
                _binding!!.root,
                "La contraseña debe tener a lo menos 6 caracteres.",
                Snackbar.LENGTH_SHORT
            ).show()
            return true
        }
        if (apellidos.split(" ").size != 2) {
            Snackbar.make(
                _binding!!.root,
                "Ingrese los 2 apellidos separados por un espacio",
                Snackbar.LENGTH_SHORT
            ).show()

            return true
        }
        return false
    }
}