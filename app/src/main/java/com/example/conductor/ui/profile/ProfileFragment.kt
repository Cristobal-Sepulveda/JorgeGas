package com.example.conductor.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.example.conductor.AuthenticationActivity
import com.example.conductor.base.BaseFragment
import com.example.conductor.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : DialogFragment() {

    //use Koin to retrieve the ViewModel instance
    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root


        _binding!!.imageviewLogout.setOnClickListener{
            logout()
        }

        _binding!!.imageviewCerrarModal.setOnClickListener{
            this.dismiss()
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