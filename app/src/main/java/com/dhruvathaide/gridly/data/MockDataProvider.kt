package com.dhruvathaide.gridly.data

import com.dhruvathaide.gridly.data.remote.model.DriverDto

object MockDataProvider {
    
    // 2026 Season Grid (Matches StandingsFragment)
    val drivers2026 = listOf(
        DriverDto(1, "NOR", "Lando Norris", "NOR", "McLaren", "F58020", null, "GBR"),
        DriverDto(2, "PIA", "Oscar Piastri", "PIA", "McLaren", "F58020", null, "AUS"),
        DriverDto(3, "RUS", "George Russell", "RUS", "Mercedes", "27F4D2", null, "GBR"),
        DriverDto(4, "ANT", "Kimi Antonelli", "ANT", "Mercedes", "27F4D2", null, "ITA"),
        DriverDto(5, "VER", "Max Verstappen", "VER", "Red Bull Racing", "3671C6", null, "NED"),
        DriverDto(6, "HAD", "Isack Hadjar", "HAD", "Red Bull Racing", "3671C6", null, "FRA"),
        DriverDto(7, "LEC", "Charles Leclerc", "LEC", "Ferrari", "F91536", null, "MON"),
        DriverDto(8, "HAM", "Lewis Hamilton", "HAM", "Ferrari", "F91536", null, "GBR"),
        DriverDto(9, "ALB", "Alexander Albon", "ALB", "Williams", "37BEDD", null, "THA"),
        DriverDto(10, "SAI", "Carlos Sainz Jr.", "SAI", "Williams", "37BEDD", null, "ESP"),
        DriverDto(11, "LAW", "Liam Lawson", "LAW", "Racing Bulls", "5E8FAA", null, "NZL"),
        DriverDto(12, "LIN", "Arvid Lindblad", "LIN", "Racing Bulls", "5E8FAA", null, "GBR"),
        DriverDto(13, "ALO", "Fernando Alonso", "ALO", "Aston Martin", "358C75", null, "ESP"),
        DriverDto(14, "STR", "Lance Stroll", "STR", "Aston Martin", "358C75", null, "CAN"),
        DriverDto(15, "BEA", "Oliver Bearman", "BEA", "Haas", "B6BABD", null, "GBR"),
        DriverDto(16, "OCO", "Esteban Ocon", "OCO", "Haas", "B6BABD", null, "FRA"),
        DriverDto(17, "HUL", "Nico Hülkenberg", "HUL", "Audi", "C92D4B", null, "GER"),
        DriverDto(18, "BOR", "Gabriel Bortoleto", "BOR", "Audi", "C92D4B", null, "BRA"),
        DriverDto(19, "GAS", "Pierre Gasly", "GAS", "Alpine", "0090FF", null, "FRA"),
        DriverDto(20, "COL", "Franco Colapinto", "COL", "Alpine", "0090FF", null, "ARG"),
        DriverDto(21, "PER", "Sergio Pérez", "PER", "Cadillac", "FCD116", null, "MEX"),
        DriverDto(22, "BOT", "Valtteri Bottas", "BOT", "Cadillac", "FCD116", null, "FIN")
    )
    
    // Data Classes for Standings (Internal to Mock for now, or could be shared)
    data class DriverStanding(
        val position: Int,
        val driver: DriverDto,
        val points: Int,
        val wins: Int,
        val podiums: Int,
        val recentForm: List<String> // "1", "2", "DNF", etc.
    )

    data class ConstructorStanding(
        val position: Int,
        val teamName: String,
        val teamColour: String,
        val points: Int,
        val driver1: String, // Acronym
        val driver2: String
    )

