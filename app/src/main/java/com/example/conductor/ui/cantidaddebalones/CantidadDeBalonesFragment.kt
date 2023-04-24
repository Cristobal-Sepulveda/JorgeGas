package com.example.conductor.ui.cantidaddebalones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.conductor.R
import com.example.conductor.databinding.FragmentCantidadDeBalonesBinding
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class CantidadDeBalonesFragment: BaseFragment() {

    private var _binding: FragmentCantidadDeBalonesBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentCantidadDeBalonesBinding.inflate(inflater, container, false)

        _binding!!.textViewCantidadDeBalonesVolver.setOnClickListener{
            findNavController().navigate(
                CantidadDeBalonesFragmentDirections.actionNavigationCantidadDeBalonesToNavigationVistaGeneral()
            )
        }

        _binding!!.buttonCantidadDeBalonesConfirmar.setOnClickListener{
            var aux = false
            _viewModel.cantidadDeBalones.forEach{
               if(it.value > 0) aux = true
            }
            if(!aux){
                Snackbar.make(_binding!!.root, R.string.debes_a_lo_menos_pedir_un_balon, Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            findNavController().navigate(
                CantidadDeBalonesFragmentDirections.actionNavigationCantidadDeBalonesToNavigationFormularioNuevoPedido()
            )
        }

        val cardCincoKilosBinding = _binding!!.includeCantidadDeBalonesImagenBalonCincoKilos
        val cardOnceKilosBinding = _binding!!.includeCantidadDeBalonesImagenBalonOnceKilos
        val cardQuinceKilosBinding = _binding!!.includeCantidadDeBalonesImagenBalonQuinceKilos
        val cardCuarentaYCincoKilosBinding = _binding!!.includeCantidadDeBalonesImagenBalonCuarentaYCincoKilos

        cardCincoKilosBinding.imageViewCardPedidoBalonOnceKilosRestar.setOnClickListener{
            val valorActual = cardCincoKilosBinding.editTextCardPedidoBalonCincoKilosCantidad.text.toString().toInt()
            if (valorActual > 0){
                val nuevoValor = valorActual - 1
                cardCincoKilosBinding.editTextCardPedidoBalonCincoKilosCantidad.setText(nuevoValor.toString())
                _viewModel.cantidadDeBalones["5kilos"] = nuevoValor
            }
        }
        cardOnceKilosBinding.imageViewCardPedidoBalonOnceKilosRestar.setOnClickListener{
            val valorActual = cardOnceKilosBinding.editTextCardPedidoBalonOnceKilosCantidad.text.toString().toInt()
            if (valorActual > 0){
                val nuevoValor = valorActual - 1
                cardOnceKilosBinding.editTextCardPedidoBalonOnceKilosCantidad.setText(nuevoValor.toString())
                _viewModel.cantidadDeBalones["11kilos"] = nuevoValor
            }
        }
        cardQuinceKilosBinding.imageViewCardPedidoBalonQuinceKilosRestar.setOnClickListener{
            val valorActual = cardQuinceKilosBinding.editTextCardPedidoBalonQuinceKilosCantidad.text.toString().toInt()
            if (valorActual > 0){
                val nuevoValor = valorActual - 1
                cardQuinceKilosBinding.editTextCardPedidoBalonQuinceKilosCantidad.setText(nuevoValor.toString())
                _viewModel.cantidadDeBalones["15kilos"] = nuevoValor
            }
        }
        cardCuarentaYCincoKilosBinding.imageViewCardPedidoBalonCuarentaYCincoKilosRestar.setOnClickListener{
            val valorActual = cardCuarentaYCincoKilosBinding.editTextCardPedidoBalonCuarentaYCincoKilosCantidad.text.toString().toInt()
            if (valorActual > 0){
                val nuevoValor = valorActual - 1
                cardCuarentaYCincoKilosBinding.editTextCardPedidoBalonCuarentaYCincoKilosCantidad.setText(nuevoValor.toString())
                _viewModel.cantidadDeBalones["45kilos"] = nuevoValor
            }
        }

        cardCincoKilosBinding.imageViewCardPedidoBalonOnceKilosSumar.setOnClickListener{
            val valorActual = cardCincoKilosBinding.editTextCardPedidoBalonCincoKilosCantidad.text.toString().toInt()
            val nuevoValor = valorActual + 1
            cardCincoKilosBinding.editTextCardPedidoBalonCincoKilosCantidad.setText(nuevoValor.toString())
            _viewModel.cantidadDeBalones["5kilos"] = nuevoValor
        }
        cardOnceKilosBinding.imageViewCardPedidoBalonOnceKilosSumar.setOnClickListener{
            val valorActual = cardOnceKilosBinding.editTextCardPedidoBalonOnceKilosCantidad.text.toString().toInt()
            val nuevoValor = valorActual + 1
            cardOnceKilosBinding.editTextCardPedidoBalonOnceKilosCantidad.setText(nuevoValor.toString())
            _viewModel.cantidadDeBalones["11kilos"] = nuevoValor
        }
        cardQuinceKilosBinding.imageViewCardPedidoBalonQuinceKilosSumar.setOnClickListener{
            val valorActual = cardQuinceKilosBinding.editTextCardPedidoBalonQuinceKilosCantidad.text.toString().toInt()
            val nuevoValor = valorActual + 1
            cardQuinceKilosBinding.editTextCardPedidoBalonQuinceKilosCantidad.setText(nuevoValor.toString())
            _viewModel.cantidadDeBalones["15kilos"] = nuevoValor
        }
        cardCuarentaYCincoKilosBinding.imageViewCardPedidoBalonCuarentaYCincoKilosSumar.setOnClickListener{
            val valorActual = cardCuarentaYCincoKilosBinding.editTextCardPedidoBalonCuarentaYCincoKilosCantidad.text.toString().toInt()
            val nuevoValor = valorActual + 1
            cardCuarentaYCincoKilosBinding.editTextCardPedidoBalonCuarentaYCincoKilosCantidad.setText(nuevoValor.toString())
            _viewModel.cantidadDeBalones["45kilos"] = nuevoValor
        }

        return _binding!!.root
    }
}