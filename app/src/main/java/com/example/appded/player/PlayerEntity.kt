package com.example.appded.player

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "race") val race: String,
    @ColumnInfo(name = "abilities") val abilities: String,
    @ColumnInfo(name = "health_points") val healthPoints: Int,
    @ColumnInfo(name = "hit_die") val hitDie: Int
)
