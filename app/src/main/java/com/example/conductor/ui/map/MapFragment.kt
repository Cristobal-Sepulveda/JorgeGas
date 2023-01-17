package com.example.conductor.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.example.conductor.utils.Constants.cameraDefaultZoom
import com.example.conductor.utils.Constants.defaultLocation
import com.example.conductor.utils.polygonsColor
import com.example.conductor.utils.polygonsList
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.koin.android.ext.android.inject


class MapFragment : BaseFragment(), OnMapReadyCallback{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private lateinit var map: GoogleMap


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        _binding!!.lifecycleOwner = this
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        _binding!!.buttonMapaReiniciarActividad.setOnClickListener {
            val intent = Intent(requireActivity(), requireActivity().javaClass)
            startActivity(intent)
            requireActivity().finish()
        }

        return _binding!!.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        startingPermissionCheck()
        markingPolygons()
    }

    private fun startingPermissionCheck(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(isPermissionGranted){
            try{
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    LatLng(defaultLocation.latitude, defaultLocation.longitude)
                    , cameraDefaultZoom.toFloat())
                )
                map.uiSettings.isMyLocationButtonEnabled = true
            }catch(e: SecurityException){
                _binding!!.map.isGone = true
                _binding!!.imageviewMapaSinPermisos.isGone = false
                _binding!!.buttonMapaReiniciarActividad.isGone = false

            }
        }else {
            _binding!!.map.isGone = true
            _binding!!.imageviewMapaSinPermisos.isGone = false
            _binding!!.buttonMapaReiniciarActividad.isGone = false
        }
    }

    private fun markingPolygons(){
        for((index,polygon) in polygonsList.withIndex()){
            val polygonOptions = PolygonOptions()
            polygonOptions.addAll(polygon)
            polygonOptions.fillColor(polygonsColor[index])
            polygonOptions.strokeColor(Color.argb(50,255,255,255))
            map.addPolygon(polygonOptions)
        }
    }


}


