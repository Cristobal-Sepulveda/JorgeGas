package com.example.conductor.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.UsuarioItemViewBinding


class UsuarioAdapter(private val onClickListener: OnClickListener)
    : ListAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder>(DiffCallBack){

    class UsuarioViewHolder(private var binding: UsuarioItemViewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario){
            binding.usuarioItem = usuario
            binding.imageViewUsuarioItemEdit.setOnClickListener{

            }
            // This is important, because it forces the data binding to execute immediately,
            // which allows the RecyclerView to make the correct view size measurements
            binding.executePendingBindings()
        }
    }

    object DiffCallBack: DiffUtil.ItemCallback<Usuario>(){
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = getItem(position)
        holder.itemView.setOnClickListener(){
            onClickListener.onClick(usuario)
        }
        holder.bind(usuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        return UsuarioViewHolder(UsuarioItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (usuario: Usuario) -> Unit) {
        fun onClick(usuario: Usuario) = clickListener(usuario)
    }
}
