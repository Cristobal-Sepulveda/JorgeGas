package com.example.conductor.utils


import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Usuario>?) {
    val adapter = recyclerView.adapter as UsuarioAdapter
    adapter.submitList(data)
}

@BindingAdapter("cloudRequestStatusImage")
fun bindStatusErrorImage(imageView: ImageView, status: LiveData<CloudRequestStatus>?) {
    Log.d("BindingAdapter", "bindStatusErrorImage: ${status?.value}")
    when (status?.value) {
        CloudRequestStatus.LOADING -> {
            imageView.visibility = View.GONE
        }
        CloudRequestStatus.ERROR -> {
            imageView.visibility = View.VISIBLE
        }
        CloudRequestStatus.DONE -> {
            imageView.visibility = View.GONE
        }
        else -> {
            imageView.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("cloudRequestStatusCircularProgress")
fun bindStatusErrorCircularProgress(progressBar: ProgressBar, status: LiveData<CloudRequestStatus>?) {
    Log.d("BindingAdapter", "bindStatusErrorCircularProgress: ${status?.value}")
    when (status?.value) {
        CloudRequestStatus.LOADING -> {
            progressBar.visibility = View.VISIBLE
        }
        CloudRequestStatus.ERROR -> {
            progressBar.visibility = View.GONE
        }
        CloudRequestStatus.DONE -> {
            progressBar.visibility = View.GONE
        }
        else -> {
            progressBar.visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("noHayVolanterosActivos")
fun bindNoHayVolanterosActivos(textView: TextView, textVisible: LiveData<Boolean>?) {
    Log.d("BindingAdapter", "bindNoHayVolanterosActivos: ${textVisible?.value}")
    when (textVisible?.value) {
        true -> {
            textView.visibility = View.VISIBLE
        }
        false -> {
            textView.visibility = View.GONE
        }
        else -> {
            textView.visibility = View.GONE
        }
    }
}

