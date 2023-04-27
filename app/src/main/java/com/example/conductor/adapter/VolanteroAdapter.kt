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
import com.example.conductor.databinding.VolanteroItemViewBinding
import com.example.conductor.ui.gestiondevolanteros.GestionDeVolanterosViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class VolanteroAdapter(viewModel: GestionDeVolanterosViewModel, dataSource: AppDataSource, val onClickListener: OnClickListener)
    : ListAdapter<Usuario, VolanteroAdapter.VolanteroViewHolder>(DiffCallBack) {

    val dataSourcee = dataSource
    val viewModell = viewModel

    class VolanteroViewHolder(private var binding: VolanteroItemViewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario){
            binding.usuarioItem = usuario
            val aux = usuario.fotoPerfil
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
        holder.itemView.setOnClickListener(){
            onClickListener.onClick(usuario)
        }

        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroItem_parar).setOnClickListener{
            CoroutineScope(Dispatchers.Main).launch{
                withContext(Dispatchers.IO){
                    dataSourcee.registroTrayectoVolanterosEstaActivoFalse(usuario.id)
                }
            }
        }

        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroItem_llamar).setOnClickListener{
            /*onClickListener.onClick(usuario)*/
        }
        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroItem_notificar).setOnClickListener{
            /*onClickListener.onClick(usuario)*/
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
