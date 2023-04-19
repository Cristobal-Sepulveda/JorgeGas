package com.example.conductor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.data.data_objects.domainObjects.Asistencia
import com.example.conductor.databinding.ItemListadoAsistenciaBinding


class AsistenciaAdapter(val onClickListener: OnClickListener) : ListAdapter<Asistencia, AsistenciaAdapter.AsistenciaViewHolder>(DiffCallBack) {

    class AsistenciaViewHolder(private var binding: ItemListadoAsistenciaBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(asistencia: Asistencia){
            binding.asistenciaItem = asistencia
            binding.executePendingBindings()
        }
    }

    object DiffCallBack: DiffUtil.ItemCallback<Asistencia>(){
        override fun areItemsTheSame(oldItem: Asistencia, newItem: Asistencia): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Asistencia, newItem: Asistencia): Boolean {
            return oldItem.idUsuario == newItem.idUsuario
        }
    }

    override fun onBindViewHolder(holder: AsistenciaViewHolder, position: Int) {

        val asistencia = getItem(position)
        holder.bind(asistencia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AsistenciaViewHolder {
        return AsistenciaViewHolder(ItemListadoAsistenciaBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (asistencia: Asistencia) -> Unit) {
        fun onClick(asistencia: Asistencia) = clickListener(asistencia)
    }

}
