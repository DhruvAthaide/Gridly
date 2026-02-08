package com.dhruvathaide.gridly.ui.common

import android.content.Context
import com.dhruvathaide.gridly.R

object ResourceHelper {

    // Manual mappings for filenames that don't match standard "{surname}" format
    private val driverMappings = mapOf(
        "perez" to "driver_checo", // API/Data says Perez, file is checo
        "russell" to "driver_russel", // File typo
        "lindblad" to "driver_lindbad", // File typo
        "bortoleto" to "driver_bortoletto", // File typo
        "piastri" to "driver_oscar", // First name used
        "sainz jr." to "driver_sainz",
        "hülkenberg" to "driver_hulkenberg"
    )

    private val teamMappings = mapOf(
        "red bull racing" to "logo_red_bull_racing",
        "haas" to "logo_haas_f1_team",
        "racing bulls" to "logo_racing_bulls",
        "cadillac" to "logo_cadillac",
        "cadillac f1 team" to "logo_cadillac"
    )

    fun getDriverHeadshot(context: Context, driverSurname: String): Int {
        val cleanName = driverSurname.lowercase().trim()
        val mappedName = driverMappings[cleanName] ?: "driver_${cleanName.replace(" ", "_")}"
        return getDrawableId(context, mappedName)
    }

    fun getTeamLogo(context: Context, teamName: String): Int {
        val cleanName = teamName.lowercase().trim()
        val mappedName = teamMappings[cleanName] ?: "logo_${cleanName.replace(" ", "_")}"
        return getDrawableId(context, mappedName)
    }
    
    fun getTrackMap(context: Context, countryName: String, locationName: String): Int {
        val cleanCountry = countryName.lowercase().trim()
        val cleanLocation = locationName.lowercase().trim()
        
        // precise mapping based on existing drawables
        val mappedName = when {
            // Specific Cities/Circuits
            cleanLocation.contains("monte carlo") -> "track_monaco"
            cleanLocation.contains("miami") -> "track_miami"
            cleanLocation.contains("las vegas") -> "track_lasvegas"
            cleanLocation.contains("silverstone") -> "track_britain"
            cleanLocation.contains("spa") -> "track_belgium"
            cleanLocation.contains("monza") -> "track_italy"
            cleanLocation.contains("baku") -> "track_azerbaijan"
            cleanLocation.contains("suzuka") -> "track_japan"
            cleanLocation.contains("interlagos") || cleanLocation.contains("sao paulo") -> "track_brazil"
            cleanLocation.contains("marina bay") -> "track_singapore"
            cleanLocation.contains("albert park") -> "track_australia"
            cleanLocation.contains("zandvoort") -> "track_netherlands"
            
            // Country Overrides
            cleanCountry.contains("united arab emirates") || cleanCountry.contains("uae") -> "track_abu_dhabi"
            cleanCountry.contains("saudi") -> "track_saudi_arabia"
            cleanCountry.contains("united states") || cleanCountry.contains("usa") -> "track_united_states" // Default to Austin if not Miami/Vegas
            cleanCountry.contains("great britain") || cleanCountry.contains("uk") -> "track_britain"
            cleanCountry.contains("netherlands") -> "track_netherlands"
            
            // Direct simple mappings (austria -> track_austria)
            else -> "track_${cleanCountry.replace(" ", "_")}"
        }
        
        return getDrawableId(context, mappedName)
    }

    private fun getDrawableId(context: Context, name: String): Int {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.ic_trophy // Fallback
    }
}
