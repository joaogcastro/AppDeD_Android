package com.example.appded

import races.Race

class Player {
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