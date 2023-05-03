package com.example.conductor.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.conductor.R
import com.example.conductor.databinding.DialogFragmentDetalleBonosDeResponsabilidadBinding
import com.example.conductor.databinding.FragmentVistaGeneralBinding

class DetalleBonosDeResponsabilidad: DialogFragment() {

    private var _binding: DialogFragmentDetalleBonosDeResponsabilidadBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogFragmentDetalleBonosDeResponsabilidadBinding.inflate(inflater, container, false)

        _binding!!.imageViewDetalleBonosDeResponsabilidadVolver.setOnClickListener {
            dismiss()
        }

        return _binding!!.root
    }
}





///+56959082109