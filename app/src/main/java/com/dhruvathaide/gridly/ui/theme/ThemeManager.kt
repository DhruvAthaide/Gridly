package com.dhruvathaide.gridly.ui.theme

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeManager {
    
    // Default F1 Red
    private const val DEFAULT_COLOR = "FF1801"
    
    private val _currentThemeColor = MutableStateFlow(DEFAULT_COLOR)
    val currentThemeColor = _currentThemeColor.asStateFlow()
    
    private val _userName = MutableStateFlow("Guest User")
    val userName = _userName.asStateFlow()
    
    // Default to Verstappen
    private val _userDriver = MutableStateFlow("verstappen")
    val userDriver = _userDriver.asStateFlow()

    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        val color = prefs.getString("theme_color", DEFAULT_COLOR) ?: DEFAULT_COLOR
        _currentThemeColor.value = color
        _userName.value = prefs.getString("user_name", "Guest User") ?: "Guest User"
        _userDriver.value = prefs.getString("user_driver", "verstappen") ?: "verstappen"
        _isProductionMode.value = prefs.getBoolean("production_mode", false)
    }

    fun setThemeColor(context: Context, colorHex: String) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("theme_color", colorHex) }
        _currentThemeColor.value = colorHex
    }

    fun setUserName(context: Context, name: String) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("user_name", name) }
        _userName.value = name
    }
    
    fun setUserDriver(context: Context, driverId: String) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("user_driver", driverId) }
        _userDriver.value = driverId
    }
    
    // Production Mode (Mock vs Real API)
    private val _isProductionMode = MutableStateFlow(false)
    val isProductionMode = _isProductionMode.asStateFlow()
    
    fun setProductionMode(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        prefs.edit { putBoolean("production_mode", enabled) }
        _isProductionMode.value = enabled
    }
    
    // Helper to get teams
    val teams = listOf(
        TeamTheme("Red Bull", "3671C6", com.dhruvathaide.gridly.R.drawable.logo_red_bull_racing),
        TeamTheme("Mercedes", "27F4D2", com.dhruvathaide.gridly.R.drawable.logo_mercedes),
        TeamTheme("Ferrari", "F91536", com.dhruvathaide.gridly.R.drawable.logo_ferrari),
        TeamTheme("McLaren", "F58020", com.dhruvathaide.gridly.R.drawable.logo_mclaren),
        TeamTheme("Aston Martin", "358C75", com.dhruvathaide.gridly.R.drawable.logo_aston_martin),
        TeamTheme("Alpine", "0090FF", com.dhruvathaide.gridly.R.drawable.logo_alpine),
        TeamTheme("Williams", "37BEDD", com.dhruvathaide.gridly.R.drawable.logo_williams),
        TeamTheme("VCARB", "5E8FAA", com.dhruvathaide.gridly.R.drawable.logo_racing_bulls),
        TeamTheme("Kick Sauber", "52E252", com.dhruvathaide.gridly.R.drawable.logo_audi), // Placeholder/Future
        TeamTheme("Haas", "B6BABD", com.dhruvathaide.gridly.R.drawable.logo_haas_f1_team),
        TeamTheme("F1 Generic", "FF1801", com.dhruvathaide.gridly.R.drawable.ic_trophy)
    )
    
    data class TeamTheme(val name: String, val colorHex: String, val logoId: Int)
}
