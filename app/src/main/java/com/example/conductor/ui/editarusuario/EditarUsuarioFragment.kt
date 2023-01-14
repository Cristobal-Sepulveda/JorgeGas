package com.example.conductor.ui.editarusuario

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.conductor.R
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentEditarUsuarioBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class EditarUsuarioFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentEditarUsuarioBinding? = null
    private val _viewModel: AdministrarCuentasViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditarUsuarioBinding.inflate(inflater, container, false)
        val bundle = arguments?.getParcelable<Usuario>("key")
        cargarPlanillaConLosDatosDelUsuarioClickeado(bundle!!)

        /****************************** clickListeners ********************************************/
        _binding!!.buttonEditarUsuarioVolver.setOnClickListener {
            this.dismiss()
        }

        _binding!!.buttonEditarUsuarioConfirmar.setOnClickListener {

            canIEditTheUserValidator(bundle)
        }

        _binding!!.buttonEditarUsuarioBorrar.setOnClickListener {
            sendAlert(bundle)
        }

        /*****************************************************************************************/

        return _binding!!.root
    }
    override fun onDestroy() {
        super.onDestroy()
        _viewModel.cleanUsuarioDetails()
        _viewModel.removeUsuariosInRecyclerView()
        _viewModel.displayUsuariosInRecyclerView()
        Log.i("EditarUsuarioFragment", "onDestroy")
    }

    private  fun sendAlert(bundle:Usuario){
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.perm_request_rationale_title)
            .setMessage(R.string.borrar_cuenta)
            .setPositiveButton(R.string.request_perm_again) { _, _ ->
                _viewModel.eliminarUsuarioDeFirebase(bundle)
                dialog?.window?.let {
                    Snackbar.make(
                        it.decorView,
                        "La cuenta ha sido borrada con éxito.",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton(R.string.dismiss){ _, _ ->

            }
            .create()
            .show()
    }

    private fun canIEditTheUserValidator(bundle: Usuario?) {
        val nombre = _binding!!.editTextEditarUsuarioNombre.text.toString()
        val aPaterno = _binding!!.editTextEditarUsuarioAPaterno.text.toString()
        val aMaterno = _binding!!.editTextEditarUsuarioAMaterno.text.toString()
        val email = bundle!!.usuario
        val password = _binding!!.editTextEditarUsuarioPassword.text.toString()
        val password2 = _binding!!.editTextEditarUsuarioConfirmarPassword.text.toString()
        val deshabilitada = false
        val usuario = Usuario(bundle.id, nombre, aPaterno, aMaterno, email, password,deshabilitada)

        if (nombre.isEmpty() || aPaterno.isEmpty() ||
            aMaterno.isEmpty() || password.isEmpty() || password2.isEmpty()
        ) {
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "Debes completar todos los campos antes de crear una cuenta.",
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
            Log.i("asd","$usuario")
            _viewModel.editarUsuarioEnFirestore(usuario)

            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "La cuenta ha sido editada con éxito.",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
        }catch(e:Exception){
            dialog?.window?.let {
                Snackbar.make(
                    it.decorView,
                    "${e.message}",
                    Snackbar.LENGTH_SHORT
                ).show()
            };
        }
    }

    private fun cargarPlanillaConLosDatosDelUsuarioClickeado(bundle: Usuario){
        _binding!!.textviewEditarUsuarioMail.text = bundle.usuario
        _binding!!.editTextEditarUsuarioNombre.setText(bundle.nombre)
        _binding!!.editTextEditarUsuarioAPaterno.setText(bundle.apellidoPaterno)
        _binding!!.editTextEditarUsuarioAMaterno.setText(bundle.apellidoMaterno)
        //_binding!!.editTextEditarUsuarioUsuario.setText(bundle.usuario)
        _binding!!.editTextEditarUsuarioPassword.setText(bundle.password)
        _binding!!.editTextEditarUsuarioConfirmarPassword.setText(bundle.password)
    }
}