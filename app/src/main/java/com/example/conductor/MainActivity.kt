package com.example.conductor

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), MenuProvider {

    //val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var firebaseUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = firebaseAuth.currentUser!!.email.toString()
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        drawerLayout = binding.drawerLayout

        seteandoDrawableLayout()
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

    }

    private fun seteandoDrawableLayout(){
        if( firebaseUser == "1@1.1"){
            Log.i("MapFragment", "true")
            NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
            NavigationUI.setupWithNavController(binding.navView, navController)
        }else{
            Log.i("MapFragment", "false")
            NavigationUI.setupActionBarWithNavController(this, navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return if( firebaseUser == "1@1.1"){
            NavigationUI.navigateUp(navController,drawerLayout)
        }else{
            navController.navigateUp()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.overflow_menu,menu )
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId){
            R.id.logout -> logout()
            else -> {
                return NavigationUI.onNavDestinationSelected(
                    menuItem,
                    navController)
            }
        }
        return false
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        this.finish()
        startActivity(Intent(this, AuthenticationActivity::class.java))
    }

}