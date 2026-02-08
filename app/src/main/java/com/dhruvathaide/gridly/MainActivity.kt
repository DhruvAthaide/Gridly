package com.dhruvathaide.gridly

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.ui.circuit.CircuitFragment
import com.dhruvathaide.gridly.ui.components.AnimatedNavigationBar
import com.dhruvathaide.gridly.ui.dashboard.DashboardFragment
import com.dhruvathaide.gridly.ui.home.HomeFragment
import com.dhruvathaide.gridly.ui.standings.StandingsFragment
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Theme
        com.dhruvathaide.gridly.ui.theme.ThemeManager.loadTheme(this)

        val composeNav = findViewById<ComposeView>(R.id.compose_navigation_bar)
        
        composeNav.setContent {
            var selectedIndex by remember { mutableStateOf(0) }
            
            AnimatedNavigationBar(
                selectedItem = selectedIndex,
                onItemSelected = { index ->
                    selectedIndex = index
                    when (index) {
                        0 -> loadFragment(HomeFragment())
                        1 -> loadFragment(DashboardFragment()) // Pit Wall
                        2 -> loadFragment(StandingsFragment())
                        3 -> loadFragment(CircuitFragment())
                        4 -> loadFragment(com.dhruvathaide.gridly.ui.settings.SettingsFragment())
                    }
                }
            )
        }

        // Default load
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        return true
    }

    fun openNewsArticle(url: String) {
        val fragment = com.dhruvathaide.gridly.ui.news.NewsDetailFragment.newInstance(url)
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in, 
                android.R.anim.fade_out,
                android.R.anim.fade_in, 
                android.R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}