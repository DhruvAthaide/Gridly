package com.dhruvathaide.gridly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.ui.circuit.CircuitFragment
import com.dhruvathaide.gridly.ui.dashboard.DashboardFragment
import com.dhruvathaide.gridly.ui.home.HomeFragment
import com.dhruvathaide.gridly.ui.standings.StandingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Theme
        com.dhruvathaide.gridly.ui.theme.ThemeManager.loadTheme(this)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> loadFragment(HomeFragment())
                R.id.nav_dashboard -> loadFragment(DashboardFragment()) // Pit Wall
                R.id.nav_standings -> loadFragment(StandingsFragment())
                R.id.nav_circuit -> loadFragment(CircuitFragment())
                R.id.nav_settings -> loadFragment(com.dhruvathaide.gridly.ui.settings.SettingsFragment())
                else -> false
            }
        }

        // Default load
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_home
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }
}