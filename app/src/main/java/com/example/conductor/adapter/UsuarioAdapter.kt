package com.example.conductor.adapter

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.*
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.conductor.R
import com.example.conductor.data.data_objects.domainObjects.Usuario
import com.example.conductor.databinding.UsuarioItemViewBinding
import com.example.conductor.ui.administrarcuentas.AdministrarCuentasViewModel
import kotlinx.coroutines.launch
import com.example.conductor.data.AppDataSource


class UsuarioAdapter(viewModel: AdministrarCuentasViewModel, dataSource: AppDataSource, val onClickListener: OnClickListener)
    : ListAdapter<Usuario, UsuarioAdapter.UsuarioViewHolder>(DiffCallBack) {

    val dataSourcee = dataSource
    val viewModell = viewModel

    class UsuarioViewHolder(private var binding: UsuarioItemViewBinding):
            RecyclerView.ViewHolder(binding.root) {
        fun bind(usuario: Usuario){
            val aux = usuario.fotoPerfil
            val aux2= aux.indexOf("=")+1
            val aux3 = aux.substring(0, aux2)
            binding.usuarioItem = usuario
            Log.i("UsuarioAdapter",aux3)
            val decodedString  = Base64.decode(aux3, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            binding.circleImageViewFotoPerfil.setImageBitmap(decodedByte)
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
        /*holder.itemView.setOnClickListener(){
            onClickListener.onClick(usuario)
        }*/
        holder.itemView.findViewById<ImageView>(R.id.imageView_usuarioItem_edit).setOnClickListener{ view ->
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.administracion_de_cuentas_usuario_menu)
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.editarPerfil -> {
                        viewModell.displayUsuarioDetails(usuario)
                        return@setOnMenuItemClickListener true
                    }
                    R.id.eliminarCuenta -> {
                        //open a coroutine that run sendAlert
                        AlertDialog.Builder(view.context)
                            .setTitle(R.string.perm_request_rationale_title)
                            .setMessage(R.string.borrar_cuenta)
                            .setPositiveButton(R.string.request_perm_again) { _, _ ->
                                viewModell.viewModelScope.launch {
                                    usuario.deshabilitada = true
                                    dataSourcee.eliminarUsuarioDeFirebase(usuario)
                                    Toast.makeText(
                                        view.context,
                                        "La cuenta ha sido borrada con éxito.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .setNegativeButton(R.string.dismiss){ _, _ -> }
                            .create()
                            .show()
                        return@setOnMenuItemClickListener true
                    }
                    else -> {
                        return@setOnMenuItemClickListener true
                    }
                }
            }
            popupMenu.show()
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