    // 2026 Driver Standings
    val driverStandings = listOf(
        DriverStanding(1, drivers2026.find { it.nameAcronym == "NOR" }!!, 25, 1, 1, listOf("1", "2", "3", "1", "1")),
        DriverStanding(2, drivers2026.find { it.nameAcronym == "PIA" }!!, 18, 0, 1, listOf("2", "3", "1", "2", "3")),
        DriverStanding(3, drivers2026.find { it.nameAcronym == "RUS" }!!, 15, 0, 1, listOf("3", "1", "2", "4", "5")),
        DriverStanding(4, drivers2026.find { it.nameAcronym == "ANT" }!!, 12, 0, 0, listOf("4", "5", "DNF", "3", "2")),
        DriverStanding(5, drivers2026.find { it.nameAcronym == "VER" }!!, 10, 0, 0, listOf("5", "4", "5", "DNF", "4")),
        DriverStanding(6, drivers2026.find { it.nameAcronym == "HAD" }!!, 8, 0, 0, listOf("6", "7", "6", "5", "6")),
        DriverStanding(7, drivers2026.find { it.nameAcronym == "LEC" }!!, 6, 0, 0, listOf("7", "6", "4", "6", "7")),
        DriverStanding(8, drivers2026.find { it.nameAcronym == "HAM" }!!, 4, 0, 0, listOf("8", "8", "7", "7", "8")),
        DriverStanding(9, drivers2026.find { it.nameAcronym == "ALB" }!!, 2, 0, 0, listOf("9", "9", "8", "8", "9")),
        DriverStanding(10, drivers2026.find { it.nameAcronym == "SAI" }!!, 1, 0, 0, listOf("10", "10", "9", "9", "10")),
        DriverStanding(11, drivers2026.find { it.nameAcronym == "LAW" }!!, 0, 0, 0, listOf("11", "11", "10", "10", "11")),
        DriverStanding(12, drivers2026.find { it.nameAcronym == "LIN" }!!, 0, 0, 0, listOf("12", "12", "11", "11", "12")),
        DriverStanding(13, drivers2026.find { it.nameAcronym == "ALO" }!!, 0, 0, 0, listOf("13", "13", "12", "12", "13")),
        DriverStanding(14, drivers2026.find { it.nameAcronym == "STR" }!!, 0, 0, 0, listOf("14", "14", "13", "13", "14")),
        DriverStanding(15, drivers2026.find { it.nameAcronym == "BEA" }!!, 0, 0, 0, listOf("15", "15", "14", "14", "15")),
        DriverStanding(16, drivers2026.find { it.nameAcronym == "OCO" }!!, 0, 0, 0, listOf("16", "16", "15", "15", "16")),
        DriverStanding(17, drivers2026.find { it.nameAcronym == "HUL" }!!, 0, 0, 0, listOf("17", "17", "16", "16", "17")),
        DriverStanding(18, drivers2026.find { it.nameAcronym == "BOR" }!!, 0, 0, 0, listOf("18", "18", "17", "17", "18")),
        DriverStanding(19, drivers2026.find { it.nameAcronym == "GAS" }!!, 0, 0, 0, listOf("19", "19", "18", "18", "19")),
        DriverStanding(20, drivers2026.find { it.nameAcronym == "COL" }!!, 0, 0, 0, listOf("20", "20", "19", "19", "20")),
        DriverStanding(21, drivers2026.find { it.nameAcronym == "PER" }!!, 0, 0, 0, listOf("21", "21", "20", "20", "21")),
        DriverStanding(22, drivers2026.find { it.nameAcronym == "BOT" }!!, 0, 0, 0, listOf("22", "22", "21", "21", "22"))
    )
    
    // 2026 Constructor Standings
    val constructorStandings = listOf(
        ConstructorStanding(1, "McLaren", "F58020", 43, "NOR", "PIA"),
        ConstructorStanding(2, "Mercedes", "27F4D2", 27, "RUS", "ANT"),
        ConstructorStanding(3, "Red Bull Racing", "3671C6", 18, "VER", "HAD"),
        ConstructorStanding(4, "Ferrari", "F91536", 10, "LEC", "HAM"),
        ConstructorStanding(5, "Williams", "37BEDD", 3, "ALB", "SAI"),
        ConstructorStanding(6, "Racing Bulls", "5E8FAA", 0, "LAW", "LIN"),
        ConstructorStanding(7, "Aston Martin", "358C75", 0, "ALO", "STR"),
        ConstructorStanding(8, "Haas", "B6BABD", 0, "BEA", "OCO"),
        ConstructorStanding(9, "Audi", "C92D4B", 0, "HUL", "BOR"),
        ConstructorStanding(10, "Alpine", "0090FF", 0, "GAS", "COL"),
        ConstructorStanding(11, "Cadillac", "FCD116", 0, "PER", "BOT")
    )
    
    fun getDrivers(): List<DriverDto> = drivers2026
    
    // Circuit Data
    data class SessionSchedule(
        val name: String,
        val day: String,
        val time: String,
        val status: String // "UPCOMING", "LIVE", "COMPLETED"
    )
    
    data class WeatherForecast(
        val type: String, // "SUNNY", "CLOUDY", "RAIN"
        val temp: String, // "28°C"
        val chanceOfRain: String // "10%"
    )
    
    val mockWeekendSchedule = listOf(
        SessionSchedule("PRACTICE 1", "FRI", "13:30", "COMPLETED"),
        SessionSchedule("PRACTICE 2", "FRI", "17:00", "COMPLETED"),
        SessionSchedule("PRACTICE 3", "SAT", "12:30", "UPCOMING"),
        SessionSchedule("QUALIFYING", "SAT", "16:00", "UPCOMING"),
        SessionSchedule("RACE", "SUN", "15:00", "UPCOMING")
    )
    
    val mockWeather = WeatherForecast("SUNNY", "28°C", "10%")
    
