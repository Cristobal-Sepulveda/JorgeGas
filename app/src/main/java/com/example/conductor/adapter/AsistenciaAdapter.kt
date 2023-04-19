package com.example.conductor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
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
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemListadoAsistenciaBinding>(inflater, R.layout.item_listado_asistencia, parent, false)
        binding.root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return AsistenciaViewHolder(binding)
    }

    class OnClickListener(val clickListener: (asistencia: Asistencia) -> Unit) {
        fun onClick(asistencia: Asistencia) = clickListener(asistencia)
    }

}
