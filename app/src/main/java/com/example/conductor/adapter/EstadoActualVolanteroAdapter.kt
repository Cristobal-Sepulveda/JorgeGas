package com.example.conductor.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.data.AppDataSource
import com.example.conductor.data.data_objects.domainObjects.VolanteroYRecorrido
import com.example.conductor.databinding.EstadoActualVolanteroItemBinding
import com.example.conductor.ui.estadoactual.EstadoActualViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class EstadoActualVolanteroAdapter(viewModel: EstadoActualViewModel,
                                   dataSource: AppDataSource,
                                   val onClickListener: OnClickListener)
    : ListAdapter<VolanteroYRecorrido, EstadoActualVolanteroAdapter.VolanteroViewHolder>(DiffCallBack) {

    val dataSourcee = dataSource

    class VolanteroViewHolder(private var binding: EstadoActualVolanteroItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(volanteroYRecorrido: VolanteroYRecorrido){
            binding.volanteroYRecorridoItem = volanteroYRecorrido
            val aux = volanteroYRecorrido.fotoPerfil
            if(aux.last().toString() == "=" || (aux.first().toString() == "/" && aux[1].toString() == "9")){
                val decodedString  = Base64.decode(aux, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                binding.circleImageViewFotoPerfil.setImageBitmap(decodedByte)
            }else{
                val aux2= aux.indexOf("=")+1
                val aux3 = aux.substring(0, aux2)
                val decodedString  = Base64.decode(aux3, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                binding.circleImageViewFotoPerfil.setImageBitmap(decodedByte)
            }
            binding.executePendingBindings()
        }
    }

    object DiffCallBack: DiffUtil.ItemCallback<VolanteroYRecorrido>(){
        override fun areItemsTheSame(oldItem: VolanteroYRecorrido, newItem: VolanteroYRecorrido): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: VolanteroYRecorrido, newItem: VolanteroYRecorrido): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onBindViewHolder(holder: VolanteroViewHolder, position: Int) {
        val volanteroYRecorrido = getItem(position)
        holder.itemView.setOnClickListener(){
            onClickListener.onClick(volanteroYRecorrido)
        }
        holder.bind(volanteroYRecorrido)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolanteroViewHolder {
        return VolanteroViewHolder(EstadoActualVolanteroItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (volanteroYRecorrido: VolanteroYRecorrido) -> Unit) {
        fun onClick(volanteroYRecorrido: VolanteroYRecorrido) = clickListener(volanteroYRecorrido)
    }

}
