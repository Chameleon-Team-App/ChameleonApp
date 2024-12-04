package com.example.mainchameleon

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.mainchameleon.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Set up AppBarConfiguration for top-level destinations
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_mental_health,
                R.id.navigation_feed_fragment,
                R.id.navigation_profile
            )
        )

        // Set up the BottomNavigationView
        val navView: BottomNavigationView = binding.navView // Assuming navView is in the layout (activity_main.xml)
        navView.setupWithNavController(navController)  // Link BottomNavigationView with NavController
    }
}
