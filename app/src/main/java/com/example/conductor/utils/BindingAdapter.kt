package com.example.conductor.utils


import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
import com.example.conductor.adapter.UsuarioAdapter
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.ui.administrarcuentas.CloudRequestStatus
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
            imageView.visibility = View.GONE
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
            progressBar.visibility = View.GONE
        }
    }
}
@BindingAdapter("recyclerViewIsVisible")
fun bindRecyclerViewIsVisible(recyclerView: RecyclerView, status: LiveData<CloudRequestStatus>?) {
    Log.d("BindingAdapter", "bindStatusErrorCircularProgress: ${status?.value}")
    when (status?.value) {
        CloudRequestStatus.LOADING -> {
            recyclerView.visibility = View.GONE
        }
        CloudRequestStatus.ERROR -> {
            recyclerView.visibility = View.GONE
        }
        CloudRequestStatus.DONE -> {
            recyclerView.visibility = View.VISIBLE
        }
        else -> {
            recyclerView.visibility = View.GONE
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

@BindingAdapter("botonIniciarODetenerRegistroVolantero")
fun bindBotonIniciarODetenerRegistroVolantero(fab: FloatingActionButton, status: LiveData<Boolean>?) {
    Log.d("BindingAdapter", "bindBotonIniciarODetenerRegistroVolantero: ${status?.value}")
    when (status?.value) {
        true -> {
            fab.setImageResource(R.drawable.baseline_stop_24)
            fab.setBackgroundColor(Color.argb(
                100,
                255,
                0,
                0
            ))
        }
        false -> {
            fab.setImageResource(R.drawable.baseline_directions_walk_24)
            fab.setBackgroundColor(Color.argb(
                100,
                0,
                255,
                0
            ))
        }
        else -> {
            fab.setImageResource(R.drawable.baseline_directions_walk_24)
            fab.setBackgroundColor(Color.argb(
                100,
                0,
                255,
                0
            ))
        }
    }
}

@BindingAdapter("hayVolanterosSinMaterial")
fun bindHayVolanterosSinMaterial(textView: TextView, textVisible: LiveData<Boolean>?) {
    Log.d("BindingAdapter", "bindHayVolanterosSinMaterial: ${textVisible?.value}")
    when (textVisible?.value) {
        true -> {
            textView.visibility = View.GONE
        }
        false -> {
            textView.visibility = View.VISIBLE
        }
        else -> {
            textView.visibility = View.GONE
        }
    }
}

