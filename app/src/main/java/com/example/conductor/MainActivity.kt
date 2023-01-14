package com.example.conductor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.loader.app.LoaderManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.conductor.databinding.ActivityMainBinding
import com.example.conductor.utils.Constants.firebaseAuth
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity(), MenuProvider{

    //val cloudDB = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityMainBinding
    private var menuHost: MenuHost = this
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    private lateinit var firebaseUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        definingDrawableMenu()
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
        NavigationUI.setupWithNavController(binding.navView, navController)
        menuHost.addMenuProvider(this, this, Lifecycle.State.RESUMED)
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)
        val a = LoaderManager.getInstance(this)
        binding.navView.menu.findItem(R.id.logout_item).setOnMenuItemClickListener {
            logout()
            true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val drawerLayout = binding.drawerLayout
        NavigationUI.navigateUp(navController,drawerLayout)
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.overflow_menu,menu )
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId){
            R.id.modoClaro -> {
                showToast()
            }
            R.id.modoOscuro ->{
                showToast()
            }
            R.id.acercaDe -> {
                showToast()
            }
            R.id.navigation_administrar_cuentas -> {
                return NavigationUI.onNavDestinationSelected(
                    menuItem,
                    navController)
            }
        }
        return false

    }

    private fun definingDrawableMenu(){
        firebaseUser = firebaseAuth.currentUser!!.email.toString()
        if( firebaseUser != "1@1.1"){
            binding.navView.menu.findItem(R.id.navigation_administrar_cuentas).isVisible = false
        }
    }

    private fun showToast(){
        Toast.makeText(
                this,
                "Esta funcionalidad se implementará en un futuro.",
                Toast.LENGTH_LONG).show()
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        this.finish()
        startActivity(Intent(this, AuthenticationActivity::class.java))
    }

}