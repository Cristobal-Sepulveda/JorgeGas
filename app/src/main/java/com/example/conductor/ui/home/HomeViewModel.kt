package com.example.conductor.ui.home

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource

class HomeViewModel(val app: Application, val dataSource: AppDataSource,) : BaseViewModel(app) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    val text: LiveData<String> = _text
}