package com.example.conductor.ui.map

import android.app.Application

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.conductor.base.BaseViewModel
import com.example.conductor.data.AppDataSource


enum class CloudDownloadComplete{LOADING, ERROR, DONE}
class MapViewModel(val app: Application, val dataSource: AppDataSource) : BaseViewModel(app) {


    private val _status = MutableLiveData<CloudDownloadComplete>(CloudDownloadComplete.LOADING)
    val status: LiveData<CloudDownloadComplete>
        get()= _status
}
