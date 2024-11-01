package com.example.appded.utils

import com.example.appded.player.Player
import races.*
import kotlin.math.floor


////////////////////////////////////////////////////////////////////////////////////////////
// Public constants
////////////////////////////////////////////////////////////////////////////////////////////

val races: Array<Race> = arrayOf(
    Dragonborn(),
    Dwarf(),
    Elf(),
    Gnome(),
    HalfElf(),
    Halfling(),
    HalfOrc(),
    Human(),
    Tiefling()
)
val abilitiesSample: Array<String> = arrayOf(
    "Strength",
    "Dexterity",
    "Constitution",
    "Intelligence",
    "Wisdom",
    "Charisma"
)
val pointCost: Map<Int, Int> = mapOf(
    8 to 0,
    9 to 1,
    10 to 2,
    11 to 3,
    12 to 4,
    13 to 5,
    14 to 7,
    15 to 9
)

////////////////////////////////////////////////////////////////////////////////////////////
// Public functions
////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Print player status
 */
fun getStatus(player: Player): String {
    return buildString {
        append("Name: ${player.name}\n")
        append("Race: ${player.race?.name}\n")
        append("Abilities:\n")
        player.abilities?.forEach { (ability, value) ->
            append("$ability: $value\n")
        }
        append("HP: ${player.healthPoints}")
    }
}

/**
 * Apply the race abilities modifiers to the player
 */
fun setRaceModifiers(player: Player) {
    player.race?.modifiers?.forEach { (ability, modifier) ->
        player.abilities?.set(ability, (player.abilities?.get(ability) ?: 0) + modifier)
    }
}

/**
 * Remove race abilities modifiers, to prevent inconsistency while editing the player
 */
fun unSetRaceModifiers(player: Player) {
    player.race?.modifiers?.forEach { (ability, modifier) ->
        player.abilities?.set(ability, (player.abilities?.get(ability))?.minus(modifier) ?: 8)
    }
}

/**
 * Calculate and apply the player health points
 */
fun setHealthPoints(player: Player) {
    player.healthPoints =
        player.hitDie + constitutionModifier(player.abilities?.get("Constitution")!!)
}

/**
 * Set the selectec abilities into the player
 */
fun assignAbilities(player: Player, abilities: Map<String, Int>) {
    val totalPointsSpent = abilities.entries.sumOf { pointCost[it.value] ?: 0 }
    var pointBuyBalance = 27

    if (totalPointsSpent <= pointBuyBalance) {
        abilities.forEach { (ability, value) ->
            player.abilities?.set(ability, value)
        }
        pointBuyBalance -= totalPointsSpent
    } else {
        throw IllegalArgumentException("Total points spent exceeds available points.")
    }
}


////////////////////////////////////////////////////////////////////////////////////////////
// Private functions
////////////////////////////////////////////////////////////////////////////////////////////

/**
 * Calculate the constitution to be used in HP calcule
 */
private fun constitutionModifier(constitution: Int): Int {
    val result = (constitution - 10).toDouble() / 2
    return floor(result).toInt()
}
