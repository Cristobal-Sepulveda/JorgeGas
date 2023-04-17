package com.example.conductor.adapter

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.UsuarioItemViewBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import kotlinx.coroutines.launch
import com.example.conductor.data.AppDataSource
import com.example.conductor.databinding.VolanteroSinMaterialItemViewBinding
import com.example.conductor.ui.gestiondematerial.GestionDeMaterialViewModel


class VolanteroSinMaterialAdapter(viewModel: GestionDeMaterialViewModel, dataSource: AppDataSource, val onClickListener: OnClickListener)
    : ListAdapter<Usuario, VolanteroSinMaterialAdapter.UsuarioViewHolder>(DiffCallBack) {

    private val viewModell = viewModel

    class UsuarioViewHolder(private var binding: VolanteroSinMaterialItemViewBinding):
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

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = getItem(position)
        holder.itemView.findViewById<ImageView>(R.id.imageView_volanteroSinMaterial_item).setOnClickListener{ view ->
            viewModell.viewModelScope.launch{
                viewModell.notificarQueSeAbastecioAlVolanteroDeMaterial(view.context, usuario.id)
            }
        }
        holder.bind(usuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        return UsuarioViewHolder(VolanteroSinMaterialItemViewBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (usuario: Usuario) -> Unit) {
        fun onClick(usuario: Usuario) = clickListener(usuario)
    }

}
