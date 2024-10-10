package com.example.appded

import races.*
import kotlin.math.floor

class PlayerBuilder(
    private val races: Array<Race> = arrayOf(
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

    fun create(playerName: String, selectedRace: Race?): Player {
        val player = Player()
        player.name = playerName
        player.race = selectedRace

        selectAbilities(player)
        setRaceModifiers(player)
        setHealthPoints(player)

        return player
    }

    fun getStatus(player: Player): String {
        return buildString {
            append("Name: ${player.name}\n")
            append("Race: ${player.race?.name}\n")
            append("Abilities:\n")
            player.abilities.forEach { (ability, value) ->
                append("$ability: $value\n")
            }
            append("HP: ${player.healthPoints}")
        }
    }

    fun getAvailableRaces(): Array<Race> {
        return races
    }

    internal fun selectAbilities(player: Player) {
        // Lógica para selecionar habilidades (placeholder)
        player.abilities["Strength"] = 10 // Exemplo de atribuição
    }

    internal fun setRaceModifiers(player: Player) {
        player.race?.modifiers?.forEach { (ability, modifier) ->
            player.abilities[ability] = (player.abilities[ability] ?: 0) + modifier
        }
    }

    internal fun setHealthPoints(player: Player) {
        player.healthPoints = player.hitDie + constitutionModifier(player.abilities["Constitution"]!!)
    }

    internal fun constitutionModifier(constitution: Int): Int {
        val result = (constitution - 10).toDouble() / 2
        return floor(result).toInt()
    }

    fun assignAbilities(player: Player, abilities: Map<String, Int>) {
        val totalPointsSpent = abilities.entries.sumOf { pointCost[it.value] ?: 0 }

        if (totalPointsSpent <= pointBuyBalance) {
            abilities.forEach { (ability, value) ->
                player.abilities[ability] = value
            }
            pointBuyBalance -= totalPointsSpent // Atualiza o saldo de pontos
        } else {
            // Aqui você pode adicionar uma mensagem de erro se os pontos ultrapassarem o limite
            throw IllegalArgumentException("Total points spent exceeds available points.")
        }
    }
}