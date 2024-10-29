package com.example.appded.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Query
import com.example.appded.player.PlayerEntity

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
