package com.example.conductor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.data.data_objects.domainObjects.AsistenciaIndividual
import com.example.conductor.databinding.ItemListadoAsistenciaIndividualBinding


class AsistenciaIndividualAdapter(val onClickListener: OnClickListener) : ListAdapter<AsistenciaIndividual, AsistenciaIndividualAdapter.AsistenciaIndividualViewHolder>(DiffCallBack) {

    class AsistenciaIndividualViewHolder(private var binding: ItemListadoAsistenciaIndividualBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(asistenciaIndividual: AsistenciaIndividual){
            binding.asistenciaIndividualItem = asistenciaIndividual
            binding.executePendingBindings()
        }
    }

    object DiffCallBack: DiffUtil.ItemCallback<AsistenciaIndividual>(){
        override fun areItemsTheSame(oldItem: AsistenciaIndividual, newItem: AsistenciaIndividual): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: AsistenciaIndividual, newItem: AsistenciaIndividual): Boolean {
            return oldItem.fecha == newItem.fecha
        }
    }

    override fun onBindViewHolder(holder: AsistenciaIndividualViewHolder, position: Int) {
        val asistenciaIndividual = getItem(position)
        holder.bind(asistenciaIndividual)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaIndividualViewHolder {
        return AsistenciaIndividualViewHolder(ItemListadoAsistenciaIndividualBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (asistenciaIndividual: AsistenciaIndividual) -> Unit) {
        fun onClick(asistenciaIndividual: AsistenciaIndividual) = clickListener(asistenciaIndividual)
    }

}
