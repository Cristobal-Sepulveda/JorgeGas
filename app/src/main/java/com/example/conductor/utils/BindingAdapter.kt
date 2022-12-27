package com.example.conductor.utils

/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

/** useful methods to bind viewModel variables to the UI */
/**
 * When there is no Asteroid data (data is null), hide the [RecyclerView], otherwise show it.
 */


/*@BindingAdapter("cloudDownloadComplete")
fun bindStatus(imageView: ImageView, status: CloudDownloadComplete?) {
    when (status) {
        CloudDownloadComplete.LOADING ->{
            imageView.visibility = View.GONE
        }

        CloudDownloadComplete.ERROR -> {
            Log.i("BindingAdapter", "image_gone")
            imageView.visibility = View.VISIBLE
        }

        CloudDownloadComplete.DONE -> {
            Log.i("BindingAdapter", "image_visible")
            imageView.visibility = View.GONE
        }
    }
}*/

/*@BindingAdapter("fieldsStatus")
fun bindStatus(progressBar: ProgressBar, status: CloudDownloadComplete?) {
    when (status) {
        CloudDownloadComplete.LOADING -> {
            progressBar.visibility = View.VISIBLE
        }
        CloudDownloadComplete.ERROR -> {
            progressBar.visibility = View.GONE
        }
        CloudDownloadComplete.DONE -> {
            progressBar.visibility = View.GONE
        }
    }
}*/

/*
@BindingAdapter("asteroidApiStatusError")
fun bindStatusError(imageView: ImageView, status: AsteroidsApiStatus?) {
    when (status) {
        AsteroidsApiStatus.LOADING -> {
            imageView.visibility = View.GONE
        }
        AsteroidsApiStatus.ERROR -> {
            imageView.visibility = View.VISIBLE
        }
        AsteroidsApiStatus.DONE -> {
            imageView.visibility = View.GONE
        }
    }
}

@BindingAdapter("asteroidStatusImage")
fun bindDetailsStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.asteroid_hazardous)
    } else {
        imageView.setImageResource(R.drawable.asteroid_safe)
    }
}

@BindingAdapter("statusIcon")
fun bindAsteroidStatusImage(imageView: ImageView, isHazardous: Boolean) {
    if (isHazardous) {
        imageView.setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        imageView.setImageResource(R.drawable.ic_status_normal)
    }
}

@BindingAdapter("astronomicalUnitText")
fun bindTextViewToAstronomicalUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

@BindingAdapter("kmUnitText")
fun bindTextViewToKmUnit(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_unit_format), number)
}

@BindingAdapter("velocityText")
fun bindTextViewToDisplayVelocity(textView: TextView, number: Double) {
    val context = textView.context
    textView.text = String.format(context.getString(R.string.km_s_unit_format), number)
}
@BindingAdapter("isHazardous")
fun bindIsDangerous(imageView: ImageView, isHazardous: Boolean){
    if(isHazardous){
        imageView.contentDescription = "This asteroid is Hazardous"
    }else{
        imageView.contentDescription = "this asteroid is not Hazardous"
    }
}*/
