package com.example.conductor.ui.filtroregistrovolanteros

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.databinding.FragmentFiltroRegistroVolanterosBinding
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.registrovolanteros.RegistroVolanterosViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
class FiltroRegistroVolanterosFragment: BottomSheetDialogFragment() {
    private val _viewModel: RegistroVolanterosViewModel by inject()
    private var _binding: FragmentFiltroRegistroVolanterosBinding? = null
    private var volanterosSeleccionadosEnElFiltro = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFiltroRegistroVolanterosBinding.inflate(inflater, container, false)
        _binding!!.viewModel = _viewModel
        _binding!!.lifecycleOwner = this

        dibujandoFiltros()

        _binding!!.textViewFiltroRegistroVolanterosVolver.setOnClickListener{
            dismiss()
        }

        _binding!!.buttonFiltroRegistroVolanterosConfirmar.setOnClickListener{
            val selectedVolanteros = volanterosSeleccionadosEnElFiltro
            if(selectedVolanteros.isEmpty()){
                Snackbar.make(_binding!!.root, R.string.filtro_sin_eleccion, Snackbar.LENGTH_LONG).show()
            }else{
                _viewModel.setearSelectedVolanteros(selectedVolanteros)
                dismiss()
            }
        }

        return _binding!!.root
    }

    private fun dibujandoFiltros() {
        val volanterosTrabajandoEseDia = _viewModel.selectedVolanteros.value
        if (volanterosTrabajandoEseDia!!.isNotEmpty()) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.LOADING)
                    val listaObtenida =
                        _viewModel.obtenerRegistroDiariosRoomDesdeFirestore(requireContext()) as MutableList<*>
                    if (listaObtenida.isNotEmpty()) {
                        listaObtenida.forEach { documento ->
                            withContext(Dispatchers.Main) {
                                val flexBoxLayout =
                                    _binding!!.flexBoxLayoutFiltroRegistroVolanterosContenedorDeVolanteros
                                val docSnapshot = documento as DocumentSnapshot
                                val id = docSnapshot.id
                                if (volanterosTrabajandoEseDia.contains(id)) {
                                    val chip = Chip(context)
                                    chip.text = docSnapshot.get("nombreCompleto") as String
                                    chip.isCheckable = true
                                    chip.setChipBackgroundColorResource(R.color.white)
                                    chip.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                                    chip.chipStrokeWidth = 1f
                                    chip.chipStrokeColor = ContextCompat.getColorStateList(requireActivity(), R.color.black)
                                    chip.setOnCheckedChangeListener { buttonView, isChecked ->
                                        if (isChecked) {
                                            chip.setChipBackgroundColorResource(R.color.orange)
                                            chip.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white))
                                            chip.chipStrokeWidth = 0f
                                            volanterosSeleccionadosEnElFiltro.add(id)
                                        } else {
                                            chip.setChipBackgroundColorResource(R.color.white)
                                            chip.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black))
                                            chip.chipStrokeWidth = 1f
                                            chip.chipStrokeColor = ContextCompat.getColorStateList(requireActivity(), R.color.black)
                                            volanterosSeleccionadosEnElFiltro.remove(id)
                                        }
                                    }
                                    flexBoxLayout.addView(chip)
                                }
                            }
                        }
                        _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.DONE)
                    } else {
                        _viewModel.cambiarStatusCloudRequestStatus(CloudRequestStatus.ERROR)
                    }
                }
            }
        }
    }
}
