package com.example.conductor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.conductor.AuthenticationActivity
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.android.inject

class ProfileFragment : BaseFragment() {

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: ProfileViewModel by inject()
    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textNotifications
        _viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        _binding!!.buttonLogout.setOnClickListener{
            logout()
        }
        return root
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        this.activity?.finish()
        startActivity(Intent(activity, AuthenticationActivity::class.java))
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}