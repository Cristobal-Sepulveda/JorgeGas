package com.example.conductor.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun Fragment.showToast(message: String) = Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

fun ViewModel.changeUiStatusInMainThread(mutableLiveData: MutableLiveData<CloudRequestStatus>, cloudRequestStatus: CloudRequestStatus){
    viewModelScope.launch(Dispatchers.Main){
        mutableLiveData.value = cloudRequestStatus
    }
}

fun Activity.cerrarTeclado(it: View){
    val inputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
}