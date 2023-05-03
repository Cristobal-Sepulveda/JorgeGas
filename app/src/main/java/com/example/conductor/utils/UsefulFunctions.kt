package com.example.conductor.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

fun showToastInMainThreadWithHardcoreString(context: Context, message: String){
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

fun showToastInMainThreadWithStringResource(context: Context, message: Int){
    Handler(Looper.getMainLooper()).post {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}

fun convertirMesDeTextoStringANumeroString(mes: String): String{
    return when(mes){
        "Enero" -> "01"
        "Febrero" -> "02"
        "Marzo" -> "03"
        "Abril" -> "04"
        "Mayo" -> "05"
        "Junio" -> "06"
        "Julio" -> "07"
        "Agosto" -> "08"
        "Septiembre" -> "09"
        "Octubre" -> "10"
        "Noviembre" -> "11"
        "Diciembre" -> "12"
        else -> ""
    }
}

fun sumarEntreDosTiemposQueVienenComoString(tiempo1:String, tiempo2:String): String{
    Log.e("sumarEntreDosTiemposQueVienenComoString", "$tiempo1'--'$tiempo2")
    val (min1, sec1) = tiempo1.split(":")
    val (min2, sec2) = tiempo2.split(":")
    var totalMin = min1.toInt() + min2.toInt()
    var totalSec = sec1.toInt() + sec2.toInt()
    if (totalSec >= 60) {
        val extraMin = totalSec / 60
        totalMin += extraMin
        totalSec %= 60
    }
    return "$totalMin:${totalSec.toString().padStart(2, '0')}"
}