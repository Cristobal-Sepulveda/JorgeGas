package com.example.conductor.ui.map

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import org.koin.android.ext.android.inject


class MapFragment : BaseFragment(), OnMapReadyCallback {

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    // The entry point to the Fused Location Provider.
    private var locationPermissionGranted = false
    private val defaultLocation = LatLng(-33.6256, -70.5841)
    private val cameraDefaultZoom = 13
    private var lastKnownLocation: Location? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()){ isGranted ->
        when{
            isGranted -> {
                Toast.makeText(requireActivity(), "Permiso otorgado", Toast.LENGTH_LONG).show()
                locationPermissionGranted = true
                getDeviceLocation()
            }
            else -> sendAlert()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // Specify the current fragment as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates
        binding.lifecycleOwner = this

        //Adding  the map setup implementation
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        setMapStyle(map)
        markingPolygons()
    }

    private fun markingPolygons(){
        val polygon1 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                LatLng(-33.60628468696749, -70.60392740281233),
                LatLng(-33.602979696865454, -70.57716705402323),
                LatLng(-33.61228664097706, -70.57541457902043),
                LatLng(-33.612434527088205, -70.57163272761613),
                LatLng(-33.61000015167158, -70.55824431743959),
                LatLng(-33.60987993382238, -70.55091858381535),
                LatLng(-33.61141061556795, -70.54301229547104),
                LatLng(-33.607542782775624, -70.53699013779527),
                LatLng(-33.605079616160396, -70.52121587303789),
                LatLng(-33.60732423465206, -70.51262049480744),
                LatLng(-33.61364236723633, -70.50982868779202),
                LatLng(-33.61333661220878, -70.51706807929435),
                LatLng(-33.61629043348729, -70.51895638302005),
                LatLng(-33.61701293550985, -70.52701994970805),
                LatLng(-33.61518541868951, -70.5296737817462),
                LatLng(-33.619852726464885, -70.5402094238831),
                LatLng(-33.618079698877835, -70.54374598499405),
                LatLng(-33.61886811353926, -70.55122087620057),
                LatLng(-33.62086937791679, -70.55735777041649),
                LatLng(-33.6210480599817, -70.56675623084328),
                LatLng(-33.62529612900278, -70.56709387434762),
                LatLng(-33.62491366548973, -70.56974770639785),
                LatLng(-33.6266134904792, -70.57556572278918),
                LatLng(-33.6312028505077, -70.5795975063703),
                LatLng(-33.634049830565, -70.58796728460732),
                LatLng(-33.633367050547506, -70.59543769051807),
                LatLng(-33.61647910680319, -70.59980056503963),
                LatLng(-33.60950986250437, -70.6035771152798),
            ))
        polygon1.fillColor = Color.argb(100,0,255,0)
        polygon1.strokeColor = -0xc771c4

        val polygon2 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                LatLng(-33.60628468696749, -70.60392740281233),
                LatLng(-33.60290490613601, -70.57716343217764),
                LatLng(-33.57923779386923, -70.58195968912496),
                LatLng(-33.58324488565879, -70.61058109615139),
                LatLng(-33.58420553980967, -70.61076317053265),
                LatLng(-33.59161232982887, -70.60797136324557),
                LatLng(-33.597552469857604, -70.60718237428078),
                LatLng(-33.60254328282471, -70.60484880289525),
            ))
        polygon2.fillColor = Color.argb(100,255,0,0)
        polygon2.strokeColor = -0xc771c4
        val polygon3 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                LatLng(-33.580486029707735, -70.58166952524635),
                LatLng(-33.61220889976072, -70.57540457499513),
                LatLng(-33.612510537608145, -70.57143087133171),
                LatLng(-33.6099729662929, -70.55842752212611),
                LatLng(-33.609700700607455, -70.5508908661282),
                LatLng(-33.61126759748528, -70.54300097604228),
                LatLng(-33.60705228081901, -70.53637647492806),
                LatLng(-33.60637173574866, -70.53467704659592),
                LatLng(-33.60188454869104, -70.53255747578064),
                LatLng(-33.59854238997668, -70.53075503131092),
                LatLng(-33.58325835591019, -70.52775830030983),
                LatLng(-33.579925035605164, -70.54338997951928),
                LatLng(-33.57921566270993, -70.55170567568396),
                LatLng(-33.57941332443297, -70.55302361549185),
            ))
        polygon3.fillColor = Color.argb(100,255,255,0)
        polygon3.strokeColor = -0xc771c4

        val polygon4 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                LatLng(-33.58040498150574, -70.58142343164874),
                LatLng(-33.56374296694831, -70.58460973333513),
                LatLng(-33.55918773484535, -70.55859455728834),
                LatLng(-33.55868183472959, -70.55377970603897),
                LatLng(-33.55921561580159, -70.5519451062063),
                LatLng(-33.562102492563426, -70.53918822476938),
                LatLng(-33.561952579366874, -70.53851313458505),
                LatLng(-33.56348619605483, -70.53163849273807),
                LatLng(-33.58309185391012, -70.52785621878306),
                LatLng(-33.57990503020975, -70.54340987039959),
                LatLng(-33.579243599193326, -70.55181822503918),
                LatLng(-33.5803259381889, -70.58148203427359),
            ))
        polygon4.fillColor = Color.argb(100,128,64,0)
        polygon4.strokeColor = -0xc771c4
        val polygon5 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion gabriela con concha y toro
                LatLng(-33.579171423272946, -70.58188502035108),
                //concha y toro con san jose de la estrella
                LatLng(-33.55335048707786, -70.58664124960988),
                //San jose de la estrella con coronel
                LatLng(-33.55406729517656, -70.6127096491245),
                //Coronel con san francisco
                LatLng(-33.56963557662313, -70.60931446593355),
                LatLng(-33.56999843787981, -70.61079776592685),
                LatLng(-33.57010571041135, -70.61279332941437),
                //acceso sur con gabriela
                LatLng(-33.583179601044584, -70.61068494489427)

            ))
        polygon5.fillColor = Color.argb(100,0,150,0)
        polygon5.strokeColor = -0xc771c4
        val polygon6 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion Vicuña mackenna con diego portales
                LatLng(-33.5638557178611, -70.58482039517871),
                //interseccion Vicuña mackenna con trinidad
                LatLng(-33.54717997201674, -70.5876482007977),
                //interseccion Vicuña mackenna con santa amalia
                LatLng(-33.54344268011333, -70.58938489605548),
                //intersección sta. amalia con rucapequén pte
                LatLng(-33.54388368510893, -70.55713604013037),
                LatLng(-33.54479972934695, -70.55402122983402),
                //intersección tobalaba con de las tinajas
                LatLng(-33.54201372671423, -70.55247012712553),
                LatLng(-33.54139383528121, -70.5457393220742),
                //final de las tinajas
                LatLng(-33.53943081643269, -70.54074389953063),
                LatLng(-33.55069731914526, -70.53669053474603),
                LatLng(-33.55220555146744, -70.5280136222116),
                LatLng(-33.56190542461234, -70.52593253387282),
                //intersección diego portales con los aromos
                LatLng(-33.56352260975264, -70.5316484353057),
                //quiebre en diego portales
                LatLng(-33.561945357956624, -70.53880313122615),
                //quiebre en diego portales
                LatLng(-33.56213644327625, -70.53915752704826),
                //quiebre en diego portales
                LatLng(-33.558637924286096, -70.55401304367804),
                //quiebre en diego portales
                LatLng(-33.55918373572842, -70.55855565419975),

            ))
        polygon6.fillColor = Color.argb(75,255,0,0)
        polygon6.strokeColor = -0xc771c4

        val polygon7 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion acceso sur con san francisco
                LatLng(-33.570103636272144, -70.61272156257617),
                LatLng(-33.569999417584924, -70.61067857489567),
                LatLng(-33.56963611196569, -70.60930532373368),
                //intersección coronel con san miguel
                LatLng(-33.561974492673336, -70.61049764563414),
                //intersección coronel con santo tomas
                LatLng(-33.555643935692906, -70.61230047400487),
                //intersección santo tomas con santa rosa
                LatLng(-33.559769826202015, -70.63067254897511),
                //intersección santa rosa con observatorio
                LatLng(-33.565775935324794, -70.63031643354147),
                //quiebre en observatorio
                LatLng(-33.56608813851543, -70.62760851008629),
                //intersección en maria elena con acceso sur
                LatLng(-33.563916883872444, -70.61410295440204),
                ))
        polygon7.fillColor = Color.argb(75,255,200,0)
        polygon7.strokeColor = -0xc771c4

        val polygon8 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion acceso sur con san francisco
                LatLng(-33.555578920270335, -70.61230338361496),
                LatLng(-33.54606178636512, -70.61521817628599),
                LatLng(-33.54341200428776, -70.6153626888217),
                LatLng(-33.54110543965214, -70.61605789280338),
                LatLng(-33.54292221019106, -70.62984094624024),
                LatLng(-33.542380196237005, -70.63416427957998),
                LatLng(-33.5515085457812, -70.63254144200152),
                LatLng(-33.55972060770881, -70.63067174818559),
            ))
        polygon8.fillColor = Color.argb(65,118,118,245)
        polygon8.strokeColor = -0xc771c4

        val polygon9 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion san jose de la estrella con vicuña mackenna
                LatLng(-33.55334639048328, -70.58668756951543),
                LatLng(-33.55400063027376, -70.61273367524656),
                LatLng(-33.54620649119451, -70.61522770028301),
                LatLng(-33.54349245781992, -70.6153485869743),
                LatLng(-33.54111296098554, -70.6160506113006),
                LatLng(-33.54066107997771, -70.61278836088316),
                LatLng(-33.53965695284856, -70.6111957509788),
                LatLng(-33.52511745975552, -70.60153671491878),
                LatLng(-33.522841332889556, -70.5983769305141),
                LatLng(-33.54717062029047, -70.5877080799857),
            ))
        polygon9.fillColor = Color.argb(35,234,39,245)
        polygon9.strokeColor = -0xc771c4

        val polygon10 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //interseccion san jose de la estrella con vicuña mackenna
                LatLng(-33.522841332889556, -70.5983769305141),
                LatLng(-33.54343063835937, -70.58934870240462),
                //orilla derecha
                LatLng(-33.54365371876317, -70.56997158768174),
                LatLng(-33.52854520945, -70.57620544908255),
                LatLng(-33.52576143111465, -70.57732362431807),
                LatLng(-33.520026274285165, -70.58020048453896),
                LatLng(-33.51797542885046, -70.5825770213538),
                LatLng(-33.5082079892582, -70.58991004131317),
                //orilla norte
                LatLng(-33.512965538469395, -70.5907888892642),


            ))
        polygon10.fillColor = Color.argb(85,0,0,245)
        polygon10.strokeColor = -0xc771c4

        val polygon11 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.5082079892582, -70.58991004131317),
                LatLng(-33.51808202277381, -70.58255810156797),
                LatLng(-33.5206723421151, -70.57969193636629),
                LatLng(-33.54363770863618, -70.57002415360533),
                //orilla inferior
                LatLng(-33.543954002786144, -70.55697640669544),
                LatLng(-33.54475957922643, -70.5540869354251),
                LatLng(-33.54201010806329, -70.55223852101992),
                LatLng(-33.54138089423347, -70.54575831388932),
                LatLng(-33.53944493885021, -70.54077906868355),
                LatLng(-33.53779660593888, -70.53952790641958),
                LatLng(-33.535189376701595, -70.53420730886431),
                LatLng(-33.53261425065831, -70.53520966410811),
                LatLng(-33.529136371027576, -70.53889929619936),
                LatLng(-33.52960645824243, -70.50773518291008),
                //orilla derecha
                LatLng(-33.523145291612934, -70.50768560035024),
                LatLng(-33.52290198229604, -70.51860933039683),
                LatLng(-33.50642487316017, -70.51860933072153),
                LatLng(-33.512299982943816, -70.53916428874815),
                LatLng(-33.511313634357705, -70.56380922615074),
                LatLng(-33.51162650240585, -70.56676947372306),
                ))
        polygon11.fillColor = Color.argb(100,0,150,0)
        polygon11.strokeColor = -0xc771c4

        val polygon12 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla inferior
                LatLng(-33.5082079892582, -70.58991004131317),
                LatLng(-33.51162650240585, -70.56676947372306),
                LatLng(-33.511313634357705, -70.56380922615074),
                LatLng(-33.512299982943816, -70.53916428874815),
                LatLng(-33.50642487316017, -70.51860933072153),
                //orilla derecha
                LatLng(-33.50372413518799, -70.51872080380035),
                LatLng(-33.50111825176601, -70.51247065238249),
                LatLng(-33.488816233517035, -70.51431796005124),
                LatLng(-33.480462854673405, -70.51988808659594),
                //orilla superior
                LatLng(-33.469146723114655, -70.57640680850993),
                //orilla izquierda
                LatLng(-33.48910098985536, -70.58064610020865),
                LatLng(-33.50022170780236, -70.5871999360835),
                LatLng(-33.50535541276519, -70.589425964101),

            ))
        polygon12.fillColor = Color.argb(50,175,0,0)
        polygon12.strokeColor = -0xc771c4

        val polygon13 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla inferior
                LatLng(-33.469146723114655, -70.57640680850993),
                LatLng(-33.480462854673405, -70.51988808659594),
                //orilla derecha
                LatLng(-33.47567529089164, -70.51725550602114),
                LatLng(-33.46858371235123, -70.51519318975399),
                LatLng(-33.46235828843926, -70.51618338119772),
                LatLng(-33.46116273607728, -70.51554414653597),
                LatLng(-33.452837529935664, -70.511692491587),
                LatLng(-33.44218599407016, -70.51372673894925),
                LatLng(-33.439914234777014, -70.51451036417559),
                LatLng(-33.42723559502753, -70.52330532570916),
                //orilla superior
                //LatLng(-33.42723559502753, -70.52330532570916),
                LatLng(-33.42710781504093, -70.53869244830808),
                LatLng(-33.42874764381798, -70.53922831823887),
                LatLng(-33.430466791225015, -70.55467784220728),
                LatLng(-33.43167358737499, -70.58484561109304),
                //orilla izquierda
                //LatLng(-33.43167358737499, -70.58484561109304),
                LatLng(-33.43311236836839, -70.58342041448135),
                LatLng(-33.43625801306047, -70.57816158627607),
                LatLng(-33.43812511538362, -70.57343414223266),
                LatLng(-33.452676884801164, -70.57072042793214),
                LatLng(-33.45898317343498, -70.5723253785799),


                ))
        polygon13.fillColor = Color.argb(100,128,64,0)
        polygon13.strokeColor = -0xc771c4

        val polygon14 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla inferior
                LatLng(-33.42723559502753, -70.52330532570916),
                LatLng(-33.42710781504093, -70.53869244830808),
                LatLng(-33.42874764381798, -70.53922831823887),
                LatLng(-33.430466791225015, -70.55467784220728),
                LatLng(-33.43167358737499, -70.58484561109304),
                //orilla izquierda
                LatLng(-33.42611118682187, -70.59092512400446),
                LatLng(-33.418006343560876, -70.60159793311544),
                LatLng(-33.415964559164486, -70.60797086178465),
                LatLng(-33.410199743190475, -70.60660549162844),
                LatLng(-33.40330941222202, -70.60393506729885),
                LatLng(-33.3963432101783, -70.60434334913515),
                LatLng(-33.388907718023376, -70.60003087201329),
                LatLng(-33.36874036027304, -70.58973559456345),
                //orilla superior
                //LatLng(-33.36874036027304, -70.58973559456345),
                LatLng(-33.359762018972646, -70.57020251505907),
                LatLng(-33.34412866754168, -70.56658057850717),
                LatLng(-33.32415030374115, -70.56099696419165),
                LatLng(-33.318267476049655, -70.55049734153913),
                LatLng(-33.32204866482092, -70.51803279338995),
                LatLng(-33.32859193553384, -70.50042216233015),
                LatLng(-33.34212217209153, -70.48481461841493),

                //orilla derecha
                //LatLng(-33.34212217209153, -70.48481461841493),
                LatLng(-33.3636993436981, -70.49401963445538),
                LatLng(-33.37717517156526, -70.49153054457696),
                LatLng(-33.38634900872422, -70.49783910015663),
                LatLng(-33.395056091447444, -70.49474919544863),
                LatLng(-33.401579914634254, -70.50589373869066),
                LatLng(-33.40549956718409, -70.5018874729682),
                LatLng(-33.41143498175826, -70.50562070245759),
                LatLng(-33.418047349992186, -70.50637265629514),
                LatLng(-33.41892086026125, -70.50862811618734),
                LatLng(-33.41705131977432, -70.5139099109832),
                LatLng(-33.413648252824224, -70.5145906378938),
                LatLng(-33.414943520173104, -70.52711295144333),


                ))
        polygon14.fillColor = Color.argb(100,0,0,255)
        polygon14.strokeColor = -0xc771c4
        //map.setOnPolygonClickListener()
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireActivity(),
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e("SelectLocationFragment", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("SelectLocationFragment", "Can't find style. Error: ", e)
        }
    }

    private fun sendAlert(){

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.perm_request_rationale_title)
            .setMessage(R.string.perm_request_rationale)
            .setPositiveButton(R.string.request_perm_again) { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            .setNegativeButton(R.string.dismiss, null)
            .create()
            .show()
    }

    private fun enableMyLocation(){
        val isPermissionGranted = ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if(isPermissionGranted){
            Toast.makeText(requireActivity(),"Ya tenemos permisos", Toast.LENGTH_LONG).show()
            locationPermissionGranted = true
            getDeviceLocation()
        }else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
/*                val permissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )*/
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION,
                )
            }else{
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getDeviceLocation() {
        try {
            Log.i("getDeviceLocation", "$locationPermissionGranted")
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        Log.i("getDeviceLocation", task.result?.longitude.toString())

                        if (lastKnownLocation != null) {
                            //zoom to the user location after taking his permission
                            Log.i("getDeviceLocation", "moving camera to user location")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                                , cameraDefaultZoom.toFloat())
                            )
                            map.addMarker(MarkerOptions()
                                .position(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                                .title("Marker in your actual location")
                            )
                        }else{
                            //zoom to defaultLocation after taking his permission
                            Log.i("getDeviceLocation", "moving camera to default location")
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(defaultLocation.latitude, defaultLocation.longitude)
                                , cameraDefaultZoom.toFloat())
                            )
                            map.addMarker(MarkerOptions().
                            position(defaultLocation).
                            title("Marker in default location"))
                            map.uiSettings.isMyLocationButtonEnabled = false
                        }
                    }
                    else {
                        Log.i("getDeviceLocation", "getting location task wasn't successfully")
                        // zoom to the default location after taking his permission
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(defaultLocation.latitude, defaultLocation.longitude)
                            , cameraDefaultZoom.toFloat())
                        )
                        map.addMarker(MarkerOptions()
                            .position(defaultLocation)
                            .title("Marker in default location"))
                        map.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }else{
                Log.i("getDeviceLocation", "getting location task wasn't successfully")
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(defaultLocation.latitude,
                            defaultLocation.longitude
                        ), cameraDefaultZoom.toFloat()))
                map.addMarker(MarkerOptions().
                position(defaultLocation).
                title("Marker in default location"))
                map.uiSettings.isMyLocationButtonEnabled = false
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

}


