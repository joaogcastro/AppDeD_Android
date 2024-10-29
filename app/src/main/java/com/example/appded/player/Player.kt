package com.example.appded.player

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import races.Race

class Player (
    var name: String = "",
    var race: Race? = null,
    var abilities: MutableMap<String, Int>? = mutableMapOf(
        "Strength" to 8,
        "Dexterity" to 8,
        "Constitution" to 8,
        "Intelligence" to 8,
        "Wisdom" to 8,
        "Charisma" to 8
    ),
    var healthPoints: Int = 10,
    var hitDie: Int = 10
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "race") val race: String,
    @ColumnInfo(name = "abilities") val abilities: String,
    @ColumnInfo(name = "health_points") val healthPoints: Int,
    @ColumnInfo(name = "hit_die") val hitDie: Int
)