package com.example.conductor.utils

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> log(tag: String, message: String, function: () -> T): T {
    Log.d(tag, message)
    val result = function()
    Log.d(tag, "Function result: $result")
    return result
}


fun lanzarAlertaConConfirmacionYFuncionEnConsecuenciaEnMainThread(context: Context, title: Int, message:Int, onOkClicked: () -> Unit) {
    Handler(Looper.getMainLooper()).post {
        AlertDialog.Builder(context).apply{
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { _, _, -> onOkClicked.invoke() }
            setNegativeButton("CANCELAR") { _, _ -> }
        }.show()
    }
}

