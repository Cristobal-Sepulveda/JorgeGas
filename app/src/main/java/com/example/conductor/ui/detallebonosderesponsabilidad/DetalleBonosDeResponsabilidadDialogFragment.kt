package com.example.conductor.ui.detallebonosderesponsabilidad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.DialogFragmentDetalleBonosDeResponsabilidadBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*

class DetalleBonosDeResponsabilidadDialogFragment: DialogFragment() {

    private var _binding: DialogFragmentDetalleBonosDeResponsabilidadBinding? = null
    private val dataSource: AppDataSource by inject()
    private val _viewModel: DetalleBonosDeResponsabilidadViewModel by inject()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFragmentDetalleBonosDeResponsabilidadBinding.inflate(inflater, container, false)
        _viewModel.montoDelBonoDeResponsabilidad.observe(viewLifecycleOwner){
            _binding!!.textViewDetalleBonosDeResponsabilidadMontoBono1.text = it.toString()
        }

        _viewModel.montoDelBonoDeResponsabilidad.observe(viewLifecycleOwner){

            _binding!!.textViewDetalleBonosDeResponsabilidadMontoBono1.text =
                String.format(Locale("es", "CL"), "\$%,d", it.toInt())
            _binding!!.textViewDetalleBonosDeResponsabilidadMontoBono2.text =
                String.format(Locale("es", "CL"), "\$%,d", (it*0.8).toInt())
            _binding!!.textViewDetalleBonosDeResponsabilidadMontoBono3.text =
                String.format(Locale("es", "CL"), "\$%,d", (it*0.6).toInt())
        }

        _binding!!.imageViewDetalleBonosDeResponsabilidadVolver.setOnClickListener {
            dismiss()
        }

        lifecycleScope.launch(Dispatchers.IO){
            _viewModel.obtenerMontoDelBonoDeResponsabilidad()
        }

        return _binding!!.root
    }
}





///+56959082109