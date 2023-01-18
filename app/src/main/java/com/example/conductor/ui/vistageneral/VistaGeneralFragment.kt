package com.example.conductor.ui.vistageneral

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import com.example.conductor.R
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentVistaGeneralBinding
import com.example.conductor.utils.SharedPreferenceUtil
import org.koin.android.ext.android.inject

class VistaGeneralFragment : BaseFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var _binding: FragmentVistaGeneralBinding? = null
    override val _viewModel: VistaGeneralViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVistaGeneralBinding.inflate(inflater, container, false)

        _viewModel.rolDelUsuario.observe(requireActivity()){
            if(it=="Volantero"){
                _binding!!.buttonVistaGeneralRegistroJornada.visibility = View.VISIBLE
            }
        }

        return _binding!!.root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        // Updates button states if new while in use location is added to SharedPreferences.
        if (key == SharedPreferenceUtil.KEY_FOREGROUND_ENABLED) {
            updateButtonState(sharedPreferences!!.getBoolean(
                SharedPreferenceUtil.KEY_FOREGROUND_ENABLED, false)
            )
        }
    }

    private fun updateButtonState(trackingLocation: Boolean) {
        if (trackingLocation) {
            _binding!!.buttonVistaGeneralRegistroJornada.text = getString(R.string.finalizar_turno)
            _binding!!.buttonVistaGeneralRegistroJornada.setBackgroundColor(Color.argb(100, 255, 0, 0))
        } else {
            _binding!!.buttonVistaGeneralRegistroJornada.text = getString(R.string.iniciar_turno)
            _binding!!.buttonVistaGeneralRegistroJornada.setBackgroundColor(Color.argb(100, 0, 255, 0))
        }
    }
}