    // Mock Session for Demo (Monaco 2026)
    val mockSession = com.dhruvathaide.gridly.data.remote.model.SessionDto(
        sessionKey = 9999,
        meetingKey = 9999,
        circuitKey = 9999,
        circuitShortName = "Monaco",
        countryCode = "MON",
        countryKey = 9999,
        countryName = "Monaco",
        dateEnd = "2026-05-24T15:00:00",
        dateStart = "2026-05-22T13:30:00",
        gmtOffset = "+02:00",
        location = "Monte Carlo",
        sessionName = "Race",
        sessionType = "Race",
        year = 2026
    )
    
    // News Data for Home Screen
    data class NewsItem(
        val id: Int,
        val title: String,
        val subtitle: String,
        val timeAgo: String,
        val category: String, // "RACE", "TECH", "RUMOR"
        val categoryColor: String, // Hex
        val url: String = "https://www.formula1.com" // Default fallback
    )
    
    val mockNews = listOf(
        NewsItem(1, "VERSTAPPEN TAKES POLE", "Red Bull dominates Monaco qualifying by 0.2s.", "1h ago", "QUALIFYING", "3671C6", "https://www.formula1.com"),
        NewsItem(2, "AUDI'S SECRET UPGRADE", "Hülkenberg spotted with new floor design.", "3h ago", "TECH", "C92D4B", "https://www.formula1.com"),
        NewsItem(3, "HAMILTON TO FERRARI?", "Rumors swirl as 2027 contract talks stall.", "5h ago", "RUMOR", "F91536", "https://www.formula1.com"),
        NewsItem(4, "NORRIS P4 START", "McLaren confident in race pace for Sunday.", "6h ago", "INTERVIEW", "F58020", "https://www.formula1.com"),
        NewsItem(5, "MONACO WEATHER UPDATE", "60% chance of rain for race start.", "8h ago", "WEATHER", "00E5FF", "https://www.formula1.com")
    )

    // Mock Strategy Data
    val mockStints = listOf(
        // Norris (Two stop: S -> M -> S)
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 4, 1, "SOFT", 1, 25, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 4, 2, "MEDIUM", 26, 55, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 4, 3, "SOFT", 56, 78, 0),
        
        // Piastri (One stop: M -> H)
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 81, 1, "MEDIUM", 1, 35, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 81, 2, "HARD", 36, 78, 0),
        
        // Russell
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 63, 1, "SOFT", 1, 28, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 63, 2, "HARD", 29, 78, 0),

        // Verstappen
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 1, 1, "MEDIUM", 1, 30, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 1, 2, "HARD", 31, 60, 0),
        com.dhruvathaide.gridly.data.remote.model.StintDto(9999, 9999, 1, 3, "SOFT", 61, 78, 0)
    )

    // Mock Team Radio
    // Updated to match DTO: sessionKey, meetingKey, date, driverNumber, recordingUrl
    val mockTeamRadio = listOf(
        com.dhruvathaide.gridly.data.remote.model.TeamRadioDto(9999, 9999, "2026-05-24T14:05:00", 4, "https://www.soundboard.com/handler/DownLoadTrack.ashx?cliptitle=Check+Radio&filename=24/249910-fe1d2797-e85d-4f32-8438-e48f65319889.mp3"),
        com.dhruvathaide.gridly.data.remote.model.TeamRadioDto(9999, 9999, "2026-05-24T14:15:00", 1, "https://www.soundboard.com/handler/DownLoadTrack.ashx?cliptitle=Check+Radio&filename=24/249910-fe1d2797-e85d-4f32-8438-e48f65319889.mp3"),
        com.dhruvathaide.gridly.data.remote.model.TeamRadioDto(9999, 9999, "2026-05-24T14:25:00", 63, "https://www.soundboard.com/handler/DownLoadTrack.ashx?cliptitle=Check+Radio&filename=24/249910-fe1d2797-e85d-4f32-8438-e48f65319889.mp3")
    )
    
    // Mock Telemetry (Generate a sine wave for simple visualisation)
    fun getMockTelemetry(driverNumber: Int): List<com.dhruvathaide.gridly.data.remote.model.TelemetryDto> {
        val baseSpeed = if (driverNumber == 1) 280 else 275
        return (1..100).map { i ->
            val speed = baseSpeed + (Math.sin(i.toDouble() / 10) * 30).toInt() + (0..5).random()
            val throttle = if (speed > 200) 100 else (speed / 2)
            val brake = if (speed < 100) 100 else 0
            val rpm = 10000 + (speed * 10)
            com.dhruvathaide.gridly.data.remote.model.TelemetryDto(
                date = "2026-05-24T14:00:00",
                driverNumber = driverNumber,
                rpm = rpm,
                speed = speed,
                throttle = throttle,
                brake = brake,
                gear = 8, // Fixed param name
                drs = 0,
                sessionKey = 9999 
                // Note: TelemetryDto definition has sessionKey but NOT meetingKey based on ApiModels.kt line 7-17
                // Wait, checking TelemetryDto again:
                // date, driverNumber, sessionKey, speed, throttle, brake, rpm, gear, drs.
                // It DOES NOT have meetingKey.
            )
        }
    }
}
