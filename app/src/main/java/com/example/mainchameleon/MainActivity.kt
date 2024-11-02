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
    val navView get() = binding.navView // Expose navView to CameraFragment for visibility control

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Define top-level destinations for AppBarConfiguration
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_journal, R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_calendar
            )
        )

        navView.setupWithNavController(navController)

        // Remove this block:
        // val shouldNavigateToCamera = intent.getBooleanExtra("navigateToCamera", false)
        // val source = intent.getStringExtra("source") ?: "journal"
        // if (shouldNavigateToCamera) {
        //     val bundle = Bundle().apply { putString("source", source) }
        //     navController.navigate(R.id.navigation_camera, bundle)
        // }

        // Handle bottom navigation item selection
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_calendar -> {
                    navController.navigate(R.id.navigation_calendar)
                    true
                }
                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }
}
