package com.dhruvathaide.gridly.widget.data

import android.content.Context
import android.content.SharedPreferences
import com.dhruvathaide.gridly.data.remote.F1ApiService
import com.dhruvathaide.gridly.data.remote.model.SessionDto
import com.dhruvathaide.gridly.data.remote.model.DriverStandingDto
import com.dhruvathaide.gridly.data.remote.model.ConstructorStandingDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant

object WidgetDataManager {
    private const val PREFS_NAME = "gridly_widget_prefs"
    private const val KEY_NEXT_RACE = "next_race"
    private const val KEY_DRIVER_STANDINGS = "driver_standings"
    private const val KEY_CONSTRUCTOR_STANDINGS = "constructor_standings"
    private const val KEY_DRIVERS = "all_drivers"
    private const val KEY_LAST_UPDATE = "last_update"

    private val json = Json { ignoreUnknownKeys = true }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    suspend fun updateAllData(context: Context) {
        try {
            val sessions = F1ApiService.getSessions(year = java.time.Year.now().value, sessionType = "Race")
            val now = Instant.now()
            
            // 1. Next Race
            val nextRace = sessions.firstOrNull { 
                try {
                    Instant.parse(it.dateEnd).isAfter(now)
                } catch (e: Exception) { false }
            } ?: sessions.lastOrNull() 
            
            if (nextRace != null) {
                saveData(context, KEY_NEXT_RACE, nextRace)
            }

            // 2. Standings & Drivers
            val lastRace = sessions.lastOrNull { 
                 try {
                    Instant.parse(it.dateEnd).isBefore(now)
                } catch (e: Exception) { false }
            }
            
            if (lastRace != null) {
                // Fetch Drivers for lookup (Names, Colors)
                val drivers = F1ApiService.getDrivers(lastRace.sessionKey)
                saveData(context, KEY_DRIVERS, drivers)
            
                val driverStandings = F1ApiService.getDriverStandings(lastRace.sessionKey)
                saveData(context, KEY_DRIVER_STANDINGS, driverStandings)
                
                val constructors = F1ApiService.getConstructorStandings(lastRace.sessionKey)
                saveData(context, KEY_CONSTRUCTOR_STANDINGS, constructors)
            }
            
            // 3. Live Pit Wall Data (Race Control / Session Status)
            // Determine "Active" session (could be next race if close, or last race if just finished)
            // For "Live" widget, we want the *current* or *upcoming* one.
            val liveSession = nextRace ?: lastRace
            if (liveSession != null) {
                // Fetch latest flag
                val raceControl = F1ApiService.getRaceControl(liveSession.sessionKey)
                val lastFlag = raceControl.lastOrNull { it.category == "Flag" }
                if (lastFlag != null) {
                    saveData(context, "latest_flag", lastFlag)
                }
                saveData(context, "live_session", liveSession)
            }
            
            getPrefs(context).edit().putLong(KEY_LAST_UPDATE, System.currentTimeMillis()).apply()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- Getters ---
    fun getNextRace(context: Context): SessionDto? {
        return loadData(context, KEY_NEXT_RACE)
    }

    fun getDriverStandings(context: Context): List<DriverStandingDto> {
        return loadData(context, KEY_DRIVER_STANDINGS) ?: emptyList()
    }
    
    fun getDrivers(context: Context): List<com.dhruvathaide.gridly.data.remote.model.DriverDto> {
        return loadData(context, KEY_DRIVERS) ?: emptyList()
    }
    
    fun getConstructorStandings(context: Context): List<ConstructorStandingDto> {
        return loadData(context, KEY_CONSTRUCTOR_STANDINGS) ?: emptyList()
    }
    
    fun getLatestFlag(context: Context): com.dhruvathaide.gridly.data.remote.model.RaceControlDto? {
        return loadData(context, "latest_flag")
    }
    
    fun getLiveSession(context: Context): SessionDto? {
        return loadData(context, "live_session")
    }

    // --- Helpers ---
    private inline fun <reified T> saveData(context: Context, key: String, data: T) {
        val jsonString = json.encodeToString(data)
        getPrefs(context).edit().putString(key, jsonString).apply()
    }

    private inline fun <reified T> loadData(context: Context, key: String): T? {
        val jsonString = getPrefs(context).getString(key, null) ?: return null
        return try {
            json.decodeFromString<T>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
