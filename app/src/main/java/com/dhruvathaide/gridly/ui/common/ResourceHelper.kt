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
        val cleanCountry = countryName.lowercase().trim().replace(" ", "_")
        val cleanLocation = locationName.lowercase().trim().replace(" ", "_")
        
        // Check for specific location overrides first (e.g., Miami, Las Vegas)
        val locationMapping = "track_$cleanLocation"
        val locationId = getDrawableId(context, locationMapping)
        if (locationId != R.drawable.ic_trophy && locationId != 0) {
            return locationId
        }

        // Fallback to Country
        val countryMapping = "track_$cleanCountry"
        return getDrawableId(context, countryMapping)
    }

    private fun getDrawableId(context: Context, name: String): Int {
        val id = context.resources.getIdentifier(name, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.ic_trophy // Fallback
    }
}
