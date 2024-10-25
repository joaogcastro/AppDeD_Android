package com.example.appded

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import races.Race

@Entity(tableName = "players")
@TypeConverters(Converters::class)
class Player {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
    var name: String = ""
    var race: Race? = null
    var abilities: MutableMap<String, Int> = mutableMapOf(
        "Strength" to 8,
        "Dexterity" to 8,
        "Constitution" to 8,
        "Intelligence" to 8,
        "Wisdom" to 8,
        "Charisma" to 8
    )
    var healthPoints: Int = 10
    var hitDie: Int = 10
}