// PlayerDao.kt
package com.example.appded

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface PlayerDao {
    @Insert
    suspend fun insert(player: PlayerEntity)

    @Update
    suspend fun update(player: PlayerEntity)

    @Delete
    suspend fun delete(player: PlayerEntity)

    @Query("SELECT * FROM players")
    suspend fun getAllPlayers(): List<PlayerEntity>
}