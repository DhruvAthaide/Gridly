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
    
    fun getDrivers(): List<DriverDto> = drivers2026
}
