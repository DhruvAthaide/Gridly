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
    
    fun loadTheme(context: Context) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        val color = prefs.getString("theme_color", DEFAULT_COLOR) ?: DEFAULT_COLOR
        _currentThemeColor.value = color
    }
    
    fun setThemeColor(context: Context, colorHex: String) {
        val prefs = context.getSharedPreferences("gridly_prefs", Context.MODE_PRIVATE)
        prefs.edit { putString("theme_color", colorHex) }
        _currentThemeColor.value = colorHex
    }
    
    // Helper to get teams
    val teams = listOf(
        TeamTheme("Red Bull", "3671C6"),
        TeamTheme("Mercedes", "27F4D2"),
        TeamTheme("Ferrari", "F91536"),
        TeamTheme("McLaren", "F58020"),
        TeamTheme("Aston Martin", "358C75"),
        TeamTheme("Alpine", "0090FF"),
        TeamTheme("Williams", "37BEDD"),
        TeamTheme("AlphaTauri", "5E8FAA"),
        TeamTheme("Alfa Romeo", "C92D4B"),
        TeamTheme("Haas", "B6BABD"),
        TeamTheme("F1 Generic", "FF1801")
    )
    
    data class TeamTheme(val name: String, val colorHex: String)
}
