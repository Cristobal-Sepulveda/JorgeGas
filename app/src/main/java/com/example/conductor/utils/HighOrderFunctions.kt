package com.example.conductor.utils

import android.util.Log

fun <T> log(tag: String, message: String, function: () -> T): T {
    Log.d(tag, message)
    val result = function()
    Log.d(tag, "Function result: $result")
    return result
}

