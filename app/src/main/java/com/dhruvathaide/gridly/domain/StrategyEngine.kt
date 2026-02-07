package com.dhruvathaide.gridly.domain

import com.dhruvathaide.gridly.domain.model.DriverPosition
import com.dhruvathaide.gridly.domain.model.Lap

class StrategyEngine {

    /**
     * Simulates Safety Car conditions.
     * 
     * @param currentField List of drivers and their current status.
     * @param pitLossNormal Expected time lost in pits under Green Flag conditions (default 23.0s).
     * @return A map of Driver Number to their potential "Net Gain" if they pit now under SC.
     */
    fun simulateSafetyCar(
        currentField: List<DriverPosition>, 
        pitLossNormal: Double = 23.0
    ): Map<Int, Double> {
        val scSpeedFactor = 0.6 // 40% speed reduction means 60% speed
        
        // Logic: 
        // 1. Pit Loss is time spent essentially stationary relative to race pace.
        // 2. Under VSC/SC, race pace is slower (times are longer).
        // 3. However, the pit lane time loss is roughly constant (excluding in-lap/out-lap pace diffs).
        // 4. Simplified "Net Gain" calculation:
        //    If you lose 23s at normal speed, you lose less "relative track position" at SC speed.
        //    Relative Time Saving = PitLossNormal * (1 - SC_Speed_Factor) ??
        //    Actually, common wisdom: "Pit stops are ~40-50% cheaper under SC".
        //    If pack speed is 60%, they cover 0.6x distance in the same time.
        //    So you lose 40% less distance relative to them.
        
        val estimatedSaving = pitLossNormal * (1.0 - scSpeedFactor)
        
        return currentField.associate { driver ->
            driver.driverNumber to estimatedSaving
        }
    }

    /**
     * Identifies the Tire Cliff.
     * 
     * @param lapData List of laps for a specific stint.
     * @return The lap number where the cliff started, or null if no cliff detected.
     */
    fun calculateTireCliff(lapData: List<Lap>): Int? {
        if (lapData.size < 5) return null // Need enough data

        // Calculate median of the stint
        val times = lapData.map { it.lapTime }.sorted()
        val median = if (times.size % 2 == 0) {
            (times[times.size / 2] + times[times.size / 2 - 1]) / 2.0
        } else {
            times[times.size / 2]
        }

        // Check rolling average of last 3 laps
        for (i in 3 until lapData.size) {
            val window = lapData.subList(i - 2, i + 1)
            val rollingAvg = window.map { it.lapTime }.average()
            
            if (rollingAvg > median + 0.5) {
                // Return the first lap of this window that started the trend? Or the last?
                // Returning the last lap of the window as the confirmation point.
                return lapData[i].lapNumber
            }
        }
        
        return null
    }
}
