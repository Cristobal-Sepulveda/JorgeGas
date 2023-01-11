package com.example.conductor.ui.datausuario

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentAdministrarCuentasBinding
import com.example.conductor.databinding.FragmentDataUsuarioBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class DataUsuarioFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDataUsuarioBinding? = null
    private val _viewModel: DataUsuarioViewModel by inject()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataUsuarioBinding.inflate(inflater, container, false)
        firebaseAuth = FirebaseAuth.getInstance()

        object {
            val TAG = "ModalBottomSheet"
        }

        _binding!!.buttonDataUsuarioVolver.setOnClickListener {
            this.dismiss()
        }

        _binding!!.buttonDataUsuarioConfirmar.setOnClickListener {
            val nombre = _binding!!.editTextDataUsuarioPassword.text.toString()
            val aPaterno = _binding!!.editTextDataUsuarioPassword.text.toString()
            val aMaterno = _binding!!.editTextDataUsuarioPassword.text.toString()
            val email = _binding!!.editTextDataUsuarioUsuario.text.toString()
            val password = _binding!!.editTextDataUsuarioPassword.text.toString()
            val password2 = _binding!!.editTextDataUsuarioConfirmarPassword.text.toString()
            it.let{ requireActivity()}

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    val usuario = Usuario(it.result.user!!.uid, nombre, aPaterno, aMaterno,email,password)
                    _viewModel.ingresarUsuarioAFirestore(usuario)
                    Snackbar.make(dialog?.window!!.decorView,"La cuenta ha sido creada con exito",Snackbar.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    Log.i("asd", it.message.toString())
                    Snackbar.make(_binding!!.root,"$it.message",Snackbar.LENGTH_LONG).show()
                }
        }
        return _binding!!.root
    }

}