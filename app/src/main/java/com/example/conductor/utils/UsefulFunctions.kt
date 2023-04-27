package com.example.conductor.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

fun mostrarToastEnMainThread(context: Context, message: String){
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