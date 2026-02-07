package com.dhruvathaide.gridly.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM historical_race_sessions ORDER BY dateStart DESC")
    fun getAllSessions(): Flow<List<HistoricalRaceSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: HistoricalRaceSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaps(laps: List<LapEntity>)

    @Query("SELECT * FROM lap_data WHERE sessionKey = :sessionKey AND driverNumber = :driverNumber")
    fun getLapsForDriver(sessionKey: Int, driverNumber: Int): Flow<List<LapEntity>>
}
