package com.example.appded.player

import races.*
import kotlin.math.floor

class PlayerBuilder(
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
    ),
    val abilitiesSample: Array<String> = arrayOf(
        "Strength",
        "Dexterity",
        "Constitution",
        "Intelligence",
        "Wisdom",
        "Charisma"
    ),
    val pointCost: Map<Int, Int> = mapOf(
        8 to 0,
        9 to 1,
        10 to 2,
        11 to 3,
        12 to 4,
        13 to 5,
        14 to 7,
        15 to 9
    ),
    var pointBuyBalance: Int = 27
) {
    internal fun getStatus(player: Player): String {
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

    internal fun setRaceModifiers(player: Player) {
        player.race?.modifiers?.forEach { (ability, modifier) ->
            player.abilities?.set(ability, (player.abilities?.get(ability) ?: 0) + modifier)
        }
    }

    internal fun setHealthPoints(player: Player) {
        player.healthPoints = player.hitDie + constitutionModifier(player.abilities?.get("Constitution")!!)
    }

    private fun constitutionModifier(constitution: Int): Int {
        val result = (constitution - 10).toDouble() / 2
        return floor(result).toInt()
    }

    internal fun assignAbilities(player: Player, abilities: Map<String, Int>) {
        val totalPointsSpent = abilities.entries.sumOf { pointCost[it.value] ?: 0 }

        if (totalPointsSpent <= pointBuyBalance) {
            abilities.forEach { (ability, value) ->
                player.abilities?.set(ability, value)
            }
            pointBuyBalance -= totalPointsSpent
        } else {
            throw IllegalArgumentException("Total points spent exceeds available points.")
        }
    }
}