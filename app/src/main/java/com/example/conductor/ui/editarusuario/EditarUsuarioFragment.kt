package com.example.conductor.ui.editarusuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentEditarUsuarioBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasFragmentDirections
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.example.conductor.utils.NavigationCommand
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
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
        val bundle = arguments?.getParcelable<Usuario>("key")
        cargarPlanillaConLosDatosDelUsuarioClickeado(bundle!!)

        _binding!!.buttonEditarUsuarioVolver.setOnClickListener {
            _viewModel.navigationCommand.value = NavigationCommand.To(
                AdministrarCuentasFragmentDirections
                    .actionNavigationAdministrarCuentasToNavigationDataUsuario())
        }

        _binding!!.buttonEditarUsuarioConfirmar.setOnClickListener {
            canIEditTheUserValidator(bundle)
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
        val nombre = _binding!!.editTextEditarUsuarioNombre.text.toString()
        val apellidos = _binding!!.editTextEditarUsuarioApellidos.text.toString()
        val telefono = _binding!!.editTextEditarUsuarioTelefono.text.toString()
        val password = _binding!!.editTextEditarUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextEditarUsuarioConfirmarPassword.text.toString()
        val rol = _binding!!.editTextEditarUsuarioRol.text.toString()
        val deshabilitada = false

        val usuario = Usuario(bundle.id, nombre, apellidos, telefono, email, password,deshabilitada, rol)

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

    private fun cargarPlanillaConLosDatosDelUsuarioClickeado(bundle: Usuario){
        _binding!!.textviewEditarUsuarioMail.text = bundle.usuario
        _binding!!.editTextEditarUsuarioNombre.setText(bundle.nombre)
        _binding!!.editTextEditarUsuarioApellidos.setText(bundle.apellidos)
        _binding!!.editTextEditarUsuarioTelefono.setText(bundle.telefono)
        _binding!!.editTextEditarUsuarioPassword.setText(bundle.password)
        _binding!!.editTextEditarUsuarioConfirmarPassword.setText(bundle.password)
        _binding!!.editTextEditarUsuarioRol.setText(bundle.rol)
    }
}