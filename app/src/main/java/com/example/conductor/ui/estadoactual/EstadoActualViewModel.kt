package com.example.conductor.ui.estadoactual

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.conductor.ui.base.BaseViewModel
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.VolanteroYRecorrido
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

class EstadoActualViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {

    private val _status = MutableLiveData<CloudRequestStatus>()
    val status: LiveData<CloudRequestStatus>
        get() = _status

    private var _volanterosActivos = MutableLiveData<Boolean>()
    val volanterosActivos: LiveData<Boolean>
        get() = _volanterosActivos

    private val _volanterosInScreen = MutableLiveData<List<VolanteroYRecorrido>>()
    val volanterosInScreen: LiveData<List<VolanteroYRecorrido>>
        get() = _volanterosInScreen

    init{
        displayUsuariosInRecyclerView()
    }
    fun displayUsuariosInRecyclerView() {
        _status.value = CloudRequestStatus.LOADING
        viewModelScope.launch {
            _volanterosActivos.value = false
            val listaDeUsuariosVolanteros = mutableListOf<VolanteroYRecorrido>()

            val listaDeUsuarios = dataSource.obtenerUsuariosDesdeFirestore()

            val registroTrayectoVolanteros = dataSource.obtenerRegistroTrayectoVolanterosColRef()

            if (listaDeUsuarios.isEmpty() || registroTrayectoVolanteros.isEmpty()) {
                _status.value = CloudRequestStatus.ERROR
                return@launch
            }

            for (usuario in listaDeUsuarios) {
                if (usuario.rol == "Volantero") {
                    for (doc in registroTrayectoVolanteros) {
                        if (doc.id == usuario.id && doc.data!!["estaActivo"] == true) {
                            Log.i("asd", "$usuario")
                            val registroJornada =
                                doc.data!!["registroJornada"] as ArrayList<Map<String, Map<*, *>>>
                            registroJornada.forEach {
                                if (it["fecha"].toString() == LocalDate.now().toString()) {
                                    var tiempoEnRojo = 0f
                                    var tiempoEnAmarillo = 0f
                                    var tiempoEnVerde = 0f
                                    var tiempoEnAzul = 0f
                                    val tiempoEnRosado = 0f
                                    var distanciaRecorrida = 0
                                    val registroLatLngs = it["registroLatLngs"] as Map<*, *>
                                    val listOfGeopoints = registroLatLngs["geopoints"] as List<GeoPoint>
                                    val listOfHours = registroLatLngs["horasConRegistro"] as List<String>

                                    listOfGeopoints.forEachIndexed loop@{ i, geopoint ->
                                        if (i == listOfGeopoints.size - 1) {
                                            return@loop
                                        }
                                        val latLng1 = Location("")
                                        latLng1.latitude = geopoint.latitude
                                        latLng1.longitude = geopoint.longitude
                                        val latLng2 = Location("")
                                        latLng2.latitude = listOfGeopoints[i + 1].latitude
                                        latLng2.longitude = listOfGeopoints[i + 1].longitude
                                        val tiempoLatLng1 = LocalTime.parse(listOfHours[i])
                                        val tiempoLatLng2 = LocalTime.parse(listOfHours[i + 1])

                                        val distanceBetweenLatLngs = latLng1.distanceTo(latLng2).toInt()
                                        distanciaRecorrida += distanceBetweenLatLngs
                                        val timeBetweenLatLngs = Duration.between(tiempoLatLng1, tiempoLatLng2).toMillis()

                                        val rangoMayor = (timeBetweenLatLngs / 1000 * 0.75).toInt()
                                        val rangoMenor = (timeBetweenLatLngs / 1000 * 0.30).toInt()
                                        val rangoMaximoHumano = rangoMayor * 4

                                        when (distanceBetweenLatLngs) {
                                            in 0..rangoMenor -> tiempoEnRojo += timeBetweenLatLngs / 1000
                                            in rangoMenor..rangoMayor -> tiempoEnAmarillo += timeBetweenLatLngs / 1000
                                            in rangoMayor..rangoMaximoHumano -> tiempoEnVerde += timeBetweenLatLngs / 1000
                                            else -> tiempoEnAzul += timeBetweenLatLngs / 1000
                                        }
                                    }
                                    val volanteroYRecorrido = VolanteroYRecorrido(
                                        doc.id,
                                        usuario.fotoPerfil,
                                        usuario.nombre,
                                        usuario.apellidos,
                                        usuario.telefono,
                                        usuario.usuario,
                                        usuario.password,
                                        usuario.deshabilitada,
                                        usuario.sesionActiva,
                                        usuario.rol,
                                        convertSecondsToHMS(tiempoEnVerde),
                                        convertSecondsToHMS(tiempoEnAmarillo),
                                        convertSecondsToHMS(tiempoEnRojo),
                                        convertSecondsToHMS(tiempoEnAzul),
                                        convertSecondsToHMS(tiempoEnRosado),
                                        editarDistanciaTotalRecorrida(distanciaRecorrida)
                                    )
                                    listaDeUsuariosVolanteros.add(volanteroYRecorrido)
                                }
                            }
                        }
                    }
                }
            }

            if (listaDeUsuariosVolanteros.isEmpty()) {
                _volanterosActivos.value = true
                _status.value = CloudRequestStatus.DONE

            } else {
                listaDeUsuariosVolanteros.sortedWith(compareBy { it.nombre })
                _volanterosInScreen.value = listaDeUsuariosVolanteros
                _status.value = CloudRequestStatus.DONE
            }
        }
    }

    private fun convertSecondsToHMS(seconds: Float): String {
        val hours = (seconds / 3600).toInt()
        val minutes = ((seconds % 3600) / 60).toInt()
        val remainingSeconds = (seconds % 60).toInt()
        return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    private fun editarDistanciaTotalRecorrida(distancia: Int): String {
        val distanciaKm = distancia / 1000.0
        return "%.2fkm".format(distanciaKm)
    }
}