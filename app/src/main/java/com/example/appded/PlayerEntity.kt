package com.example.appded

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String = "",
    var race: String = "",
    var abilities: String = "",
    var healthPoints: Int = 10
)
