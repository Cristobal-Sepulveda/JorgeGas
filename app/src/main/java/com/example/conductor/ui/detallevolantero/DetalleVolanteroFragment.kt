package com.example.conductor.ui.detallevolantero

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.FragmentDetalleVolanteroBinding
import com.example.conductor.utils.Constants
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.collections.ArrayList

class DetalleVolanteroFragment: BaseFragment(), OnMapReadyCallback {

    override val _viewModel: DetalleVolanteroViewModel by inject()
    private var _binding: FragmentDetalleVolanteroBinding? = null
    private lateinit var map: GoogleMap
    private var datePickerDialog: DatePickerDialog? = null
    private var selectedDate: String?= null
    private var registroDelVolantero: Any? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetalleVolanteroBinding.inflate(inflater, container, false)
        val bundle = DetalleVolanteroFragmentArgs.fromBundle(requireArguments()).usuarioDetails
        val today = Calendar.getInstance()
        cargarDatosDelVolantero(bundle)


        lifecycleScope.launch{
            withContext(Dispatchers.IO){
                registroDelVolantero = _viewModel.obtenerRegistroDelVolantero(bundle.id)
            }
        }

        _binding!!.sliderDetalleVolanteroTrayecto.addOnChangeListener{_,value,_ ->
            val minutes = (value * 10).toInt() % 60
            val hours = 10 + (value * 10).toInt() / 60
            val time = String.format("%02.0f:%02d", hours.toFloat(), minutes)
            Log.d("Slider", "Selected time: $time")
            val registroDelVolanteroParseado = registroDelVolantero as DocumentSnapshot
            val registroJornada = registroDelVolanteroParseado.data!!["registroJornada"] as ArrayList<Map<String,Map<*,*>>>
            println(selectedDate)
            for(registro in registroJornada){
                if(registro["fecha"].toString() == selectedDate){
                    val registroLatLngsDelDia = registro["registroLatLngs"]
                    val horasConRegistro = registroLatLngsDelDia!!["horasConRegistro"] as ArrayList<String>
                    val geopointsRegistradosDelDia = registroLatLngsDelDia["geopoints"] as ArrayList<GeoPoint>
                    for(horaRegistrada in horasConRegistro){
                        if(hours.toFloat()>=horaRegistrada.substring(0,2).toFloat()){
                            if(minutes.toFloat() >= horaRegistrada.substring(3,5).toFloat()){
                                var i = horasConRegistro.indexOf(horaRegistrada)
                                while(i<horasConRegistro.size){
                                    if(horasConRegistro[i].substring(0,2).toFloat() == hours.toFloat()){
                                        if(horasConRegistro[i].substring(3,5).toFloat() > minutes.toFloat()){
                                            break
                                        }
                                    }
                                    val latitud = geopointsRegistradosDelDia[i].latitude
                                    val longitud = geopointsRegistradosDelDia[i].longitude
                                    val latLng = LatLng(latitud,longitud)
                                    map.addMarker(latLng.let { MarkerOptions().position(it) })
                                    i++
                                }
                                break
                            }
                        }
                    }
                    return@addOnChangeListener
                }
            }
            Toast.makeText(requireActivity(), "No hay registro con esa fecha.", Toast.LENGTH_SHORT).show()
            _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = false
        }
        _binding!!.sliderDetalleVolanteroTrayecto.setLabelFormatter { value ->
            val minutes = (value * 10).toInt() % 60
            val hours = 10 + (value * 10).toInt() / 60
            String.format("%02d:%02d", hours, minutes)
        }

        _binding!!.imageViewDetalleVolanteroCalendario.setOnClickListener{
            configurarCalendario(today)
        }

        _binding!!.textViewDetalleVolanteroRecorrido.setOnClickListener {
            if(registroDelVolantero != null && registroDelVolantero != "Error"){
                val aux = registroDelVolantero as DocumentSnapshot
                println(aux.data!!["registroJornada"])
            }
        }

        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (childFragmentManager.findFragmentById(R.id.fragmentContainerView_detalleVolantero_googleMaps)
                as? SupportMapFragment)?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
    }

    private fun configurarCalendario(today: Calendar) {
        datePickerDialog = DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
            if(month<9){
                selectedDate = "$year-0${month+1}-$dayOfMonth"
                _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = true
            }else{
                selectedDate = "$year-${month+1}-$dayOfMonth"
                _binding!!.sliderDetalleVolanteroTrayecto.isEnabled = true
            }

        }, today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        datePickerDialog?.show()
    }

    private fun cargarDatosDelVolantero(bundle: Usuario) {
        val fotoPerfil = bundle.fotoPerfil
        if(fotoPerfil.last().toString() == "=" || (fotoPerfil.first().toString() == "/" && fotoPerfil[1].toString() == "9")){
            val decodedString  = Base64.decode(fotoPerfil, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            _binding!!.imageViewDetalleVolanteroFotoPerfil.setImageBitmap(decodedByte)
        }else{
            val aux2= fotoPerfil.indexOf("=")+1
            val aux3 = fotoPerfil.substring(0, aux2)
            val decodedString  = Base64.decode(aux3, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            _binding!!.imageViewDetalleVolanteroFotoPerfil.setImageBitmap(decodedByte)
        }
    }

    private fun startingPermissionCheck(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(isPermissionGranted){
            try{
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                    LatLng(Constants.defaultLocation.latitude, Constants.defaultLocation.longitude)
                    , Constants.cameraDefaultZoom.toFloat())
                )
            }catch(e: SecurityException){
                _binding!!.fragmentContainerViewDetalleVolanteroGoogleMaps.isGone = true
                _binding!!.imageviewDetalleVolanteroMapaSinPermisos.isGone = false

            }
        }else {
            _binding!!.fragmentContainerViewDetalleVolanteroGoogleMaps.isGone = true
            _binding!!.imageviewDetalleVolanteroMapaSinPermisos.isGone = false
        }
    }
}