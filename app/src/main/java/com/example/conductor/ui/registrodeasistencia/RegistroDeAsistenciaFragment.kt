package com.example.conductor.ui.registrodeasistencia

import android.R
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.conductor.adapter.AsistenciaAdapter
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentRegistroDeAsistenciaBinding
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.example.conductor.ui.base.BaseFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RegistroDeAsistenciaFragment: BaseFragment() {

    private var _binding: FragmentRegistroDeAsistenciaBinding? = null
    override val _viewModel: RegistroDeAsistenciaViewModel by inject()
    private var datePickerDialog: DatePickerDialog? = null
    private var desde: String? = null
    private var hasta: String? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        _binding = FragmentRegistroDeAsistenciaBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        _binding!!.viewModel = _viewModel

        val recyclerViewAdapter = AsistenciaAdapter(AsistenciaAdapter.OnClickListener { _ -> })

        val today = Calendar.getInstance()

        _binding!!.recyclerViewRegistroDeAsistenciaListadoDeAsistencia.adapter = recyclerViewAdapter



        _viewModel.registroDeAsistencia.observe(viewLifecycleOwner){
            it.let {
                recyclerViewAdapter.submitList(it as MutableList<Asistencia>)
            }
        }

        _binding!!.buttonRegistroDeAsistenciaObtenerDesde.setOnClickListener {
            abrirCalendario(today, "desde")
        }

        _binding!!.buttonRegistroDeAsistenciaObtenerHasta.setOnClickListener {
            if(desde == null){
                Toast.makeText(requireActivity(), "Primero debes elegir la fecha: 'desde'.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            abrirCalendario(today, "hasta")
        }

        _binding!!.buttonRegistroDeAsistenciaGenerarReporte.setOnClickListener{
            obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista()
        }

        _binding!!.buttonRegistroDeAsistenciaObtenerExcel.setOnClickListener{
            if(desde == null || hasta == null){
                Toast.makeText(requireActivity(), "Por favor, seleccione un rango de fechas", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            lifecycleScope.launch{
                withContext(Dispatchers.IO){
                    _viewModel.exportarRegistroDeAsistenciaAExcel(requireActivity(), desde!!, hasta!!)
                }
            }
        }

        return _binding!!.root
    }
    private fun abrirCalendario(today: Calendar, aux: String) {
        if(aux == "desde") {
            datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                if (month < 9) {
                    desde = "$dayOfMonth-0${month + 1}-$year"
                    if(dayOfMonth<10){
                        desde = "0$dayOfMonth-0${month + 1}-$year"
                    }
                    _binding!!.buttonRegistroDeAsistenciaObtenerDesde.text = desde
                    return@DatePickerDialog
                } else {
                    desde = "$dayOfMonth-${month + 1}-$year"
                    if(dayOfMonth<10){
                        desde = "0$dayOfMonth-${month + 1}-$year"
                    }
                    _binding!!.buttonRegistroDeAsistenciaObtenerDesde.text = desde
                    return@DatePickerDialog
                }
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        }else{
            datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                if (month < 9) {
                    hasta = "$dayOfMonth-0${month + 1}-$year"
                    if(dayOfMonth<10){
                        hasta = "0$dayOfMonth-0${month + 1}-$year"
                    }
                    val desdeDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(desde)
                    val hastaDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(hasta)
                    if (hastaDate.before(desdeDate) || desdeDate == hastaDate) {
                        Toast.makeText(requireActivity(), "La fecha 'hasta' no puede ser menor o igual a la fecha 'desde'", Toast.LENGTH_LONG).show()
                        hasta = null
                        return@DatePickerDialog
                    }
                    _binding!!.buttonRegistroDeAsistenciaObtenerHasta.text = hasta
                    return@DatePickerDialog
                } else {
                    hasta = "$dayOfMonth-${month + 1}-$year"
                    if(dayOfMonth<10){
                        hasta = "0$dayOfMonth-${month + 1}-$year"
                    }
                    val desdeDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(desde)
                    val hastaDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(hasta)
                    if (hastaDate.before(desdeDate) || desdeDate == hastaDate ) {
                        Toast.makeText(requireActivity(), "La fecha 'hasta' no puede ser menor o igual a la fecha 'desde'", Toast.LENGTH_LONG).show()
                        hasta = null
                        return@DatePickerDialog
                    }
                    _binding!!.buttonRegistroDeAsistenciaObtenerHasta.text = hasta
                    return@DatePickerDialog
                }
            }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        }
        datePickerDialog?.show()
    }

    private fun obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista(){
        if(desde == null || hasta == null){
            Toast.makeText(requireActivity(), "Por favor, seleccione un rango de fechas", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                _viewModel.obtenerExcelDelRegistroDeAsistenciaDesdeElBackendYParcearloALista(
                        requireActivity(), desde!!, hasta!!)
            }
        }
    }
}