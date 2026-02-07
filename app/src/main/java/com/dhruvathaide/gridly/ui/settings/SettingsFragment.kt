package com.dhruvathaide.gridly.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.dhruvathaide.gridly.R
import com.dhruvathaide.gridly.ui.theme.ThemeManager

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Team Picker Container
        val teamContainer = view.findViewById<LinearLayout>(R.id.teamColorContainer)
        
        // Populate Team Circles dynamically
        ThemeManager.teams.forEach { team ->
            val circle = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(100, 100).apply {
                    marginEnd = 32
                }
                background = androidx.core.content.ContextCompat.getDrawable(context, R.drawable.bg_circle)
                backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#${team.colorHex}"))
                
                setOnClickListener {
                    ThemeManager.setThemeColor(requireContext(), team.colorHex)
                    // Visual feedback (alpha or scale) could be added here
                }
            }
            teamContainer.addView(circle)
        }
        
        // Toggle Logic (Mock for now)
        view.findViewById<Switch>(R.id.spoilerToggle).setOnCheckedChangeListener { _, isChecked ->
            // Save spoiler pref
        }
    }
}
