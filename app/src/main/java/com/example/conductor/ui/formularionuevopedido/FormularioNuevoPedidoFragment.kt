package com.example.conductor.ui.formularionuevopedido

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.conductor.databinding.FragmentFormularioNuevoPedidoBinding
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.ui.vistageneral.VistaGeneralViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class FormularioNuevoPedidoFragment: BaseFragment() {

    private var _binding: FragmentFormularioNuevoPedidoBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View{

        _binding = FragmentFormularioNuevoPedidoBinding.inflate(inflater, container, false)

        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                cargarDatosEnFormulario()
            }
        }

        _binding!!.buttonFormularioNuevoPedidoVolver.setOnClickListener{
            findNavController().navigate(FormularioNuevoPedidoFragmentDirections
                .actionNavigationFormularioNuevoPedidoToNavigationCantidadDeBalones()
            )
        }

        _binding!!.buttonFormularioNuevoPedidoConfirmar.setOnClickListener{

        }

        return _binding!!.root
    }

    private suspend fun cargarDatosEnFormulario() {
        _viewModel.obtenerUsuariosDesdeSqlite()
        var detalleDelPedido = ""
        _viewModel.cantidadDeBalones.forEach{
            if(it.value > 0) detalleDelPedido += "${it.key} (${it.value}) - "
        }
        _binding!!.includeFormularioNuevoPedidoDetallePedido.apply{
            textViewDetallePedidoVendedora.text = _viewModel.usuarioDesdeSqlite
            textViewDetallePedidoNombreDelCliente.text = _viewModel.nombreDelCliente
            textViewDetallePedidoDireccion.text = _viewModel.direccionDelCliente
            textViewDetallePedidoDepto.text = _viewModel.deptoDelCliente
            textViewDetallePedidoBlock.text = _viewModel.blockDelCliente
            textViewDetallePedidoTelefono.text = _viewModel.telefonoDelCliente
            textViewDetallePedidoProductos.text = detalleDelPedido
            textViewDetallePedidoComentarios.text = _viewModel.comentarios
        }
    }
}