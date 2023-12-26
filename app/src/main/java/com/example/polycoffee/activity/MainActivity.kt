package com.example.polycoffee.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.polycoffee.R
import com.example.polycoffee.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        binding.container.background.alpha = 60

        val role = intent.getIntExtra("role",0)

        val navView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_qltk,
                R.id.navigation_menu,
                R.id.navigation_order,
                R.id.navigation_profile,
                R.id.navigation_thongke
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        val menu = navView.menu
        if(role==0){
            menu.findItem(R.id.navigation_qltk).isVisible = false
            menu.findItem(R.id.navigation_menu).isVisible = false
            menu.findItem(R.id.navigation_thongke).isVisible = false
        }

        navView.setupWithNavController(navController)
    }

}