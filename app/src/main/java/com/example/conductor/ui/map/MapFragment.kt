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


class MapFragment : BaseFragment(), OnMapReadyCallback{

    override val _viewModel: MapViewModel by inject()
    private var _binding: FragmentMapBinding? = null
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
        Log.i("MapFragment", "MapFragment onCreateView")
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        // Specify the current fragment as the lifecycle owner of the binding. This is used so that
        // the binding can observe LiveData updates
        _binding!!.lifecycleOwner = this

        //Adding  the map setup implementation
        (childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment)?.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        return _binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("MapFragment", "MapFragment onDestroyView")
        _binding = null
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        //setMapStyle(map)
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

        val polygon15 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.367965921553406, -70.69945622013051),
                LatLng(-33.39093926914157, -70.68909043204256),
                LatLng(-33.402326081330585, -70.68230961279791),
                //orilla inferior
                //LatLng(-33.402326081330585, -70.68230961279791),
                LatLng(-33.39883226490089, -70.67257750530274),
                LatLng(-33.398600741681264, -70.67127676747016),
                LatLng(-33.39752451657337, -70.66921260663689),
                LatLng(-33.3952494995071, -70.65311834383692),
                LatLng(-33.39576975674669, -70.65039759914973),
                LatLng(-33.39614639104907, -70.64590787843372),
                LatLng(-33.39711487176298, -70.64138951516634),
                LatLng(-33.397856473875976, -70.6398837981435),
                LatLng(-33.40021097862057, -70.62931572720349),
                //orilla derecha
                //LatLng(-33.40021097862057, -70.62931572720349),
                LatLng(-33.39398317034928, -70.624819552343),
                LatLng(-33.381331832322246, -70.62065206040525),
                LatLng(-33.38019575250548, -70.61899079214258),
                LatLng(-33.376296780285124, -70.61659638600962),
                LatLng(-33.3712999573455, -70.62898105626259),
                LatLng(-33.36253733535497, -70.62569216046694),
                LatLng(-33.35849751422479, -70.62967740657746),
                LatLng(-33.35926566359923, -70.63775008448923),
                LatLng(-33.33735670731707, -70.66600240824822),
                LatLng(-33.33895377904075, -70.67792290934818),
                LatLng(-33.33895377904075, -70.67792290934818),
                LatLng(-33.35490448735543, -70.69146947020909),
                )
        )
        polygon15.fillColor = Color.argb(100,0,200,80)
        polygon15.strokeColor = -0xc771c4

        val polygon16 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla inferior
                LatLng(-33.367965921553406, -70.69945622013051),
                LatLng(-33.367084113284825, -70.70198223576737),
                LatLng(-33.36855251611958, -70.70405358218835),
                LatLng(-33.37343256723458, -70.71201925345964),
                LatLng(-33.37411643410278, -70.71474090611245),
                LatLng(-33.37162230559324, -70.73740528920683),
                LatLng(-33.37152173438838, -70.74205377577891),
                LatLng(-33.38656589317883, -70.76028644091411),
                //orilla izquierda
                //LatLng(-33.38656589317883, -70.76028644091411),
                LatLng(-33.376111120920825, -70.77760065843722),
                LatLng(-33.3686689222336, -70.77757657304097),
                LatLng(-33.363936447938194, -70.76284265858735),
                LatLng(-33.35977390367044, -70.76370193529715),
                LatLng(-33.35845687993358, -70.75643194211636),
                LatLng(-33.35392312865247, -70.75855854348396),
                LatLng(-33.350945611280885, -70.75349520705839),
                //orilla superior
                LatLng(-33.34740967714015, -70.74569766894662),
                LatLng(-33.34869774534686, -70.74002490391467),
                LatLng(-33.34852702532649, -70.72883555928475),
                LatLng(-33.350471116417644, -70.71768540483178),
                LatLng(-33.35643454020242, -70.71225750815583),
                LatLng(-33.35636687245486, -70.70989799339506),
                LatLng(-33.357449550382434, -70.70706252495646),
                LatLng(-33.35698433886634, -70.70440933666627),
                //orilla derecha
                //LatLng(-33.35698433886634, -70.70440933666627),
                LatLng(-33.35698433886634, -70.70440933666627),
                LatLng(-33.362319771057415, -70.70193556100757),
            )
        )
        polygon16.fillColor = Color.argb(100,200,0,0)
        polygon16.strokeColor = -0xc771c4

        val polygon17 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.367965921553406, -70.69945622013051),
                LatLng(-33.367084113284825, -70.70198223576737),
                LatLng(-33.36855251611958, -70.70405358218835),
                LatLng(-33.37343256723458, -70.71201925345964),
                LatLng(-33.37411643410278, -70.71474090611245),
                LatLng(-33.37162230559324, -70.73740528920683),
                LatLng(-33.37152173438838, -70.74205377577891),
                LatLng(-33.38656589317883, -70.76028644091411),
                LatLng(-33.40420350216588, -70.78119640970522),
                LatLng(-33.40635920008716, -70.78251287719789),
                LatLng(-33.41585997812206, -70.78218730623662),
                //orilla inferior
                //LatLng(-33.41585997812206, -70.78218730623662),
                LatLng(-33.415495560537195, -70.77492825359403),
                LatLng(-33.413296004810164, -70.77139089792539),
                LatLng(-33.412829787472205, -70.76962938073152),
                LatLng(-33.41300910212671, -70.76536163984338),
                LatLng(-33.411861481925605, -70.75937534555021),
                LatLng(-33.41202884414532, -70.75533674513791),
                LatLng(-33.41155066550687, -70.75053911696195),
                LatLng(-33.411789755150316, -70.73956901789465),
                LatLng(-33.4114669839714, -70.73312444267467),
                LatLng(-33.412635509929125, -70.72744961397832),
                LatLng(-33.411206193445494, -70.72299188514413),
                LatLng(-33.40974901682923, -70.7203038788019),
                LatLng(-33.40913056965962, -70.71604325100639),
                LatLng(-33.40738916358338, -70.71223214710845),
                LatLng(-33.407316155518444, -70.7045506033562),
                LatLng(-33.40779953449935, -70.70317431752396),
                LatLng(-33.41344633529561, -70.6944350550364),
                LatLng(-33.41577931061239, -70.69186288009158),
                //orilla derecha
                //LatLng(-33.41577931061239, -70.69186288009158),
                LatLng(-33.411950155098175, -70.69250086050452),
                LatLng(-33.40685345847944, -70.69465527769432),
                LatLng(-33.400179117905786, -70.69777413913788),
                LatLng(-33.394632976674366, -70.70113619457062),
                LatLng(-33.39358458095296, -70.70115644791687),
                LatLng(-33.3837042760854, -70.6963539106719),
                LatLng(-33.381723277282816, -70.69516168222148),
                LatLng(-33.38024504064071, -70.69389719751226),
            )
        )
        polygon17.fillColor = Color.argb(100,255,255,0)
        polygon17.strokeColor = -0xc771c4
        val polygon18 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.41577931061239, -70.69186288009158),
                LatLng(-33.41344633529561, -70.6944350550364),
                LatLng(-33.40779953449935, -70.70317431752396),
                LatLng(-33.407316155518444, -70.7045506033562),
                LatLng(-33.40738916358338, -70.71223214710845),
                LatLng(-33.40913056965962, -70.71604325100639),
                LatLng(-33.40974901682923, -70.7203038788019),
                LatLng(-33.411206193445494, -70.72299188514413),
                LatLng(-33.412635509929125, -70.72744961397832),
                LatLng(-33.4114669839714, -70.73312444267467),
                LatLng(-33.411789755150316, -70.73956901789465),
                LatLng(-33.41155066550687, -70.75053911696195),
                LatLng(-33.41202884414532, -70.75533674513791),
                LatLng(-33.411861481925605, -70.75937534555021),
                LatLng(-33.41300910212671, -70.76536163984338),
                LatLng(-33.412829787472205, -70.76962938073152),
                LatLng(-33.413296004810164, -70.77139089792539),
                LatLng(-33.415495560537195, -70.77492825359403),
                LatLng(-33.41585997812206, -70.78218730623662),
                //orilla izquierda
                //LatLng(-33.41585997812206, -70.78218730623662),
                LatLng(-33.418480402135955, -70.7819784354597),
                LatLng(-33.43262411559484, -70.78508713161278),
                LatLng(-33.43434901986538, -70.7850871316031),
                LatLng(-33.43664081608109, -70.78443469839681),
                LatLng(-33.43786523201317, -70.7837839991719),
                LatLng(-33.44601886235158, -70.77786658163161),
                //orilla inferior
                //LatLng(-33.44601886235158, -70.77786658163161),
                LatLng(-33.45862264017564, -70.71221372034135),
                LatLng(-33.45383648948167, -70.69085150182336),
                //orila derecha
                LatLng(-33.45383648948167, -70.69085150182336),
                LatLng(-33.44952672004219, -70.69230605581289),
                LatLng(-33.44853924926007, -70.69235157441499),
                LatLng(-33.43967682885347, -70.69168396835248),
                LatLng(-33.43661275287396, -70.69229088298822),
                LatLng(-33.42952191433057, -70.69223019153425),
                LatLng(-33.42020164541187, -70.69065221349801),
                LatLng(-33.41898588434675, -70.6908039421627),
                LatLng(-33.41577931061239, -70.69186288009158),

            )
        )
        polygon18.fillColor = Color.argb(100,128,64,0)
        polygon18.strokeColor = -0xc771c4

        val polygon19 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orila izquierda
                LatLng(-33.45383648948167, -70.69085150182336),
                LatLng(-33.44952672004219, -70.69230605581289),
                LatLng(-33.44853924926007, -70.69235157441499),
                LatLng(-33.43967682885347, -70.69168396835248),
                LatLng(-33.43661275287396, -70.69229088298822),
                LatLng(-33.42952191433057, -70.69223019153425),
                LatLng(-33.42020164541187, -70.69065221349801),
                LatLng(-33.41898588434675, -70.6908039421627),
                LatLng(-33.411950155098175, -70.69250086050452),
                LatLng(-33.40685345847944, -70.69465527769432),
                LatLng(-33.400179117905786, -70.69777413913788),
                LatLng(-33.394632976674366, -70.70113619457062),
                LatLng(-33.39358458095296, -70.70115644791687),
                LatLng(-33.3837042760854, -70.6963539106719),
                LatLng(-33.381723277282816, -70.69516168222148),
                LatLng(-33.38024504064071, -70.69389719751226),
                //orilla superior
                //LatLng(-33.38024504064071, -70.69389719751226),
                LatLng(-33.39093926914157, -70.68909043204256),
                LatLng(-33.402326081330585, -70.68230961279791),
                LatLng(-33.39883226490089, -70.67257750530274),
                LatLng(-33.398600741681264, -70.67127676747016),
                LatLng(-33.39752451657337, -70.66921260663689),
                LatLng(-33.3952494995071, -70.65311834383692),
                LatLng(-33.39576975674669, -70.65039759914973),
                LatLng(-33.39614639104907, -70.64590787843372),
                LatLng(-33.39711487176298, -70.64138951516634),
                LatLng(-33.397856473875976, -70.6398837981435),
                LatLng(-33.40021097862057, -70.62931572720349),
                LatLng(-33.39400558909397, -70.62484213657197),
                LatLng(-33.39676389160262, -70.61886320171658),
                LatLng(-33.39721992061465, -70.6171638407445),
                //orilla derecha
                LatLng(-33.39721992061465, -70.6171638407445),
                LatLng(-33.39897744482788, -70.61504743748705),
                LatLng(-33.40166165416776, -70.61305706336427),
                LatLng(-33.404345780637186, -70.61290395766913),
                LatLng(-33.41030879777241, -70.61536376275866),
                LatLng(-33.411437231748984, -70.61540667809938),
                LatLng(-33.41343933154565, -70.61467464438802),
                LatLng(-33.41468096318018, -70.61358950452022),
                LatLng(-33.41458414255164, -70.61233220100772),
                LatLng(-33.41267890126766, -70.61056679118279),
                LatLng(-33.41233694057248, -70.60851845432016),
                LatLng(-33.41137437734133, -70.60677357479112),
                LatLng(-33.416212416140084, -70.6083363799676),
                LatLng(-33.4162377455446, -70.60687978487515),
                LatLng(-33.417115599340235, -70.60492713665879),
                //orilla inferior
                //LatLng(-33.417115599340235, -70.60492713665879),
                LatLng(-33.41996833818851, -70.60627957039493),
                LatLng(-33.420567798057284, -70.60784961944336),
                LatLng(-33.42354628031994, -70.612429284972),
                LatLng(-33.427087361800766, -70.61641257600404),
                LatLng(-33.42884064980527, -70.62035001647268),
                LatLng(-33.43102368548371, -70.62371353782515),
                LatLng(-33.432793859031555, -70.62574713682787),
                LatLng(-33.434593252477086, -70.62774555809237),
                LatLng(-33.4366924976778, -70.63287308638175),
                LatLng(-33.43665592590379, -70.63444202236786),
                LatLng(-33.4371386721777, -70.63526593288861),
                LatLng(-33.43750438726438, -70.63668586379762),
                LatLng(-33.43843329664694, -70.63814085472855),
                LatLng(-33.44113732298466, -70.64195460084551),
                LatLng(-33.44290532889718, -70.64128251109882),
                LatLng(-33.44218664527311, -70.6439566823151),
                LatLng(-33.4470557940686, -70.66465712323757),
                LatLng(-33.4506059061429, -70.6778224942839),
                LatLng(-33.45091338701772, -70.6795766641679),
                LatLng(-33.45149759765526, -70.6807485676169),
                )
        )
        polygon19.fillColor = Color.argb(100,150,0,0)
        polygon19.strokeColor = -0xc771c4

        val polygon20 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orila superior
                LatLng(-33.417115599340235, -70.60492713665879),
                LatLng(-33.41996833818851, -70.60627957039493),
                LatLng(-33.420567798057284, -70.60784961944336),
                LatLng(-33.42354628031994, -70.612429284972),
                LatLng(-33.427087361800766, -70.61641257600404),
                LatLng(-33.42884064980527, -70.62035001647268),
                LatLng(-33.43102368548371, -70.62371353782515),
                LatLng(-33.432793859031555, -70.62574713682787),
                LatLng(-33.433610471063254, -70.62650023380381),
                //orilla izquierda
                //LatLng(-33.433610471063254, -70.62650023380381),
                LatLng(-33.439774937436006, -70.62468860788233),
                LatLng(-33.44464536559267, -70.62323722724987),
                LatLng(-33.446817180554014, -70.62258418942014),
                LatLng(-33.44906801177662, -70.62191683930611),
                LatLng(-33.45030855003318, -70.62159929552176),
                LatLng(-33.4534569603371, -70.62060686093278),
                LatLng(-33.454504522877045, -70.62064390611728),
                LatLng(-33.455843538788926, -70.61783099287777),
                LatLng(-33.461285207663174, -70.61501668732484),
                LatLng(-33.46266641891651, -70.61510492749848),
                LatLng(-33.465662347120784, -70.6148625535565),
                LatLng(-33.46766864395508, -70.61400771340966),
                LatLng(-33.485254768707975, -70.61284710097293),
                //orilla inferior
                //LatLng(-33.485254768707975, -70.61284710097293),
                LatLng(-33.4850001596817, -70.60903817159037),
                LatLng(-33.48502273314279, -70.60462649194037),
                LatLng(-33.48478947377358, -70.60165830664748),
                LatLng(-33.4850453065984, -70.60090047212066),
                LatLng(-33.48582507038609, -70.59329673342978),
                LatLng(-33.486208814745495, -70.59021126418092),
                LatLng(-33.48708616637395, -70.58593614438861),
                LatLng(-33.48771252120738, -70.5850349221383),
                LatLng(-33.48899181620626, -70.58063255434779),
                //orilla derecha
                //LatLng(-33.48899181620626, -70.58063255434779),
                LatLng(-33.46910471625917, -70.57650861763321),
                LatLng(-33.45900936416617, -70.57234482354653),
                LatLng(-33.45275409331351, -70.57070973193235),
                LatLng(-33.43817816151477, -70.57338900553638),
                //orilla superior
                //LatLng(-33.43817816151477, -70.57338900553638),
                LatLng(-33.43624012033715, -70.57816057020236),
                LatLng(-33.43313233043785, -70.58345563609788),
                LatLng(-33.43166384155687, -70.58488010815117),
                LatLng(-33.42789654227437, -70.58889176598251),
                LatLng(-33.42613465840562, -70.5909727927234),
                LatLng(-33.4229043768711, -70.5951439009538),
                LatLng(-33.42077097001878, -70.59796757213522),
                LatLng(-33.41786063621321, -70.60173278815357),

            )
        )
        polygon20.fillColor = Color.argb(100,150,200,0)
        polygon20.strokeColor = -0xc771c4

        val polygon21 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.439774937436006, -70.62468860788233),
                LatLng(-33.44464536559267, -70.62323722724987),
                LatLng(-33.446817180554014, -70.62258418942014),
                LatLng(-33.44906801177662, -70.62191683930611),
                LatLng(-33.45030855003318, -70.62159929552176),
                LatLng(-33.4534569603371, -70.62060686093278),
                LatLng(-33.454504522877045, -70.62064390611728),
                LatLng(-33.455843538788926, -70.61783099287777),
                LatLng(-33.461285207663174, -70.61501668732484),
                LatLng(-33.46266641891651, -70.61510492749848),
                LatLng(-33.465662347120784, -70.6148625535565),
                LatLng(-33.46766864395508, -70.61400771340966),
                LatLng(-33.485254768707975, -70.61284710097293),
                //orilla inferior
                //LatLng(-33.485254768707975, -70.61284710097293),
                LatLng(-33.48548561405476, -70.61627782004994),
                LatLng(-33.48597914041968, -70.61767372363462),
                LatLng(-33.485925934843436, -70.6184137345264),
                LatLng(-33.48737715567004, -70.61917947700583),
                LatLng(-33.48448985350259, -70.62929773085305),
                LatLng(-33.480929269640804, -70.64174304625367),
                LatLng(-33.47999912446728, -70.64649451896881),
                LatLng(-33.4795551529361, -70.64868682608773),
                LatLng(-33.47921020549267, -70.65165196350479),
                LatLng(-33.478749978395804, -70.6547314587486),
                LatLng(-33.478712353235494, -70.65586821058804),
                //orilla izquierda
                //LatLng(-33.478712353235494, -70.65586821058804),
                LatLng(-33.47734350278473, -70.65537690201248),
                LatLng(-33.475381839655356, -70.65452204916723),
                LatLng(-33.47369100547673, -70.65473781041497),
                LatLng(-33.47131292435143, -70.65623543583588),
                LatLng(-33.460324740138084, -70.6569571830168),
                LatLng(-33.458071503778875, -70.65660794560804),
                LatLng(-33.45607961657121, -70.65688500829349),
                LatLng(-33.45441901431358, -70.65810985387431),
                LatLng(-33.44615810531292, -70.66069801586947),
                //orilla superior
                //LatLng(-33.44615810531292, -70.66069801586947),
                LatLng(-33.443081597510876, -70.64764935150957),
                LatLng(-33.44217001868276, -70.6440382095581),
                LatLng(-33.44292442408814, -70.6412582406888),
                LatLng(-33.4411295739933, -70.64191905629531),
                LatLng(-33.44011324798242, -70.64046654010355),
                LatLng(-33.438479344987734, -70.63809546734066),
                LatLng(-33.43754375380971, -70.63651064455178),
                LatLng(-33.436974134277904, -70.63476906723308),
                LatLng(-33.43672392638737, -70.63447561463806),
                LatLng(-33.436750111628875, -70.63296012294232),
                LatLng(-33.435606092003404, -70.62983494587458),
                LatLng(-33.43458956433369, -70.62760131370395),
                LatLng(-33.433610471063254, -70.62650023380381)

            )
        )
        polygon21.fillColor = Color.argb(100,230,100,230)
        polygon21.strokeColor = -0xc771c4

        val polygon22 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.478712353235494, -70.65586821058804),
                LatLng(-33.47734350278473, -70.65537690201248),
                LatLng(-33.475381839655356, -70.65452204916723),
                LatLng(-33.47369100547673, -70.65473781041497),
                LatLng(-33.47131292435143, -70.65623543583588),
                LatLng(-33.460324740138084, -70.6569571830168),
                LatLng(-33.458071503778875, -70.65660794560804),
                LatLng(-33.45607961657121, -70.65688500829349),
                LatLng(-33.45441901431358, -70.65810985387431),
                LatLng(-33.44615810531292, -70.66069801586947),
                //orilla superior
                LatLng(-33.44705827458407, -70.6646607471422),
                LatLng(-33.450765874704906, -70.67854865971036),
                LatLng(-33.45089828832731, -70.67957016203792),
                LatLng(-33.45141733075541, -70.68052605258474),
                LatLng(-33.45383648948167, -70.69085150182336),
                LatLng(-33.45862264017564, -70.71221372034135),
                LatLng(-33.44601886235158, -70.77786658163161),
                //orilla izquierda
                //LatLng(-33.44601886235158, -70.77786658163161),
                LatLng(-33.44860271716984, -70.77607024939451),
                LatLng(-33.457146111106205, -70.76613190920406),
                LatLng(-33.45892996964469, -70.76483276424729),
                LatLng(-33.46786628400551, -70.76196359564533),
                LatLng(-33.47001621146665, -70.7611215143526),
                LatLng(-33.48544042200525, -70.75198744930017),
                LatLng(-33.49060204046467, -70.74911850423615),
                //orilla inferior
                //23
                //LatLng(-33.49060204046467, -70.74911850423615),
                LatLng(-33.486299637970475, -70.74386869274902),
                LatLng(-33.48520107231194, -70.74220867418984),
                LatLng(-33.48450271743252, -70.73967670484802),
                LatLng(-33.48406091243843, -70.73103438985416),
                LatLng(-33.48361249414936, -70.72184165859457),
                LatLng(-33.48342227871012, -70.71690083754241),
                LatLng(-33.482916082821646, -70.71322900415927),
                LatLng(-33.48046891308726, -70.70226425171538),
                LatLng(-33.480128375157506, -70.699597660823),
                LatLng(-33.47876621008501, -70.69506828394793),
                LatLng(-33.47823209020067, -70.69267266020309),
                LatLng(-33.478130644984745, -70.68514291335886),
                //24
                //LatLng(-33.478130644984745, -70.68514291335886),
                LatLng(-33.47839911108342, -70.68263236571511),
                LatLng(-33.47843490649284, -70.67434970419296),
                LatLng(-33.47852136213743, -70.67163491893443),
                LatLng(-33.478711195564976, -70.66902518609201),
                LatLng(-33.47867978920735, -70.66439957584895),
                LatLng(-33.47889519182088, -70.66034715282458),
                LatLng(-33.478737166294565, -70.65590840763909),
            )
        )
        polygon22.fillColor = Color.argb(100,0,250,0)
        polygon22.strokeColor = -0xc771c4

        val polygon23 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.49060204046467, -70.74911850423615),
                LatLng(-33.486299637970475, -70.74386869274902),
                LatLng(-33.48520107231194, -70.74220867418984),
                LatLng(-33.48450271743252, -70.73967670484802),
                LatLng(-33.48406091243843, -70.73103438985416),
                LatLng(-33.48361249414936, -70.72184165859457),
                LatLng(-33.48342227871012, -70.71690083754241),
                LatLng(-33.482916082821646, -70.71322900415927),
                LatLng(-33.48046891308726, -70.70226425171538),
                LatLng(-33.480128375157506, -70.699597660823),
                LatLng(-33.47876621008501, -70.69506828394793),
                LatLng(-33.47823209020067, -70.69267266020309),
                LatLng(-33.478130644984745, -70.68514291335886),
                //orilla derecha
                //LatLng(-33.478130644984745, -70.68514291335886),
                LatLng(-33.47918364835211, -70.68532131508485),
                LatLng(-33.482530425578595, -70.68658731766048),
                LatLng(-33.488272355840095, -70.68896465389861),
                LatLng(-33.495837810037756, -70.69215345633086),
                LatLng(-33.50555376589341, -70.69689560204077),
                LatLng(-33.5128948089324, -70.70052949585026),
                LatLng(-33.51702657438214, -70.70253256924339),
                LatLng(-33.519962418226356, -70.7037829323768),
                //orilla inferior
                //LatLng(-33.519962418226356, -70.7037829323768),
                LatLng(-33.51852036106649, -70.70778856878087),
                LatLng(-33.517141529540545, -70.71149074796983),
                LatLng(-33.51488086735867, -70.7170440167321),
                LatLng(-33.51234042563778, -70.7203699559969),
                LatLng(-33.50917028165241, -70.72389138698043),
                LatLng(-33.506148962459065, -70.72743833575785),
                LatLng(-33.50394299684193, -70.73460358324247),
                LatLng(-33.50213749604351, -70.74073843422181),
                LatLng(-33.49801479466044, -70.74450956335006),
                LatLng(-33.49601355863567, -70.74611545080087),
            )
        )
        polygon23.fillColor = Color.argb(100,255,165,0)
        polygon23.strokeColor = -0xc771c4

        val polygon24 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.478130644984745, -70.68514291335886),
                LatLng(-33.47839911108342, -70.68263236571511),
                LatLng(-33.47843490649284, -70.67434970419296),
                LatLng(-33.47852136213743, -70.67163491893443),
                LatLng(-33.478711195564976, -70.66902518609201),
                LatLng(-33.47867978920735, -70.66439957584895),
                LatLng(-33.47889519182088, -70.66034715282458),
                LatLng(-33.478737166294565, -70.65590840763909),
                //orilla derecha
                //LatLng(-33.478737166294565, -70.65590840763909),
                LatLng(-33.48179556998076, -70.65744513722078),
                LatLng(-33.484748526362836, -70.65879697046857),
                LatLng(-33.48829194116877, -70.66057795716576),
                LatLng(-33.49487729665743, -70.66364640426599),
                LatLng(-33.50037930989657, -70.66628128304203),
                LatLng(-33.50678094185235, -70.66927157764076),
                LatLng(-33.51070903223496, -70.67119450043762),
                //orilla inferior
                //LatLng(-33.51070903223496, -70.67119450043762),
                LatLng(-33.50921968923939, -70.67477972541897),
                LatLng(-33.50838990131357, -70.67682113461638),
                LatLng(-33.508283517642674, -70.67827563890899),
                LatLng(-33.505900489738195, -70.68385123795356),
                LatLng(-33.505645161417505, -70.68478263098872),
                LatLng(-33.50331723343999, -70.69147386480367),
                LatLng(-33.50206468110237, -70.6952215623694),
                //orilla izquierda
                //LatLng(-33.50206468110237, -70.6952215623694),
                LatLng(-33.493103350156005, -70.69080318923741),
                LatLng(-33.48832021574144, -70.68895209973255),
                LatLng(-33.485135442413544, -70.68750543064975),
                LatLng(-33.47923819363731, -70.68530536520286),
            )
        )
        polygon24.fillColor = Color.argb(75,255,0,0)
        polygon24.strokeColor = -0xc771c4

        val polygon25 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.478737166294565, -70.65590840763909),
                LatLng(-33.48179556998076, -70.65744513722078),
                LatLng(-33.484748526362836, -70.65879697046857),
                LatLng(-33.48829194116877, -70.66057795716576),
                LatLng(-33.49487729665743, -70.66364640426599),
                LatLng(-33.50037930989657, -70.66628128304203),
                LatLng(-33.50678094185235, -70.66927157764076),
                LatLng(-33.51070903223496, -70.67119450043762),
                //orilla inferior
                //LatLng(-33.51070903223496, -70.67119450043762),
                LatLng(-33.51210404625855, -70.66659930162425),
                LatLng(-33.51434036061665, -70.65937879505474),
                LatLng(-33.51490364868641, -70.65724943865436),
                LatLng(-33.51578916226238, -70.65250791818485),
                LatLng(-33.516746445226964, -70.64769978046589),
                LatLng(-33.51763792788677, -70.64330781123587),
                LatLng(-33.51877440707717, -70.63748152715377),
                LatLng(-33.518733683773256, -70.6366425415581),
                //orilla derecha
                //LatLng(-33.518733683773256, -70.6366425415581),
                LatLng(-33.516567448604604, -70.6368861312072),
                LatLng(-33.51410066046176, -70.63717441562625),
                LatLng(-33.51251302369694, -70.63740959503183),
                LatLng(-33.51021058237249, -70.63792547240725),
                LatLng(-33.50701695535976, -70.63864430441278),
                LatLng(-33.503411276997966, -70.63897348433767),
                LatLng(-33.496866117186485, -70.63967718787308),
                LatLng(-33.490845365221425, -70.64044987345468),
                LatLng(-33.48092066127345, -70.64175839520888),
                //orilla superior
                //LatLng(-33.48092066127345, -70.64175839520888),
                LatLng(-33.47957694728911, -70.64872349049892),
                LatLng(-33.47920817183202, -70.6516404110602),
                LatLng(-33.47873108317216, -70.65472854895665),
            )
        )
        polygon25.fillColor = Color.argb(75,255,100,0)
        polygon25.strokeColor = -0xc771c4

        val polygon26 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.507137786822994, -70.63862285362195),
                LatLng(-33.503411276997966, -70.63897348433767),
                LatLng(-33.496866117186485, -70.63967718787308),
                LatLng(-33.490845365221425, -70.64044987345468),
                LatLng(-33.48092066127345, -70.64175839520888),
                //orilla superior
                //LatLng(-33.48092066127345, -70.64175839520888),
                LatLng(-33.48167705480181, -70.63903957985752),
                LatLng(-33.4836670187472, -70.63218809998615),
                LatLng(-33.48448343438536, -70.62933268763392),
                LatLng(-33.485514456803564, -70.62578403377675),
                LatLng(-33.485628347724564, -70.62522263765342),
                LatLng(-33.486786230178865, -70.62113355039726),
                LatLng(-33.48737680436248, -70.61911127934968),
                LatLng(-33.48597218885791, -70.61844782136052),
                LatLng(-33.48597797928177, -70.61768928125763),
                LatLng(-33.48553407147723, -70.61627701884579),
                LatLng(-33.485002578691294, -70.60914577215817),
                LatLng(-33.48501017776615, -70.60682893569798),
                LatLng(-33.48499752313768, -70.60545579138791),
                LatLng(-33.48501650507945, -70.60459093806512),
                LatLng(-33.48480137614915, -70.60167774788981),
                LatLng(-33.48499752312854, -70.60101014184572),
                LatLng(-33.48535817932603, -70.5975507285393),
                LatLng(-33.48568964752832, -70.59478667272315),
                LatLng(-33.48619976908832, -70.59034370733954),
                LatLng(-33.486975636855504, -70.58634444167745),
                LatLng(-33.48718845638504, -70.58584046878184),
                LatLng(-33.487704541577706, -70.58512597551757),
                LatLng(-33.48902689895911, -70.58064239384309),
                //orilla derecha
                //LatLng(-33.48902689895911, -70.58064239384309),
                LatLng(-33.49932286152575, -70.58668694389743),
                LatLng(-33.50020910452407, -70.58719891647286),
                LatLng(-33.50541810719324, -70.58948147633598),
                LatLng(-33.508170412770966, -70.58996762518717),
                LatLng(-33.51318152192266, -70.59091806699722),
                LatLng(-33.5151644160357, -70.59263500418307),
                LatLng(-33.51762148795468, -70.59470577082095),
                LatLng(-33.51950629676697, -70.59607132875234),
                LatLng(-33.5222385640882, -70.59801345550615),
                LatLng(-33.52281759248361, -70.59840946299794),
                //orilla inferior
                //LatLng(-33.52281759248361, -70.59840946299794),
                LatLng(-33.52134274148347, -70.60074168745193),
                LatLng(-33.5200279020641, -70.60246903007503),
                LatLng(-33.51780962429014, -70.60461479723442),
                LatLng(-33.516396336998056, -70.60607391893735),
                LatLng(-33.51480412492339, -70.6076188713498),
                LatLng(-33.51241063871657, -70.60967941886112),
                LatLng(-33.50980804911204, -70.61212007455185),
                LatLng(-33.50843198924616, -70.61359008096679),
                LatLng(-33.50809156129265, -70.61538907284941),
                LatLng(-33.507888135534934, -70.61909532661535),
                LatLng(-33.5078333914333, -70.62105374503649),
                LatLng(-33.50743876443713, -70.62612590657437),
                LatLng(-33.5070567189949, -70.63212447866424),
                LatLng(-33.50704406758636, -70.63470386589954),

            )
        )
        polygon26.fillColor = Color.argb(100,128,64,0)
        polygon26.strokeColor = -0xc771c4

        val polygon27 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.52281759248361, -70.59840946299794),
                LatLng(-33.52134274148347, -70.60074168745193),
                LatLng(-33.5200279020641, -70.60246903007503),
                LatLng(-33.51780962429014, -70.60461479723442),
                LatLng(-33.516396336998056, -70.60607391893735),
                LatLng(-33.51480412492339, -70.6076188713498),
                LatLng(-33.51241063871657, -70.60967941886112),
                LatLng(-33.50980804911204, -70.61212007455185),
                LatLng(-33.50843198924616, -70.61359008096679),
                LatLng(-33.50809156129265, -70.61538907284941),
                LatLng(-33.507888135534934, -70.61909532661535),
                LatLng(-33.5078333914333, -70.62105374503649),
                LatLng(-33.50743876443713, -70.62612590657437),
                LatLng(-33.5070567189949, -70.63212447866424),
                //orilla izquierda
                //LatLng(-33.5070567189949, -70.63212447866424),
                LatLng(-33.50704406758636, -70.63470386589954),
                LatLng(-33.50715217651171, -70.63861473372778),
                LatLng(-33.509509385099214, -70.63809974959428),
                LatLng(-33.51224670805212, -70.63746138385017),
                LatLng(-33.51307576694185, -70.63729429235245),
                LatLng(-33.51492741854987, -70.63704216470235),
                LatLng(-33.51702054222821, -70.63682758798493),
                LatLng(-33.518697688115964, -70.63660228243168),
                LatLng(-33.52131844297167, -70.63628578176393),
                LatLng(-33.52256618062255, -70.6361570357358),
                LatLng(-33.52550433041713, -70.63580834856613),
                LatLng(-33.52828139392179, -70.63552403440782),
                LatLng(-33.529904660292985, -70.6352236270017),
                LatLng(-33.53409329155267, -70.63465722586201),
                LatLng(-33.535953436147466, -70.63454457308197),
                LatLng(-33.5381595726459, -70.63445153171084),
                LatLng(-33.540511465074005, -70.63428523475181),
                LatLng(-33.54233570257363, -70.634129666626),
                //Orilla inferior
                // LatLng(-33.54233570257363, -70.634129666626),
                LatLng(-33.54262632509082, -70.63212873869789),
                LatLng(-33.54288564897172, -70.62980058125991),
                LatLng(-33.542823053622236, -70.62822880678958),
                LatLng(-33.54238935603183, -70.6252944701232),
                LatLng(-33.54204060793814, -70.62238695553941),
                LatLng(-33.541763396912565, -70.62043430738305),
                LatLng(-33.54107930783077, -70.61584772995012),
                LatLng(-33.54072649486196, -70.61321420682575),
                LatLng(-33.54023913198942, -70.61202330601391),
                LatLng(-33.53985460521556, -70.61143858444876),
                LatLng(-33.539179443591905, -70.61076803221798),
                LatLng(-33.538298595668394, -70.61017794623457),
                LatLng(-33.53616127253892, -70.60883684174448),
                LatLng(-33.53296860360479, -70.60675008311016),
                LatLng(-33.53107443396449, -70.60555599260303),
                LatLng(-33.52862252275782, -70.60393507673552),
                LatLng(-33.52656445742554, -70.60258264306917),
                LatLng(-33.52508602366695, -70.60151090318591),
                LatLng(-33.52455420678159, -70.60094951561992),
                LatLng(-33.52295591390241, -70.59860517266912),
            )
        )

        polygon27.fillColor = Color.argb(100,250,100,0)
        polygon27.strokeColor = -0xc771c4

        val polygon28 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.51876677203752, -70.63665701649214),
                LatLng(-33.52131844297167, -70.63628578176393),
                LatLng(-33.52256618062255, -70.6361570357358),
                LatLng(-33.52550433041713, -70.63580834856613),
                LatLng(-33.52828139392179, -70.63552403440782),
                LatLng(-33.529904660292985, -70.6352236270017),
                LatLng(-33.53409329155267, -70.63465722586201),
                LatLng(-33.535953436147466, -70.63454457308197),
                LatLng(-33.5381595726459, -70.63445153171084),
                LatLng(-33.540511465074005, -70.63428523475181),
                LatLng(-33.54233570257363, -70.634129666626),
                //orilla inferior
                //LatLng(-33.54233570257363, -70.634129666626),
                LatLng(-33.542218902375325, -70.6353330474854),
                LatLng(-33.54194616306815, -70.6376182895716),
                LatLng(-33.54170024983679, -70.6394743782022),
                LatLng(-33.5413470277878, -70.6423389774416),
                LatLng(-33.54133361426274, -70.64245163020554),
                LatLng(-33.540908851593365, -70.64518211900345),
                LatLng(-33.54062716572239, -70.64708112297444),
                LatLng(-33.540242640673, -70.64984379826657),
                LatLng(-33.53984917140883, -70.652118311507),
                LatLng(-33.539223193887985, -70.65558372555029),
                LatLng(-33.53869110943424, -70.65860925732966),
                LatLng(-33.53822942787649, -70.66120364319335),
                LatLng(-33.53765262365493, -70.66324212204765),
                LatLng(-33.537285970908385, -70.66446520934497),
                //orilla izquierda
                //LatLng(-33.537285970908385, -70.66446520934497),
                LatLng(-33.53617258902943, -70.66405751356514),
                LatLng(-33.53423643297458, -70.6634888852523),
                LatLng(-33.53276081147198, -70.66309191832873),
                LatLng(-33.53027902760688, -70.66243745932815),
                LatLng(-33.528315908472905, -70.66198148379691),
                LatLng(-33.525118746044285, -70.66106298612067),
                LatLng(-33.522157116203765, -70.66024764290624),
                LatLng(-33.51957362780137, -70.65949662436914),
                LatLng(-33.51634020907826, -70.65860861695703),
                LatLng(-33.514686893418336, -70.65812124770076),
                //orilla superior
                //LatLng(-33.514686893418336, -70.65812124770076),
                LatLng(-33.51494219505502, -70.65703355930887),
                LatLng(-33.51516558336597, -70.65578638579639),
                LatLng(-33.515580667292696, -70.65356269873443),
                LatLng(-33.516035418106405, -70.65132671760132),
                LatLng(-33.51641038627621, -70.64922789365039),
                LatLng(-33.51693304909121, -70.64677347398873),
                LatLng(-33.5174242254381, -70.64431738152344),
                LatLng(-33.51771940854499, -70.6429649478665),
                LatLng(-33.51825071120304, -70.64022610417065),
                LatLng(-33.51853094488226, -70.63885742431148),
                LatLng(-33.5187170941239, -70.63800258415726),
                LatLng(-33.51878091662992, -70.63711265729984),
            ),
        )

        polygon28.fillColor = Color.argb(100, 0, 100, 0)
        polygon28.strokeColor = -0xc771c4

        val polygon29 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.537285970908385, -70.66446520934497),
                LatLng(-33.53617258902943, -70.66405751356514),
                LatLng(-33.53423643297458, -70.6634888852523),
                LatLng(-33.53276081147198, -70.66309191832873),
                LatLng(-33.53027902760688, -70.66243745932815),
                LatLng(-33.528315908472905, -70.66198148379691),
                LatLng(-33.525118746044285, -70.66106298612067),
                LatLng(-33.522157116203765, -70.66024764290624),
                LatLng(-33.51957362780137, -70.65949662436914),
                LatLng(-33.51634020907826, -70.65860861695703),
                LatLng(-33.514686893418336, -70.65812124770076),
                //orilla superior
                //LatLng(-33.514686893418336, -70.65812124770076),
                LatLng(-33.51444800247415, -70.65895532327133),
                LatLng(-33.51415704449813, -70.65996431881362),
                LatLng(-33.51359410130796, -70.66181161521041),
                LatLng(-33.513053292875256, -70.66355649473721),
                LatLng(-33.51288883583261, -70.66404581963702),
                LatLng(-33.51270856526505, -70.66461859532092),
                LatLng(-33.51246504126321, -70.66537344537512),
                LatLng(-33.512306908426574, -70.66592346174785),
                LatLng(-33.51212979930939, -70.66652658314581),
                LatLng(-33.51155102948729, -70.66844974384966),
                LatLng(-33.5108995134703, -70.67060808395998),
                LatLng(-33.51075402864406, -70.67101775133654),
                //orilla izquierda
                //LatLng(-33.51075402864406, -70.67101775133654),
                LatLng(-33.51143130948609, -70.67134993268282),
                LatLng(-33.5118111792849, -70.67155743499119),
                LatLng(-33.51279657631902, -70.67199499422334),
                LatLng(-33.51386846493316, -70.67251826093863),
                LatLng(-33.51576054156057, -70.67343267290029),
                LatLng(-33.51714454431965, -70.67410479996158),
                LatLng(-33.5194311090601, -70.67521899717032),
                LatLng(-33.52128658279642, -70.67609651361816),
                LatLng(-33.52381133197243, -70.67731460094164),
                LatLng(-33.52439045583841, -70.67760329980814),
                LatLng(-33.52613083407418, -70.6784219067253),
                LatLng(-33.52779293975879, -70.67932409070605),
                LatLng(-33.52963976999531, -70.68031782273881),
                LatLng(-33.530835538292145, -70.68097641704067),
                LatLng(-33.53195984623379, -70.68158990214378),
                //orilla inferior
                //LatLng(-33.53195984623379, -70.68158990214378),
                LatLng(-33.53223204605386, -70.68076915737305),
                LatLng(-33.532777272020134, -70.67916778079723),
                LatLng(-33.533494564694436, -70.67701027828703),
                LatLng(-33.53411498491416, -70.67517884481624),
                LatLng(-33.53462465183909, -70.67349599489577),
                LatLng(-33.535123560107664, -70.67156352252277),
                LatLng(-33.535413084641725, -70.67042225980136),
                LatLng(-33.53671784931519, -70.66635266422209),
            )
        )
        
        polygon29.fillColor = Color.argb(100, 255,255,0)
        polygon29.strokeColor = -0xc771c4

        val polygon30 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.51075402864406, -70.67101775133654),
                LatLng(-33.51143130948609, -70.67134993268282),
                LatLng(-33.5118111792849, -70.67155743499119),
                LatLng(-33.51279657631902, -70.67199499422334),
                LatLng(-33.51386846493316, -70.67251826093863),
                LatLng(-33.51576054156057, -70.67343267290029),
                LatLng(-33.51714454431965, -70.67410479996158),
                LatLng(-33.5194311090601, -70.67521899717032),
                LatLng(-33.52128658279642, -70.67609651361816),
                LatLng(-33.52381133197243, -70.67731460094164),
                LatLng(-33.52439045583841, -70.67760329980814),
                LatLng(-33.52613083407418, -70.6784219067253),
                LatLng(-33.52779293975879, -70.67932409070605),
                LatLng(-33.52963976999531, -70.68031782273881),
                LatLng(-33.530835538292145, -70.68097641704067),
                LatLng(-33.53195984623379, -70.68158990214378),
                LatLng(-33.53288309866711, -70.68204528316346),
                LatLng(-33.53467172502063, -70.683032336079),
                LatLng(-33.5364871429311, -70.68400866016114),
                LatLng(-33.53749767924954, -70.68449145778503),
                LatLng(-33.53911460925181, -70.68540514935464),
                //orilla inferior
                //LatLng(-33.53911460925181, -70.68540514935464),
                LatLng(-33.538301060111976, -70.68770173483927),
                LatLng(-33.53772678551506, -70.68914985955716),
                LatLng(-33.53715250710032, -70.69077660758921),
                LatLng(-33.53637616170498, -70.69291370797336),
                LatLng(-33.53552004670982, -70.69524856987442),
                LatLng(-33.53490853080621, -70.69688807671378),
                LatLng(-33.533732170568626, -70.69996295181316),
                LatLng(-33.532812217290406, -70.70257212809999),
                LatLng(-33.53192947834657, -70.70509199272186),
                LatLng(-33.53145088117209, -70.70629132066004),
                //orilla izquierda
                //LatLng(-33.53145088117209, -70.70629132066004),
                LatLng(-33.5300576165596, -70.70603614450279),
                LatLng(-33.528611150140854, -70.70590217701591),
                LatLng(-33.526425450962606, -70.70576820953033),
                LatLng(-33.52509060504823, -70.705653380258),
                LatLng(-33.524207787275586, -70.70545561873112),
                LatLng(-33.52296863628321, -70.70503457806268),
                LatLng(-33.521458230808356, -70.70440939646873),
                LatLng(-33.519947798961844, -70.70375869725025),
                LatLng(-33.51822459802895, -70.70304420399077),
                LatLng(-33.51760232256836, -70.70278264842709),
                LatLng(-33.51616628524763, -70.70210005218735),
                LatLng(-33.51418238705447, -70.70109848574441),
                LatLng(-33.51270906055923, -70.7004031307045),
                LatLng(-33.51149633979525, -70.6997970873259),
                LatLng(-33.5087357409783, -70.69843189483802),
                LatLng(-33.507155936706525, -70.69765998695136),
                LatLng(-33.503214279130134, -70.69573978630918),
                LatLng(-33.50209185797341, -70.69519753697553),
                //orilla superior
                //LatLng(-33.50209185797341, -70.69519753697553),
                LatLng(-33.50255997850935, -70.69367285938442),
                LatLng(-33.50354408731852, -70.69073195407387),
                LatLng(-33.50423561654317, -70.68882451225686),
                LatLng(-33.50486330752115, -70.6869681056793),
                LatLng(-33.505666532550606, -70.68472893484184),
                LatLng(-33.50597505408778, -70.68365719496173),
                LatLng(-33.50645911152348, -70.68249614340586),
                LatLng(-33.50713465969059, -70.68092680999717),
                LatLng(-33.50826233463683, -70.67830487490055),
                LatLng(-33.50835807994785, -70.67672916211055),
                LatLng(-33.509590494986526, -70.67391652861433),
                LatLng(-33.510058574990765, -70.67278099470003),
                LatLng(-33.51046282384549, -70.67180494585291),
            )
        )

        polygon30.fillColor = Color.argb(100,0,100,255)
        polygon30.strokeColor = -0xc771c4

        val polygon31 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.47886703568699, -70.75578565637319),
                LatLng(-33.478875057106485, -70.7617696003731),
                LatLng(-33.4813892523002, -70.76674888579836),
                LatLng(-33.48354060225476, -70.77114901681168),
                LatLng(-33.48560331705275, -70.77506361609),
                LatLng(-33.48990140712173, -70.78499038347253),
                LatLng(-33.49152986732676, -70.78574140201327),
                LatLng(-33.493981447532626, -70.78481872208232),
                LatLng(-33.49700556389504, -70.78389604216085),
                //orilla izquierda
                //LatLng(-33.49700556389504, -70.78389604216085),
                LatLng(-33.495023338599744, -70.77468036062989),
                LatLng(-33.49677696000379, -70.77483056436739),
                LatLng(-33.49859852601766, -70.77503916447864),
                LatLng(-33.49982206696078, -70.77523054659991),
                LatLng(-33.50267338216748, -70.77560055203428),
                LatLng(-33.50386494876141, -70.7757664165372),
                LatLng(-33.504194754466575, -70.77580469296257),
                LatLng(-33.50604589869064, -70.77575365772566),
                LatLng(-33.50680123925898, -70.77568986368533),
                LatLng(-33.50926934839469, -70.7755750344171),
                LatLng(-33.509183966555106, -70.77432611643833),
                LatLng(-33.50898183937409, -70.77207418678383),
                LatLng(-33.50887545645745, -70.77005829510455),
                LatLng(-33.50964859586959, -70.76586714113927),
                LatLng(-33.51014954952643, -70.76313128790639),
                LatLng(-33.51185813751842, -70.76316347443087),
                LatLng(-33.51361141786044, -70.76335659349132),
                LatLng(-33.5140050065168, -70.76336732232761),
                LatLng(-33.51467589214482, -70.76343169534539),
                LatLng(-33.51598186793899, -70.76345315302056),
                LatLng(-33.51743988611985, -70.76339950884359),
                LatLng(-33.51856692627143, -70.76338878001464),
                LatLng(-33.51996228898579, -70.76321711863434),
                LatLng(-33.52108929629619, -70.7631205591058),
                LatLng(-33.52330748996258, -70.76306691492717),
                LatLng(-33.525427242861916, -70.76303472841448),
                LatLng(-33.52750222488934, -70.76290598241678),
                LatLng(-33.52786891914642, -70.76289525357835),
                LatLng(-33.5281282872967, -70.76284160939687),
                LatLng(-33.52909420323031, -70.76225152340554),
                LatLng(-33.529764971828754, -70.76161852206405),
                LatLng(-33.530382074376554, -70.7612644704845),
                //orilla inferior
                //LatLng(-33.530382074376554, -70.7612644704845),
                LatLng(-33.528457616805824, -70.75854673827224),
                LatLng(-33.52753641264175, -70.75702324355154),
                LatLng(-33.52581581140108, -70.75382115539365),
                LatLng(-33.52388389550111, -70.75023772411471),
                LatLng(-33.52086866701959, -70.74473918695622),
                LatLng(-33.518206071048525, -70.73977299102057),
                LatLng(-33.51625609527519, -70.73599644071248),
                LatLng(-33.514896453117785, -70.73332496056084),
                LatLng(-33.513062691389415, -70.72967715628259),
                LatLng(-33.51186401636875, -70.72712369330611),
                LatLng(-33.51092355242914, -70.72522444270338),
                LatLng(-33.509993215935225, -70.72294992943601),
                //orilla derecha
                //LatLng(-33.509993215935225, -70.72294992943601),
                LatLng(-33.50855296373958, -70.72449488184776),
                LatLng(-33.50661171634756, -70.72662992021704),
                LatLng(-33.50585130804669, -70.72807831311702),
                LatLng(-33.50433941751856, -70.73307795068031),
                LatLng(-33.50433941751856, -70.73307795068031),
                LatLng(-33.50216546950748, -70.74050230525482),
                LatLng(-33.501467647471905, -70.74144644284888),
                LatLng(-33.496967454808974, -70.74537319686091),
                LatLng(-33.495911709704025, -70.74612421536975),
                LatLng(-33.49307544942289, -70.74771208310183),
                LatLng(-33.49057910561178, -70.74910683178554),
                LatLng(-33.48920116456001, -70.74996513864873),
                LatLng(-33.48763529589887, -70.75079125904357),
                LatLng(-33.485630942659284, -70.75188560032876),
                LatLng(-33.484521370015315, -70.75252933047766),
                LatLng(-33.481827912822745, -70.75409574055465),
            )
        )

        polygon31.fillColor = Color.argb(100, 255,0,128 )
        polygon31.strokeColor = -0xc771c4

        val polygon32 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.49700556389504, -70.78389604216085),
                LatLng(-33.495023338599744, -70.77468036062989),
                LatLng(-33.49677696000379, -70.77483056436739),
                LatLng(-33.49859852601766, -70.77503916447864),
                LatLng(-33.49982206696078, -70.77523054659991),
                LatLng(-33.50267338216748, -70.77560055203428),
                LatLng(-33.50386494876141, -70.7757664165372),
                LatLng(-33.504194754466575, -70.77580469296257),
                LatLng(-33.50604589869064, -70.77575365772566),
                LatLng(-33.50680123925898, -70.77568986368533),
                LatLng(-33.50926934839469, -70.7755750344171),
                LatLng(-33.509183966555106, -70.77432611643833),
                LatLng(-33.50898183937409, -70.77207418678383),
                LatLng(-33.50887545645745, -70.77005829510455),
                LatLng(-33.50964859586959, -70.76586714113927),
                LatLng(-33.51014954952643, -70.76313128790639),
                LatLng(-33.51185813751842, -70.76316347443087),
                LatLng(-33.51361141786044, -70.76335659349132),
                LatLng(-33.5140050065168, -70.76336732232761),
                LatLng(-33.51467589214482, -70.76343169534539),
                LatLng(-33.51598186793899, -70.76345315302056),
                LatLng(-33.51743988611985, -70.76339950884359),
                LatLng(-33.51856692627143, -70.76338878001464),
                LatLng(-33.51996228898579, -70.76321711863434),
                LatLng(-33.52108929629619, -70.7631205591058),
                LatLng(-33.52330748996258, -70.76306691492717),
                LatLng(-33.525427242861916, -70.76303472841448),
                LatLng(-33.52750222488934, -70.76290598241678),
                LatLng(-33.52786891914642, -70.76289525357835),
                LatLng(-33.5281282872967, -70.76284160939687),
                LatLng(-33.52909420323031, -70.76225152340554),
                LatLng(-33.529764971828754, -70.76161852206405),
                LatLng(-33.530382074376554, -70.7612644704845),
                LatLng(-33.52858881376292, -70.75871559243771),
                LatLng(-33.530270207080584, -70.75660201173255),
                LatLng(-33.53266703039236, -70.75342627624423),
                LatLng(-33.53403533629682, -70.75163456061777),
                LatLng(-33.53573450956454, -70.74942442040444),
                LatLng(-33.53690724119885, -70.74783186593952),
                LatLng(-33.53801614228685, -70.74631910006175),
                LatLng(-33.53914291429315, -70.74481706301296),
                LatLng(-33.541691511063604, -70.74137310662715),
                //orilla inferior
                //LatLng(-33.541691511063604, -70.74137310662715),
                LatLng(-33.54238900867595, -70.74194173493761),
                LatLng(-33.542916599264565, -70.74246744791613),
                LatLng(-33.54458877191236, -70.74395875613028),
                LatLng(-33.54679856775514, -70.74589832291568),
                LatLng(-33.54841486498466, -70.74737834466968),
                LatLng(-33.54982910024955, -70.74857767264264),
                LatLng(-33.54758545309654, -70.7528263557669),
                LatLng(-33.54610737882269, -70.7555567407542),
                LatLng(-33.54519434914144, -70.7571493383845),
                LatLng(-33.54571556749742, -70.75749588582451),
                LatLng(-33.54659719023181, -70.75808230541314),
                LatLng(-33.547215636631705, -70.75847926636384),
                LatLng(-33.54799761723845, -70.75898900031068),
                LatLng(-33.54847319332833, -70.75932280838104),
                LatLng(-33.54884162201742, -70.75957541989492),
                LatLng(-33.549345389315164, -70.75988667336843),
                LatLng(-33.54982471740979, -70.76020243776033),
                LatLng(-33.550271488722814, -70.76044407525227),
                LatLng(-33.55081206439973, -70.76081960367499),
                LatLng(-33.55116612383002, -70.76105857630701),
                LatLng(-33.551485408324275, -70.76128996250401),
                LatLng(-33.54935903189829, -70.76594376136033),
                LatLng(-33.550598177462426, -70.7666787106524),
                LatLng(-33.55193651062461, -70.76754480727469),
                LatLng(-33.55222038346292, -70.767731271949),
                LatLng(-33.551415884938564, -70.76961683646732),
                LatLng(-33.55047800572136, -70.77154699717893),
                LatLng(-33.54875187523001, -70.77524613375869),
                LatLng(-33.547257441933745, -70.77859706295168),
                LatLng(-33.5460386214409, -70.78133103824055),
                LatLng(-33.54530489113815, -70.78293226866612),
                LatLng(-33.546123691212436, -70.78414435542855),
                LatLng(-33.54718705635251, -70.78574558585848),
                LatLng(-33.550036377882684, -70.78988575054157),
                LatLng(-33.55162794125931, -70.79231046744746),
                LatLng(-33.55545472635666, -70.79795383528253),

                //Orilla izquierda
                //LatLng(-33.55545472635666, -70.79795383528253),
                LatLng(-33.55246075528783, -70.80370843476577),
                LatLng(-33.54219259649021, -70.78993147269627),
                LatLng(-33.5342756708853, -70.80109870182764),
                LatLng(-33.529646589266434, -70.79939934084551),
                LatLng(-33.52635802014946, -70.79894415487155),
                LatLng(-33.51322779382441, -70.79930830366473),
                LatLng(-33.50572743101567, -70.79921070604931),
                LatLng(-33.503455549894326, -70.79883178877486),
                LatLng(-33.5028224122234, -70.79857615337976),
                LatLng(-33.501167349856175, -70.79733113946233),
                LatLng(-33.500505316048404, -70.79660939226382),
                LatLng(-33.499647673814664, -70.795057635787),
                LatLng(-33.498850209329426, -70.79227890918068),
                LatLng(-33.49781999624666, -70.78760255006287),
            )
        )

        polygon32.fillColor = Color.argb(50,0,255,0)
        polygon32.strokeColor = -0xc771c4

        val polygon33 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.53195984623379, -70.68158990214378),
                LatLng(-33.53223204605386, -70.68076915737305),
                LatLng(-33.532777272020134, -70.67916778079723),
                LatLng(-33.533494564694436, -70.67701027828703),
                LatLng(-33.53411498491416, -70.67517884481624),
                LatLng(-33.53462465183909, -70.67349599489577),
                LatLng(-33.535123560107664, -70.67156352252277),
                LatLng(-33.535413084641725, -70.67042225980136),
                LatLng(-33.53671784931519, -70.66635266422209),
                LatLng(-33.537285970908385, -70.66446520934497),
                //orilla derecha
                LatLng(-33.538845478936146, -70.66469096707948),
                LatLng(-33.540108869617804, -70.66503158638594),
                LatLng(-33.542451061854955, -70.66574688693706),
                LatLng(-33.545431941991225, -70.66816528404796),
                LatLng(-33.54720622662235, -70.66957885417344),
                LatLng(-33.5490507591232, -70.67101095775844),
                //orilla inferior
                //LatLng(-33.5490507591232, -70.67101095775844),
                LatLng(-33.54857903988936, -70.67392529390578),
                LatLng(-33.548137428063306, -70.67659877583432),
                LatLng(-33.54791662129115, -70.67794755945081),
                LatLng(-33.547434859125964, -70.68081372474532),
                LatLng(-33.54697316786245, -70.68359559103186),
                LatLng(-33.54650143728338, -70.68625703023923),
                //orilla izquierda
                LatLng(-33.542597015109486, -70.68445062349265),
                LatLng(-33.54185425160366, -70.68741313051729),
                LatLng(-33.54068990675571, -70.68622090208135),
                LatLng(-33.53983671305909, -70.68575123633727),
            )
        )

        polygon33.fillColor = Color.argb(100,255,0,0)
        polygon33.strokeColor = -0xc771c4

        val polygon34 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.538845478936146, -70.66469096707948),
                LatLng(-33.540108869617804, -70.66503158638594),
                LatLng(-33.542451061854955, -70.66574688693706),
                LatLng(-33.545431941991225, -70.66816528404796),
                //orilla inferior
                //LatLng(-33.545431941991225, -70.66816528404796),
                LatLng(-33.54572443156222, -70.66799145704096),
                LatLng(-33.54634188463765, -70.66674819652981),
                LatLng(-33.5469779849258, -70.66315386958256),
                LatLng(-33.54761672015307, -70.65915159265766),
                LatLng(-33.54799995902526, -70.6568098349006),
                LatLng(-33.54819867480373, -70.6568268658665),
                LatLng(-33.54873847505818, -70.6534829078001),
                LatLng(-33.54996118084416, -70.65380465302076),
                LatLng(-33.5510353613683, -70.65413404241603),
                LatLng(-33.55107116715537, -70.65406959666424),
                LatLng(-33.5513814833683, -70.65418416688756),
                LatLng(-33.55214731942325, -70.65465158165539),
                LatLng(-33.554945631494355, -70.65491007483514),
                LatLng(-33.555783347105056, -70.6490336438723),
                //orilla derecha
                //LatLng(-33.555783347105056, -70.6490336438723),
                LatLng(-33.55375548668808, -70.64856332525959),
                LatLng(-33.550805447113795, -70.64793743450433),
                LatLng(-33.550332397359576, -70.64774364548038),
                LatLng(-33.54863351107526, -70.6474325092412),
                LatLng(-33.54669316325018, -70.64700872021234),
                LatLng(-33.54395245947107, -70.64645082073947),
                LatLng(-33.54200411331435, -70.64669448778425),
                LatLng(-33.54072267987376, -70.646464829232),
                //orilla superior
                //LatLng(-33.54072267987376, -70.646464829232),
                LatLng(-33.54001782848759, -70.65113188719867),
                LatLng(-33.53870187017175, -70.65838544641196),
                LatLng(-33.538338437972136, -70.66065400431427),
                LatLng(-33.53724805275553, -70.66450632991254),
            ))

        polygon34.fillColor = Color.argb(50,0,0,230)
        polygon34.strokeColor = -0xc771c4

        val polygon35 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.54072267987376, -70.646464829232),
                LatLng(-33.54200411331435, -70.64669448778425),
                LatLng(-33.54395245947107, -70.64645082073947),
                LatLng(-33.54669316325018, -70.64700872021234),
                LatLng(-33.54863351107526, -70.6474325092412),
                LatLng(-33.550332397359576, -70.64774364548038),
                LatLng(-33.550805447113795, -70.64793743450433),
                LatLng(-33.55375548668808, -70.64856332525959),
                LatLng(-33.555783347105056, -70.6490336438723),
                //orilla inferior
                //LatLng(-33.54072267987376, -70.646464829232),
                LatLng(-33.55650198773857, -70.64495893758269),
                LatLng(-33.55685602385908, -70.64187884590822),
                LatLng(-33.55703415703133, -70.64049214381274),
                //orilla derecha
                //LatLng(-33.55703415703133, -70.64049214381274),
                LatLng(-33.55526068531273, -70.64015172294329),
                LatLng(-33.555117836012265, -70.6410178195815),
                LatLng(-33.55302017956818, -70.64055770575517),
                LatLng(-33.55025330033312, -70.6400434608922),
                LatLng(-33.54838109537945, -70.6396464999354),
                LatLng(-33.54839613333161, -70.63940291025591),
                LatLng(-33.54507268222228, -70.63872627227501),
                LatLng(-33.545012528084435, -70.63898790563447),
                LatLng(-33.5418468574395, -70.63839246421087),
                //orilla superior
                //LatLng(-33.5418468574395, -70.63839246421087),
                LatLng(-33.541252811299955, -70.64302968988612),

            )
        )
        polygon35.fillColor = Color.argb(100,255,125,0)
        polygon35.strokeColor = -0xc771c4

        val polygon36 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.55703415703133, -70.64049214381274),
                LatLng(-33.55526068531273, -70.64015172294329),
                LatLng(-33.555117836012265, -70.6410178195815),
                LatLng(-33.55302017956818, -70.64055770575517),
                LatLng(-33.55025330033312, -70.6400434608922),
                LatLng(-33.54838109537945, -70.6396464999354),
                LatLng(-33.54839613333161, -70.63940291025591),
                LatLng(-33.54507268222228, -70.63872627227501),
                LatLng(-33.545012528084435, -70.63898790563447),
                LatLng(-33.5418468574395, -70.63839246421087),
                //Orilla superior
                //LatLng(-33.5418468574395, -70.63839246421087),
                LatLng(-33.54233175144734, -70.6342144824438),
                //orilla derecha
                //LatLng(-33.54233175144734, -70.6342144824438),
                LatLng(-33.54520277254204, -70.63368249923299),
                LatLng(-33.54755486426228, -70.63327283186274),
                LatLng(-33.5513990053566, -70.63259005291049),
                LatLng(-33.5538394403933, -70.63196796539133),
                LatLng(-33.55797786754834, -70.63107931998518),
                //orilla inferior
                //LatLng(-33.55797786754834, -70.63107931998518),
                LatLng(-33.558169247974945, -70.63216381870421),
                LatLng(-33.55788364954842, -70.63447973810439)
            )
        )

        polygon36.fillColor = Color.argb(100,255,0,0)
        polygon36.strokeColor = -0xc771c4

        val polygon37 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                LatLng(-33.55797786754834, -70.63107931998518),
                LatLng(-33.558169247974945, -70.63216381870421),
                LatLng(-33.55788364954842, -70.63447973810439),
                LatLng(-33.55649714851069, -70.644541783197),
                LatLng(-33.55646898681493, -70.64494054904306),
                LatLng(-33.55636760462404, -70.64555559467901),
                //orilla izquierda
                LatLng(-33.55788832501598, -70.64590029056727),
                LatLng(-33.55911614640575, -70.64611657034328),
                LatLng(-33.560364889419176, -70.64635083125548),
                LatLng(-33.56109766386128, -70.64650854330223),
                LatLng(-33.5621450426284, -70.64662802212867),
                LatLng(-33.56312072624755, -70.64665669704759),
                LatLng(-33.5637180781189, -70.6467952924878),
                //orilla inferior
                //LatLng(-33.5637180781189, -70.6467952924878),
                LatLng(-33.564060558002765, -70.64421932897362),
                LatLng(-33.56461438435938, -70.63983390015161),
                LatLng(-33.565797215846054, -70.6303564175626),
                //orilla derecha
                //LatLng(-33.565797215846054, -70.6303564175626),
                LatLng(-33.55972468091972, -70.63070339757014)
            )

        )

        polygon37.fillColor = Color.argb(100, 0, 255,0)
        polygon37.strokeColor = -0xc771c4
        
        val polygon38 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.55636760462404, -70.64555559467901),
                LatLng(-33.55788832501598, -70.64590029056727),
                LatLng(-33.55911614640575, -70.64611657034328),
                LatLng(-33.560364889419176, -70.64635083125548),
                LatLng(-33.56109766386128, -70.64650854330223),
                LatLng(-33.5621450426284, -70.64662802212867),
                LatLng(-33.56312072624755, -70.64665669704759),
                LatLng(-33.5637180781189, -70.6467952924878),
                //orilla inferior
                //LatLng(-33.5637180781189, -70.6467952924878),
                LatLng(-33.56315931607766, -70.65085771137181),
                LatLng(-33.562648111600474, -70.65474612429986),
                LatLng(-33.562377472712214, -70.65682114743568),
                //orilla izquierda
                //LatLng(-33.562377472712214, -70.65682114743568),
                LatLng(-33.561204694406676, -70.6565234267217),
                LatLng(-33.55971614513872, -70.65620766232594),
                LatLng(-33.55812231645864, -70.65578363586111),
                LatLng(-33.55678238289264, -70.6554252189084),
                LatLng(-33.55496422251862, -70.65488934895312),
                //orilla superior
                //LatLng(-33.55496422251862, -70.65488934895312),
                LatLng(-33.555767659311684, -70.64906003057442),
            )
        )

        polygon38.fillColor = Color.argb(100,255,255,0)
        polygon38.strokeColor = -0xc771c4
        //map.setOnPolygonClickListener()

        val polygon39 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.545431941991225, -70.66816528404796),
                LatLng(-33.54572443156222, -70.66799145704096),
                LatLng(-33.54634188463765, -70.66674819652981),
                LatLng(-33.5469779849258, -70.66315386958256),
                LatLng(-33.54761672015307, -70.65915159265766),
                LatLng(-33.54799995902526, -70.6568098349006),
                LatLng(-33.54819867480373, -70.6568268658665),
                LatLng(-33.54873847505818, -70.6534829078001),
                LatLng(-33.54996118084416, -70.65380465302076),
                LatLng(-33.5510353613683, -70.65413404241603),
                LatLng(-33.55107116715537, -70.65406959666424),
                LatLng(-33.5513814833683, -70.65418416688756),
                LatLng(-33.55214731942325, -70.65465158165539),
                LatLng(-33.554945631494355, -70.65491007483514),
                //orilla izquierda
                LatLng(-33.55496422251862, -70.65488934895312),
                LatLng(-33.55678238289264, -70.6554252189084),
                LatLng(-33.55812231645864, -70.65578363586111),
                LatLng(-33.562377472712214, -70.65682114743568),
                //orilla inferior
                //LatLng(-33.562377472712214, -70.65682114743568),
                LatLng(-33.5615512410612, -70.66219182447007),
                LatLng(-33.56026406551597, -70.6704978085844),
                LatLng(-33.5590077478107, -70.67874561685657),
                //orilla izquierda
                //LatLng(-33.5590077478107, -70.67874561685657),
                LatLng(-33.55347284141725, -70.67455436052605),
                LatLng(-33.54717319309366, -70.6695026678264),
            )
        )

        polygon39.fillColor = Color.argb(100,0,255,0)
        polygon39.strokeColor = -0xc771c4

        val polygon40 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.5590077478107, -70.67874561685657),
                LatLng(-33.55347284141725, -70.67455436052605),
                LatLng(-33.54909149006993, -70.67102722979222),
                //orilla superior
                //LatLng(-33.54909149006993, -70.67102722979222),
                LatLng(-33.54857826060461, -70.67390580666977),
                LatLng(-33.54840519415885, -70.6749154567556),
                LatLng(-33.54795163909117, -70.6778369974507),
                LatLng(-33.54764727843163, -70.67959851460934),
                LatLng(-33.54650740840479, -70.68625790884009),
                LatLng(-33.54571366803007, -70.68595000135217),
                LatLng(-33.54416794218964, -70.68519813424923),
                LatLng(-33.543177230783925, -70.68471837143531),
                LatLng(-33.543177230783925, -70.68471837143531),
                LatLng(-33.542574442966554, -70.68443910650414),
                LatLng(-33.542067143052805, -70.6866732259),
                //orilla izquierda
                //LatLng(-33.542067143052805, -70.6866732259),
                LatLng(-33.54400826822888, -70.68754346990579),
                LatLng(-33.545004938164816, -70.68802323271353),
                LatLng(-33.547791972115384, -70.68928350516846),
                LatLng(-33.550948910538345, -70.69074427552182),
                LatLng(-33.55095487818728, -70.69072995423157),
                LatLng(-33.5502329069787, -70.69301093540491),
                LatLng(-33.55584484648821, -70.69544814572444),
            )
        )
        polygon40.fillColor = Color.argb(100,255,125,0)
        polygon40.strokeColor = -0xc771c4

        val polygon41 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.55584484648821, -70.69544814572444),
                LatLng(-33.554409070776444, -70.70961926325566),
                LatLng(-33.559040383016125, -70.71049902776525),
                LatLng(-33.558021158011755, -70.71755860191801),
                //orilla izquierda
                //LatLng(-33.558021158011755, -70.71755860191801),
                LatLng(-33.56742618133074, -70.71712944844158),
                LatLng(-33.567193750158594, -70.71163628432933),
                LatLng(-33.57400551261028, -70.71275208334674),
                //orilla inferior
                //LatLng(-33.57400551261028, -70.71275208334674),
                LatLng(-33.57302222052912, -70.7070658001812),
                LatLng(-33.57301084933551, -70.70085850973794),
                //orilla derecha
                //LatLng(-33.57301084933551, -70.70085850973794),
                LatLng(-33.56384468874635, -70.69671884171616),
                LatLng(-33.5637056145605, -70.69718920055591),
                //LatLng(-33.55584484648821, -70.69544814572444),
            ))
        polygon41.fillColor = Color.argb(100,200,0,200)
        polygon41.strokeColor = -0xc771c4

        val polygon42 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.5589823628411, -70.67879338672678),
                LatLng(-33.55940337285084, -70.67612292215797),
                LatLng(-33.55977927290484, -70.67332615176375),
                LatLng(-33.56026042258668, -70.67052938136949),
                //orilla derecha
                //LatLng(-33.56026042258668, -70.67052938136949),
                LatLng(-33.56119498288367, -70.67080939689708),
                LatLng(-33.5637961238238, -70.67154918776961),
                LatLng(-33.56594614072086, -70.6721085418385),
                LatLng(-33.568321621607204, -70.67274007063641),
                LatLng(-33.57131343176485, -70.67355203622041),
                //orilla inferior
                //LatLng(-33.57131343176485, -70.67355203622041),
                LatLng(-33.57053166224573, -70.67595184565546),
                LatLng(-33.56914851420996, -70.68037254719776),
                LatLng(-33.56866741400953, -70.68174386674917),
                LatLng(-33.56738947868937, -70.68553303954137),
                //orilla izquierda
                //LatLng(-33.56738947868937, -70.68553303954137),
                LatLng(-33.56608145466989, -70.68443237506125),
                LatLng(-33.563314993838794, -70.68219495876771),
                LatLng(-33.55898980483896, -70.67878007894315),
                //orilla superior
                //LatLng(-33.55898980483896, -70.67878007894315),
            )
        )

        polygon42.fillColor = Color.argb(100,0,0,240)
        polygon42.strokeColor = -0xc771c4

        val polygon43 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.57131343176485, -70.67355203622041),
                LatLng(-33.568321621607204, -70.67274007063641),
                LatLng(-33.56594614072086, -70.6721085418385),
                LatLng(-33.5637961238238, -70.67154918776961),
                LatLng(-33.56119498288367, -70.67080939689708),
                LatLng(-33.56026042258668, -70.67052938136949),
                //orilla superior
                //LatLng(-33.56026042258668, -70.67052938136949),
                LatLng(-33.56045507853574, -70.66905077679935),
                LatLng(-33.560937119497794, -70.66614085128042),
                LatLng(-33.56139228514867, -70.66325800683188),
                LatLng(-33.561980203895416, -70.65957858698364),
                LatLng(-33.56241377807511, -70.65682251412815),
                //orilla derecha
                //LatLng(-33.56241377807511, -70.65682251412815),
                LatLng(-33.56541117920456, -70.65760870080824),
                LatLng(-33.56719913231471, -70.65814514262685),
                LatLng(-33.57032796112564, -70.65904636487053),
                LatLng(-33.573724846911524, -70.66014070615104),
                //orilla inferior
                //LatLng(-33.573724846911524, -70.66014070615104),
                LatLng(-33.57265216060329, -70.66649217716788),

            )
        )

        polygon43.fillColor = Color.argb(100,255,50,50)
        polygon43.strokeColor = -0xc771c4

        val polygon44 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla izquierda
                LatLng(-33.56241377807511, -70.65682251412815),
                LatLng(-33.56541117920456, -70.65760870080824),
                LatLng(-33.56719913231471, -70.65814514262685),
                LatLng(-33.57032796112564, -70.65904636487053),
                LatLng(-33.573724846911524, -70.66014070615104),
                LatLng(-33.57430330547654, -70.66035069573547),
                LatLng(-33.57717289329211, -70.66106382044038),
                //orilla inferior
                //LatLng(-33.57717289329211, -70.66106382044038),
                LatLng(-33.57743835697524, -70.65931894088787),
                LatLng(-33.57789343563578, -70.65555607023009),
                LatLng(-33.578982070669014, -70.64756385798024),
                LatLng(-33.57903233697465, -70.64707898436077),
                LatLng(-33.57949568956781, -70.64199092416659),
                LatLng(-33.57881308336013, -70.64061019342986),
                LatLng(-33.57906590110552, -70.6374390645441),
                LatLng(-33.57960945675598, -70.6312940540101),
                LatLng(-33.579773786857295, -70.62924571712728),
                //orilla derecha
                //LatLng(-33.579773786857295, -70.62924571712728),
                LatLng(-33.57848287125116, -70.62938063475926),
                LatLng(-33.577143514506226, -70.62946994641848),
                //orilla superior
                //LatLng(-33.577143514506226, -70.62946994641848),
                LatLng(-33.57703721545605, -70.63096272696569),
                LatLng(-33.57663327789268, -70.63517313375725),
                LatLng(-33.57661201796087, -70.63604073270294),
                LatLng(-33.57648445830618, -70.63702316095429),
                LatLng(-33.57623996843028, -70.63863077078442),
                LatLng(-33.57538540251454, -70.64851230674682),
                LatLng(-33.574456270561754, -70.65518981948024),
                LatLng(-33.56297191590945, -70.65217690332692),
            )
        )
        polygon44.fillColor = Color.argb(100,200,50,255)
        polygon44.strokeColor = -0xc771c4

        val polygon45 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.578958536689534, -70.64755548482194),
                LatLng(-33.57903233697465, -70.64707898436077),
                LatLng(-33.57949568956781, -70.64199092416659),
                LatLng(-33.57881308336013, -70.64061019342986),
                LatLng(-33.57906590110552, -70.6374390645441),
                LatLng(-33.57960945675598, -70.6312940540101),
                LatLng(-33.579773786857295, -70.62924571712728),
                LatLng(-33.58340882791885, -70.62890640855564),
                LatLng(-33.582834853231056, -70.62490014278191),
                LatLng(-33.58184633223982, -70.61767865735388),
                LatLng(-33.58139989968751, -70.61419550274655),
                LatLng(-33.58095346483824, -70.6111078710994),
                //orilla derecha
                //LatLng(-33.58095346483824, -70.6111078710994),
                LatLng(-33.58320268044627, -70.61071570714005),
                LatLng(-33.58520093785755, -70.61033294290688),
                LatLng(-33.58727354958368, -70.60963120847508),
                LatLng(-33.59103600972889, -70.60811291025492),
                LatLng(-33.59310359794888, -70.60765242810596),
                LatLng(-33.59430114627445, -70.60750222439783),
                //orilla inferior
                //LatLng(-33.59430114627445, -70.60750222439783),
                LatLng(-33.59441732546585, -70.60859656566947),
                LatLng(-33.59464968334979, -70.6111285710354),
                LatLng(-33.59481948294965, -70.61242676017848),
                LatLng(-33.594980345421725, -70.6143364929888),
                LatLng(-33.595256433126956, -70.61632041108783),
                LatLng(-33.59571220703321, -70.6209981836581),
                LatLng(-33.595828384310664, -70.62167410032558),
                LatLng(-33.596114358478246, -70.62459234372311),
                LatLng(-33.59621266187925, -70.62532190453965),
                LatLng(-33.59649863477187, -70.6274891294715),
                LatLng(-33.59879999769678, -70.627257336882),
                LatLng(-33.60007789339371, -70.62720369269505),
                LatLng(-33.60001533950065, -70.62829803399728),
                LatLng(-33.599774059770965, -70.63049744538954),
                LatLng(-33.59964001518214, -70.63308309488626),
                LatLng(-33.59946128874341, -70.63446711474317),
                LatLng(-33.599037848598975, -70.640179670981),
                LatLng(-33.59842146873683, -70.6492894600249),
                LatLng(-33.5981516504635, -70.65237303475705),
                //orilla izquierda
                //LatLng(-33.5981516504635, -70.65237303475705),
                LatLng(-33.59654976808395, -70.65217743383131),
                LatLng(-33.59379981741526, -70.65183747799618),
                LatLng(-33.5925865523468, -70.65170164045975),
                LatLng(-33.590912508754805, -70.65148449564259),
                LatLng(-33.59101585181313, -70.65028660086811),
                LatLng(-33.58825999648176, -70.64995875477953),
                LatLng(-33.58676770405176, -70.64957797875068),
                LatLng(-33.58541836676614, -70.64921067605461),
                LatLng(-33.58418939809516, -70.64882530928583),
                LatLng(-33.58199911123971, -70.64827480502342),
                LatLng(-33.580383827652675, -70.64788341689474),
                LatLng(-33.57941062976493, -70.64764858402326),
            )
        )
        polygon45.fillColor = Color.argb(100,250,180,0)
        polygon45.strokeColor = -0xc771c4

        val polygon46 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.5981516504635, -70.65237303475705),
                LatLng(-33.59654976808395, -70.65217743383131),
                LatLng(-33.59379981741526, -70.65183747799618),
                LatLng(-33.5925865523468, -70.65170164045975),
                LatLng(-33.590912508754805, -70.65148449564259),
                LatLng(-33.59101585181313, -70.65028660086811),
                LatLng(-33.58825999648176, -70.64995875477953),
                LatLng(-33.58676770405176, -70.64957797875068),
                LatLng(-33.58541836676614, -70.64921067605461),
                LatLng(-33.58418939809516, -70.64882530928583),
                LatLng(-33.58199911123971, -70.64827480502342),
                LatLng(-33.580383827652675, -70.64788341689474),
                LatLng(-33.57941062976493, -70.64764858402326),
                LatLng(-33.578958536689534, -70.64755548482194),
                //orilla superior
                //LatLng(-33.578958536689534, -70.64755548482194),
                LatLng(-33.57890475042705, -70.64783321349405),
                LatLng(-33.578854584997536, -70.64834502874261),
                LatLng(-33.578513459300105, -70.65094625444372),
                LatLng(-33.578282697029756, -70.65258406317592),
                LatLng(-33.57808705027501, -70.65412553024136),
                LatLng(-33.57788136987726, -70.6555104420629),
                LatLng(-33.577565323434094, -70.65799726200058),
                LatLng(-33.57737970831291, -70.6595507717666),
                LatLng(-33.57715897630082, -70.66105383214548),
                //orilla izquierda
                //LatLng(-33.57715897630082, -70.66105383214548),
                LatLng(-33.57772252015526, -70.66121860138368),
                LatLng(-33.58017762291027, -70.66187683512996),
                LatLng(-33.5823964357725, -70.66249456217692),
                LatLng(-33.58519198799258, -70.66325583128629),
                LatLng(-33.58648783870936, -70.66364801416265),
                LatLng(-33.58979048606781, -70.66467587145868),
                LatLng(-33.59074793490425, -70.6650100516637),
                LatLng(-33.592555453565566, -70.6656052888745),
                LatLng(-33.5942636150854, -70.66618757255917),
                LatLng(-33.59537706510946, -70.66654200611639),
                LatLng(-33.59657063383666, -70.66695213637254),
                //orilla inferior
                //LatLng(-33.59657063383666, -70.66695213637254),
                LatLng(-33.59739454041785, -70.65976007192364),
            )
        )
        polygon46.fillColor = Color.argb(100,30,255,0)
        polygon46.strokeColor = -0xc771c4

        val polygon47 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.573724846911524, -70.66014070615104),
                LatLng(-33.57265216060329, -70.66649217716788),
                LatLng(-33.57131343176485, -70.67355203622041),
                //orilla izquierda
                LatLng(-33.57516895443248, -70.67459302733509),
                LatLng(-33.573820711242284, -70.67422763193946),
                LatLng(-33.57564027244013, -70.67474218729528),
                LatLng(-33.576964544853, -70.67508669146851),
                LatLng(-33.578187525623086, -70.67544593426084),
                LatLng(-33.57935680597231, -70.67577532365962),
                //orilla inferior
                //LatLng(-33.57935680597231, -70.67577532365962),
                LatLng(-33.579655089236255, -70.67411405535658),
                LatLng(-33.58002854562627, -70.67232525722305),
                LatLng(-33.58039194373623, -70.67063467339135),
                LatLng(-33.58099413667521, -70.66770095480973),
                LatLng(-33.58161459031447, -70.66455911415545),
                LatLng(-33.58189537442934, -70.66316219378315),
                LatLng(-33.581973410760945, -70.66288544058146),
                LatLng(-33.582062088324854, -70.66240005804312),
                //orilla derecha
                //LatLng(-33.582062088324854, -70.66240005804312),
                LatLng(-33.581467449172585, -70.6622368743545),
                LatLng(-33.57890223324455, -70.66154229236666),
                LatLng(-33.57566875940279, -70.66066153376316),
                LatLng(-33.574911082875545, -70.66046819651447),
            )
        )
        polygon47.fillColor = Color.argb(100,255,255,0)
        polygon47.strokeColor = -0xc771c4

        val polygon48 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.57131343176485, -70.67355203622041),
                LatLng(-33.57053166224573, -70.67595184565546),
                LatLng(-33.56914851420996, -70.68037254719776),
                LatLng(-33.56866741400953, -70.68174386674917),
                LatLng(-33.56738947868937, -70.68553303954137),
                //orilla izquierda
                //LatLng(-33.56738947868937, -70.68553303954137),
                LatLng(-33.568300547794344, -70.68636575335347),
                LatLng(-33.569528887686836, -70.68715188786658),
                LatLng(-33.57047085355803, -70.68784615068647),
                LatLng(-33.571342827429085, -70.68853181053922),
                //orilla inferior
                //LatLng(-33.571342827429085, -70.68853181053922),
                LatLng(-33.57209124765706, -70.68669623516853),
                LatLng(-33.57237060316911, -70.68591770860196),
                LatLng(-33.5726658810186, -70.685688393643),
                LatLng(-33.57308708444387, -70.68470338167221),
                LatLng(-33.57346052081231, -70.68382260376951),
                LatLng(-33.57442050874749, -70.68120886147162),
                LatLng(-33.57523913777271, -70.67894180622453),
                LatLng(-33.57620870550188, -70.67629447484417),
                LatLng(-33.576682771235916, -70.67499169301655),
                //orilla derecha
                //LatLng(-33.576682771235916, -70.67499169301655),
                LatLng(-33.57579642178811, -70.67476599412085),
                LatLng(-33.5747375371825, -70.67448989644642),
                LatLng(-33.57382104381677, -70.67423132878382),
                LatLng(-33.572414581350664, -70.67388680269903),
            )
        )
        polygon48.fillColor = Color.argb(100,150,150,150)
        polygon48.strokeColor = -0xc771c4

        val polygon49 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.576682771235916, -70.67499169301655),
                LatLng(-33.57620870550188, -70.67629447484417),
                LatLng(-33.57523913777271, -70.67894180622453),
                LatLng(-33.57442050874749, -70.68120886147162),
                LatLng(-33.57346052081231, -70.68382260376951),
                LatLng(-33.57308708444387, -70.68470338167221),
                LatLng(-33.5726658810186, -70.685688393643),
                LatLng(-33.57237060316911, -70.68591770860196),
                LatLng(-33.57209124765706, -70.68669623516853),
                LatLng(-33.571342827429085, -70.68853181053922),
                LatLng(-33.56658387837932, -70.69790856304951),
                //orilla izquierda
                //LatLng(-33.56658387837932, -70.69790856304951),
                LatLng(-33.56778727919099, -70.69840844979718),
                LatLng(-33.569248235908844, -70.69908821266478),
                LatLng(-33.56978866818157, -70.6993774578489),
                LatLng(-33.570920643719056, -70.6998420031465),
                LatLng(-33.57182621347283, -70.70023642839348),
                LatLng(-33.573001979366055, -70.70081491876763),
                //orilla inferior
                //LatLng(-33.573001979366055, -70.70081491876763),
                LatLng(-33.57342599982764, -70.69964745819502),
                LatLng(-33.57408977917071, -70.69810148296546),
                LatLng(-33.575207093002234, -70.69520903112313),
                LatLng(-33.57682510937355, -70.69586178440267),
                LatLng(-33.57738278411451, -70.69427710932466),
                LatLng(-33.575916301180804, -70.69335983567218),
                LatLng(-33.576396525765915, -70.69220822293462),
                LatLng(-33.577815894575686, -70.6885273098801),
                LatLng(-33.57892103786667, -70.68576353937256),
                LatLng(-33.58002602759886, -70.68293114711132),
                LatLng(-33.581203290808716, -70.67996240335977),
                LatLng(-33.581832769832744, -70.67842492051432),
                LatLng(-33.582488514345904, -70.67675771368187),
                //orilla derecha
                //LatLng(-33.582488514345904, -70.67675771368187),
                LatLng(-33.582018650659585, -70.67663375779381),
                LatLng(-33.58071059317154, -70.67623337660528),
                LatLng(-33.579993882994785, -70.67599502563178),
                LatLng(-33.579114173671705, -70.67573951147487),
                LatLng(-33.577976899817344, -70.6753879100886),
                LatLng(-33.57748737381469, -70.6752491203265),
            )
        )

        polygon49.fillColor = Color.argb(100,255,0,50)
        polygon49.strokeColor = -0xc771c4

        val polygon50 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.57935680597231, -70.67577532365962),
                LatLng(-33.579655089236255, -70.67411405535658),
                LatLng(-33.58002854562627, -70.67232525722305),
                LatLng(-33.58039194373623, -70.67063467339135),
                LatLng(-33.58099413667521, -70.66770095480973),
                LatLng(-33.58161459031447, -70.66455911415545),
                LatLng(-33.58189537442934, -70.66316219378315),
                LatLng(-33.581973410760945, -70.66288544058146),
                LatLng(-33.582062088324854, -70.66240005804312),
                //orilla derecha
                LatLng(-33.5823964357725, -70.66249456217692),
                LatLng(-33.58519198799258, -70.66325583128629),
                LatLng(-33.58648783870936, -70.66364801416265),
                LatLng(-33.58979048606781, -70.66467587145868),
                LatLng(-33.59074793490425, -70.6650100516637),
                //orilla inferior
                //LatLng(-33.59074793490425, -70.6650100516637),
                LatLng(-33.5903368524027, -70.66708931603448),
                LatLng(-33.58948780695378, -70.67112335846319),
                LatLng(-33.588808564560466, -70.67457804363791),
                LatLng(-33.58890546341463, -70.67396055767468),
                LatLng(-33.58863371529931, -70.67521231910305),
                LatLng(-33.589480555440815, -70.67541715279134),
                LatLng(-33.58922144851891, -70.67663856848814),
                LatLng(-33.5883935650896, -70.67637304333667),
                LatLng(-33.58803965828271, -70.6782013736125),
                //orilla izquierda
                //LatLng(-33.58803965828271, -70.6782013736125)
                //inicio de cuadro 52
                LatLng(-33.58247676167266, -70.67675007272983),
            )
        )

        polygon50.fillColor = Color.argb(100,0,0,255)
        polygon50.strokeColor = -0xc771c4

        val polygon51 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.59074793490425, -70.6650100516637),
                LatLng(-33.5903368524027, -70.66708931603448),
                LatLng(-33.58948780695378, -70.67112335846319),
                LatLng(-33.588808564560466, -70.67457804363791),
                LatLng(-33.58890546341463, -70.67396055767468),
                LatLng(-33.58863371529931, -70.67521231910305),
                LatLng(-33.589480555440815, -70.67541715279134),
                LatLng(-33.58922144851891, -70.67663856848814),
                LatLng(-33.5883935650896, -70.67637304333667),
                LatLng(-33.58803965828271, -70.6782013736125),
                //orilla izquierda
                //LatLng(-33.58803965828271, -70.6782013736125),
                //toda es compartida por la 52
                LatLng(-33.58908062662909, -70.67849375035257),
                LatLng(-33.59088272035443, -70.67898676352144),
                LatLng(-33.59269009814151, -70.67947229521978),
                LatLng(-33.593602914424146, -70.67972601932729),
                LatLng(-33.59554305761339, -70.68021837167143),
                LatLng(-33.59628455546069, -70.68045263363324),
                LatLng(-33.597669079462584, -70.68073231317548),
                LatLng(-33.598758130364885, -70.68104863980716),
                //orilla inferior
                //LatLng(-33.598758130364885, -70.68104863980716),
                LatLng(-33.59931771592088, -70.67920680275886),
                LatLng(-33.599851120647784, -70.67751626422839),
                LatLng(-33.60054528613231, -70.67530522183957),
                LatLng(-33.601285174608975, -70.67230682674777),
                LatLng(-33.60187496686815, -70.66976144449377),
                LatLng(-33.6021193840964, -70.66874711922127),
                //orilla derecha
                //LatLng(-33.6021193840964, -70.66874711922127),
                LatLng(-33.59657063383666, -70.66695213637254),
                LatLng(-33.59537706510946, -70.66654200611639),
                LatLng(-33.5942636150854, -70.66618757255917),
            )
        )

        polygon51.fillColor = Color.argb(100,255,0,30)
        polygon51.strokeColor = -0xc771c4

        val polygon52 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla derecha
                LatLng(-33.60377820230097, -70.68224788206687),
                LatLng(-33.60317706798396, -70.68209451079366),
                LatLng(-33.60212882999405, -70.68184641020022),
                LatLng(-33.59971216591455, -70.68121707906633),
                LatLng(-33.598758130364885, -70.68104863980716),
                LatLng(-33.598758130364885, -70.68104863980716),
                LatLng(-33.597669079462584, -70.68073231317548),
                LatLng(-33.59628455546069, -70.68045263363324),
                LatLng(-33.59554305761339, -70.68021837167143),
                LatLng(-33.593602914424146, -70.67972601932729),
                LatLng(-33.59269009814151, -70.67947229521978),
                LatLng(-33.59088272035443, -70.67898676352144),
                LatLng(-33.58908062662909, -70.67849375035257),
                LatLng(-33.58803965828271, -70.6782013736125),
                LatLng(-33.582458898081164, -70.67673358904977),
                //orilla top
                //LatLng(-33.582458898081164, -70.67673358904977),
                LatLng(-33.581959601955035, -70.67807638767296),
                LatLng(-33.58116957052124, -70.68003368731921),
                LatLng(-33.580840915311285, -70.68086819490102),
                LatLng(-33.58003823291736, -70.68292411818663),
                LatLng(-33.579115454949196, -70.6852834987609),
                //orilla izquierda
                //LatLng(-33.579115454949196, -70.6852834987609),
                LatLng(-33.57999010962411, -70.68572194786894),
                LatLng(-33.581132778409426, -70.68634712946572),
                LatLng(-33.58202564395347, -70.68683834357776),
                LatLng(-33.58309458883398, -70.68745899700504),
                LatLng(-33.5844285387434, -70.68821176669081),
                LatLng(-33.58589001397176, -70.68916867731438),
                LatLng(-33.588244265655604, -70.69068059608614),
                LatLng(-33.58757997993642, -70.69211596203418),
                LatLng(-33.58801043766948, -70.69236475879156),
                LatLng(-33.588302722562844, -70.69207130619701),
                LatLng(-33.590423086954836, -70.69193095930154),
                LatLng(-33.590837588202724, -70.6919437181019),
                LatLng(-33.59242117712273, -70.69226268830899),
                LatLng(-33.592591225230784, -70.69260079671066),
                LatLng(-33.59234146698614, -70.69395960978517),
                LatLng(-33.5913763700776, -70.69758728918906),
                LatLng(-33.59285705143624, -70.69746709549479),
                LatLng(-33.59596918127028, -70.69725065661882),
                LatLng(-33.59682586800499, -70.697178481895),
                LatLng(-33.597599884657534, -70.69729576581396),
                //orilla inferior
                //LatLng(-33.597599884657534, -70.69729576581396),
                LatLng(-33.59800292087365, -70.69630562841932),
                LatLng(-33.59841365796306, -70.69539525647374),
                LatLng(-33.599538435700005, -70.6930965673658),
                LatLng(-33.60036621217735, -70.6915185893463),
                LatLng(-33.60133237505482, -70.68934442209247),
                LatLng(-33.603101621823384, -70.6858850087253),
                LatLng(-33.60445380738975, -70.68341941808892),
                LatLng(-33.60482344320421, -70.68254697830554),
            )
        )

        polygon52.fillColor = Color.argb(100,0,255,0)
        polygon52.strokeColor = -0xc771c4

        val polygon53 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.57400551261028, -70.71275208334674),
                LatLng(-33.57302222052912, -70.7070658001812),
                LatLng(-33.57301084933551, -70.70085850973794),
                LatLng(-33.573001979366055, -70.70081491876763),
                LatLng(-33.57342599982764, -70.69964745819502),
                LatLng(-33.57408977917071, -70.69810148296546),
                LatLng(-33.575207093002234, -70.69520903112313),
                LatLng(-33.57682510937355, -70.69586178440267),
                LatLng(-33.57738278411451, -70.69427710932466),
                LatLng(-33.575916301180804, -70.69335983567218),
                LatLng(-33.576396525765915, -70.69220822293462),
                LatLng(-33.577815894575686, -70.6885273098801),
                //orilla derecha
                LatLng(-33.579115454949196, -70.6852834987609),
                LatLng(-33.57999010962411, -70.68572194786894),
                LatLng(-33.581132778409426, -70.68634712946572),
                LatLng(-33.58202564395347, -70.68683834357776),
                LatLng(-33.58309458883398, -70.68745899700504),
                LatLng(-33.5844285387434, -70.68821176669081),
                LatLng(-33.58589001397176, -70.68916867731438),
                LatLng(-33.588244265655604, -70.69068059608614),
                LatLng(-33.58757997993642, -70.69211596203418),
                LatLng(-33.58801043766948, -70.69236475879156),
                LatLng(-33.588302722562844, -70.69207130619701),
                LatLng(-33.590423086954836, -70.69193095930154),
                LatLng(-33.590837588202724, -70.6919437181019),
                LatLng(-33.59242117712273, -70.69226268830899),
                LatLng(-33.592591225230784, -70.69260079671066),
                LatLng(-33.59234146698614, -70.69395960978517),
                LatLng(-33.5913763700776, -70.69758728918906),
                //orilla inferior
                //LatLng(-33.5913763700776, -70.69758728918906),
                LatLng(-33.59097254811624, -70.69915373491112),
                LatLng(-33.590123508923845, -70.70283372571055),
                LatLng(-33.58911358825284, -70.70685703924448),
                LatLng(-33.58832709316288, -70.70997913050756),
                LatLng(-33.588076843220236, -70.71129877733271),
                LatLng(-33.58765677919528, -70.71347673107766),
                LatLng(-33.58748696549789, -70.71459253002932),
                //orilla izquierda
                //LatLng(-33.58748696549789, -70.71459253002932),
                LatLng(-33.585119201906195, -70.71425498476327),
                LatLng(-33.58235100138764, -70.71393635457714),
                LatLng(-33.577461158901734, -70.71327570801587),
                LatLng(-33.57655835451926, -70.7131576908185),
                LatLng(-33.57400551261028, -70.71275208334674),
            )
        )

        polygon53.fillColor = Color.argb(100,0,120,255)
        polygon53.strokeColor = -0xc771c4

        val polygon54 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla inferior
                LatLng(-33.58748696549789, -70.71459253002932),
                LatLng(-33.58765677919528, -70.71347673107766),
                LatLng(-33.588076843220236, -70.71129877733271),
                LatLng(-33.58832709316288, -70.70997913050756),
                LatLng(-33.58911358825284, -70.70685703924448),
                LatLng(-33.590123508923845, -70.70283372571055),
                LatLng(-33.59097254811624, -70.69915373491112),
                LatLng(-33.5913763700776, -70.69758728918906),
                ///
                LatLng(-33.59687026033783, -70.69719964608201),
                LatLng(-33.597599884657534, -70.69729576581396),
                LatLng(-33.59800292087365, -70.69630562841932),
                LatLng(-33.59841365796306, -70.69539525647374),
                LatLng(-33.599538435700005, -70.6930965673658),
                LatLng(-33.60036621217735, -70.6915185893463),
                LatLng(-33.60133237505482, -70.68934442209247),
                LatLng(-33.603101621823384, -70.6858850087253),
                LatLng(-33.60445380738975, -70.68341941808892),
                LatLng(-33.60482344320421, -70.68254697830554),
                //
                LatLng(-33.60483734613178, -70.68253172665138),
                LatLng(-33.60651897116098, -70.68299742315637),
                LatLng(-33.607682540853695, -70.68323984052387),
                LatLng(-33.60880890385244, -70.68346949907233),
                LatLng(-33.60984493230677, -70.68371191642855),
                LatLng(-33.61056748838638, -70.68349501669401),
                LatLng(-33.61264480331997, -70.68308035543114),
                LatLng(-33.61451145911903, -70.68266887631346),
                LatLng(-33.616465018196585, -70.68232242857945),
                LatLng(-33.621519025339715, -70.68126032797655),
                //orilla inferior
                //LatLng(-33.621519025339715, -70.68126032797655),
                LatLng(-33.62085791345965, -70.68428264435163),
                LatLng(-33.621316184734965, -70.6843909064314),
                LatLng(-33.62029446181301, -70.68866725847768),
                LatLng(-33.619498110544676, -70.69306089444555),
                LatLng(-33.61847636606766, -70.69842888913739),
                LatLng(-33.618536469015304, -70.69916867997327),
                LatLng(-33.616755901317305, -70.70387808038227),
                LatLng(-33.61556132259811, -70.70701768060945),
                LatLng(-33.61390090582199, -70.71141131658965),
                LatLng(-33.61388587927472, -70.71250295914604),
                LatLng(-33.61354778138303, -70.71603952039048),
                //orilla izquierda
                //LatLng(-33.61354778138303, -70.71603952039048),
                LatLng(-33.60900211426626, -70.71667104917206),
                LatLng(-33.60625205712584, -70.71683344228079),
                LatLng(-33.60398281824769, -70.71664398363939),
                LatLng(-33.60071410759701, -70.71621093533783),
                LatLng(-33.598948200868826, -70.71600343302107),
                LatLng(-33.592504030202285, -70.71525627430698),
                LatLng(-33.589270595245324, -70.71482910912306),
            )
        )

        polygon54.fillColor = Color.argb(100,255,175,0)
        polygon54.strokeColor = -0xc771c4

        val polygon55 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.598758130364885, -70.68104863980716),
                LatLng(-33.59931771592088, -70.67920680275886),
                LatLng(-33.599851120647784, -70.67751626422839),
                LatLng(-33.60054528613231, -70.67530522183957),
                LatLng(-33.601285174608975, -70.67230682674777),
                LatLng(-33.60187496686815, -70.66976144449377),
                LatLng(-33.6021193840964, -70.66874711922127),
                //orilla derecha
                //LatLng(-33.6021193840964, -70.66874711922127),
                LatLng(-33.60272296640602, -70.6690351628254),
                LatLng(-33.605939003147384, -70.67016289281274),
                LatLng(-33.60673086276922, -70.67043017261959),
                LatLng(-33.60743852152527, -70.67066155882134),
                LatLng(-33.60771968790465, -70.67071466385231),
                LatLng(-33.60798505757763, -70.67078294174766),
                LatLng(-33.60896123183834, -70.67104467368084),
                LatLng(-33.60969132692681, -70.6712672477678),
                LatLng(-33.61049152559167, -70.67150181560392),
                LatLng(-33.61498305001037, -70.6728760382364),
                //orilla inferior
                //LatLng(-33.61498305001037, -70.6728760382364),
                LatLng(-33.61458583614491, -70.67775673516869),
                LatLng(-33.614272389683435, -70.68088264317929),
                LatLng(-33.61451145911903, -70.68266887631346),
                //orilla izquierda
                //LatLng(-33.61451145911903, -70.68266887631346),
                LatLng(-33.61264480331997, -70.68308035543114),
                LatLng(-33.61056748838638, -70.68349501669401),
                LatLng(-33.60984493230677, -70.68371191642855),
                LatLng(-33.60880890385244, -70.68346949907233),
                LatLng(-33.607682540853695, -70.68323984052387),
                LatLng(-33.60651897116098, -70.68299742315637),
                LatLng(-33.60483734613178, -70.68253172665138),
                //de aqui para abajo toda la orilla izquierda es compartida por el polygon52
                LatLng(-33.60377820230097, -70.68224788206687),
                LatLng(-33.60317706798396, -70.68209451079366),
                LatLng(-33.60212882999405, -70.68184641020022),
                LatLng(-33.59971216591455, -70.68121707906633),
                //LatLng(-33.598758130364885, -70.68104863980716),
            )
        )
        polygon55.fillColor = Color.argb(100,255,0,255)
        polygon55.strokeColor = -0xc771c4



        val polygon56 = map.addPolygon(PolygonOptions()
            .clickable(true)
            .add(
                //orilla superior
                LatLng(-33.621519025339715, -70.68126032797655),
                LatLng(-33.62085791345965, -70.68428264435163),
                LatLng(-33.621316184734965, -70.6843909064314),
                LatLng(-33.62029446181301, -70.68866725847768),
                LatLng(-33.619498110544676, -70.69306089444555),
                LatLng(-33.61847636606766, -70.69842888913739),
                LatLng(-33.618536469015304, -70.69916867997327),
                LatLng(-33.616755901317305, -70.70387808038227),
                LatLng(-33.61556132259811, -70.70701768060945),
                LatLng(-33.61390090582199, -70.71141131658965),
                LatLng(-33.61388587927472, -70.71250295914604),
                LatLng(-33.61354778138303, -70.71603952039048),
                //orrilla izquierda
                //LatLng(-33.61354778138303, -70.71603952039048),
                LatLng(-33.61982689079374, -70.7150780006383),
                LatLng(-33.625041240342675, -70.71430388277986),
                LatLng(-33.63341225182708, -70.71314649555418),
                LatLng(-33.63823110259856, -70.71240748590726),
                //orilla superior
                //LatLng(-33.63823110259856, -70.71240748590726),
                LatLng(-33.63829095277931, -70.71161792391376),
                LatLng(-33.63765383582689, -70.70974382816232),
                LatLng(-33.637640833391025, -70.70683897974756),
                LatLng(-33.638147926948136, -70.70483994433174),
                LatLng(-33.6385249945707, -70.70457444743361),
                LatLng(-33.63909709401746, -70.70454321250443),
                LatLng(-33.63937014014074, -70.70415277588955),
                LatLng(-33.64034280056032, -70.69733987815754),
                LatLng(-33.639744705158755, -70.69001528746843),
                LatLng(-33.638990578933424, -70.68222217272863),
                LatLng(-33.63909459673521, -70.68116018523814),
                LatLng(-33.6394976645399, -70.6786301560785),
                LatLng(-33.63924347329206, -70.67502088198),
                LatLng(-33.63869679658627, -70.67303128254161),
                //orilla derecha
                //LatLng(-33.63869679658627, -70.67303128254161),
/*                LatLng(),
                LatLng(),
                LatLng(),
                LatLng(),*/
            )
        )

        polygon56.fillColor = Color.argb(100,128,64,0)
        polygon56.strokeColor = -0xc771c4
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


