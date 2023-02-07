package com.example.conductor.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.VolanteroItemViewBinding
import com.example.conductor.ui.gestiondevolanteros.GestionDeVolanterosViewModel


class VolanteroAdapter(viewModel: GestionDeVolanterosViewModel, dataSource: AppDataSource, val onClickListener: OnClickListener)
    : ListAdapter<Usuario, VolanteroAdapter.VolanteroViewHolder>(DiffCallBack) {

    val dataSourcee = dataSource
    val viewModell = viewModel

    class VolanteroViewHolder(private var binding: VolanteroItemViewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario){
            binding.usuarioItem = usuario
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

    override fun onBindViewHolder(holder: VolanteroViewHolder, position: Int) {
        val usuario = getItem(position)
        /*holder.itemView.setOnClickListener(){
            onClickListener.onClick(usuario)
        }*/

        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroItem_llamar).setOnClickListener{ view ->
            onClickListener.onClick(usuario)
        }
        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroItem_notificar).setOnClickListener{ view ->
            onClickListener.onClick(usuario)
        }
        holder.bind(usuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolanteroViewHolder {
        return VolanteroViewHolder(VolanteroItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (usuario: Usuario) -> Unit) {
        fun onClick(usuario: Usuario) = clickListener(usuario)
    }

}
