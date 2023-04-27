package com.example.conductor.ui.registrodeasistencia

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.AsistenciaAdapter
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.databinding.FragmentRegistroDeAsistenciaBinding
import com.example.conductor.ui.base.BaseFragment
import com.example.conductor.utils.cerrarTeclado
import com.example.conductor.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class RegistroDeAsistenciaFragment: BaseFragment() {

    private var _binding: FragmentRegistroDeAsistenciaBinding? = null
    override val _viewModel: RegistroDeAsistenciaViewModel by inject()
    private var mes: String? = null
    private var anio: String? = null
    private var volanteroConPosibleBono: String? = null
    private lateinit var recyclerViewAdapter: AsistenciaAdapter
    private var hashMapVolanterosQueAsistieronEnElMesSeleccionado = HashMap<String, String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentRegistroDeAsistenciaBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        recyclerViewAdapter = AsistenciaAdapter(AsistenciaAdapter.OnClickListener { _ -> })
        _binding!!.recyclerViewRegistroDeAsistenciaListadoDeAsistencia.adapter = recyclerViewAdapter

        _viewModel.registroDeAsistencia.observe(viewLifecycleOwner){
            it.let {
                recyclerViewAdapter.submitList(it as MutableList<Asistencia>)
            }

            _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirVolanteroBono.apply{
                visibility = View.VISIBLE
                val listadoDeVolanteros = mutableListOf<String>()
                it.forEach{ asistencia ->
                    listadoDeVolanteros.add(asistencia.nombreCompleto)
                    hashMapVolanterosQueAsistieronEnElMesSeleccionado[asistencia.nombreCompleto] = asistencia.idUsuario
                }

                val listadoDeVolanterosAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    listadoDeVolanteros
                )
                setAdapter(listadoDeVolanterosAdapter)
                setOnClickListener {
                    showDropDown()
                }
                setOnItemClickListener{ parent, view, position, id ->
                    volanteroConPosibleBono = listadoDeVolanteros[position]
                }
            }
            _binding!!.autoCompleteTextViewRegistroDeAsistenciaIngresarMontoBono.visibility = View.VISIBLE
            _binding!!.buttonRegistroDeAsistenciaAgregarBono.visibility = View.VISIBLE
        }

        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirMes.apply{
            val listaDeMeses = mutableListOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio",
                "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
            val listaDeMesesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listaDeMeses)
            setAdapter(listaDeMesesAdapter)
            setOnClickListener {
                showDropDown()
            }
            setOnItemClickListener{ parent, view, position, id ->
                Log.i("hola", listaDeMeses[position])
                mes = listaDeMeses[position]
            }
        }

        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirAnio.apply{
            val listaDeAnios = mutableListOf("2023", "2024")
            val listaDeAniosAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listaDeAnios)
            setAdapter(listaDeAniosAdapter)
            setOnClickListener {
                showDropDown()
            }
            setOnItemClickListener{ parent, view, position, id ->
                Log.i("hola", listaDeAnios[position])
                anio = listaDeAnios[position]
            }
        }

        _binding!!.buttonRegistroDeAsistenciaGenerarReporte.setOnClickListener{
            if(mes == null || anio == null){
                Toast.makeText(requireActivity(), "Por favor, seleccione mes y año", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.obtenerRegistroDeAsistenciaYMostrarloComoExcel(
                        requireActivity(), mes!!, anio!!)
                }
            }
        }

        _binding!!.imageViewRegistroDeAsistenciaBotonExcel.setOnClickListener{
            if(mes == null || anio == null){
                Toast.makeText(requireActivity(), "Por favor, seleccione mes y año", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.exportarRegistroDeAsistenciaAExcel(requireActivity(), mes!!, anio!!)
                }
            }
        }

        _binding!!.buttonRegistroDeAsistenciaAgregarBono.setOnClickListener{
            requireActivity().cerrarTeclado(it)
            val volantero = _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirVolanteroBono.text.toString()
            if(volantero.isEmpty()) return@setOnClickListener showToast("Por favor, seleccione un volantero")
            val bono = _binding!!.autoCompleteTextViewRegistroDeAsistenciaIngresarMontoBono.text.toString()
            if(bono.isEmpty() || bono == "0") return@setOnClickListener showToast("El bono debe ser mayor a 0")
            val volanteroElegido = _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirVolanteroBono.text.toString()
            val mes = _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirMes.text.toString()
            val anio = _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirAnio.text.toString()
            lifecycleScope.launch(Dispatchers.IO){
                _viewModel.agregarBonoPersonalAlVolantero(bono, hashMapVolanterosQueAsistieronEnElMesSeleccionado[volanteroElegido]!!,mes,anio)
            }
        }

        return _binding!!.root
    }

    override fun onResume() {
        super.onResume()
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirVolanteroBono.visibility = View.GONE
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaIngresarMontoBono.visibility = View.GONE
        _binding!!.buttonRegistroDeAsistenciaAgregarBono.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirAnio.setText("")
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirMes.setText("")
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaElegirVolanteroBono.visibility = View.GONE
        _binding!!.autoCompleteTextViewRegistroDeAsistenciaIngresarMontoBono.visibility = View.GONE
        _binding!!.buttonRegistroDeAsistenciaAgregarBono.visibility = View.GONE
        _viewModel.vaciarRecyclerView()
        _binding!!.recyclerViewRegistroDeAsistenciaListadoDeAsistencia.adapter = null
        recyclerViewAdapter.submitList(null)
        mes = null
        anio = null
        volanteroConPosibleBono = null
    }
}