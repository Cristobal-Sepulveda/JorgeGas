package com.example.conductor.ui.administrarcuentas

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource

class AdministrarCuentasViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }


    val text: LiveData<String> = _text
}