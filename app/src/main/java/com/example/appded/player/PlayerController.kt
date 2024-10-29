package com.example.appded.player

import com.example.appded.database.PlayerDao
import races.Race
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import races.*

class PlayerController (private val playerDao: PlayerDao){
    private val gson = Gson()

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Public functions
    ////////////////////////////////////////////////////////////////////////////////////////////

     suspend fun save(player: Player) {
         val playerEntity = PlayerEntity(
             name = player.name,
             race = convertRace(player.race),
             abilities = convertAbilities(player.abilities),
             healthPoints = player.healthPoints,
             hitDie = player.hitDie
         )
        playerDao.insert(playerEntity)
     }

    suspend fun listAll(): List<Player> {
        val playersEntity: List<PlayerEntity> = playerDao.getAllPlayers()
        val players: MutableList<Player> = mutableListOf()

        for (playerE in playersEntity) {
            players.add(Player(
                name = playerE.name,
                race = convertRace(playerE.race),
                abilities = convertAbilities(playerE.abilities),
                healthPoints = playerE.healthPoints,
                hitDie = playerE.hitDie
            ))
        }

        return players
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Private functions
    ////////////////////////////////////////////////////////////////////////////////////////////

    private fun convertRace(race: Race?): String {
        return gson.toJson(race)
    }

    private fun convertRace(race: String): Race? {
        val raceType = getRaceName(race)

        return when (raceType) {
            "Dragonborn" -> gson.fromJson(race, Dragonborn::class.java)
            "Dwarf" -> gson.fromJson(race, Dwarf::class.java)
            "Elf" -> gson.fromJson(race, Elf::class.java)
            "Gnome" -> gson.fromJson(race, Gnome::class.java)
            "HalfElf" -> gson.fromJson(race, HalfElf::class.java)
            "Halfling" -> gson.fromJson(race, Halfling::class.java)
            "HalfOrc" -> gson.fromJson(race, HalfOrc::class.java)
            "Human" -> gson.fromJson(race, Human::class.java)
            "Tiefling" -> gson.fromJson(race, Tiefling::class.java)
            else -> null
        }
    }

    private fun getRaceName(json: String): String? {
        val regex = """"name"\s*:\s*["'](.*?)["']""".toRegex()
        return regex.find(json)?.groups?.get(1)?.value
    }

    private fun convertAbilities(abilities: MutableMap<String, Int>?): String {
        return gson.toJson(abilities)
    }

    private fun convertAbilities(json: String): MutableMap<String, Int>? {
        return try {
            gson.fromJson(json, object : TypeToken<MutableMap<String, Int>>() {}.type)
        } catch (e: Exception) {
            null
        }
    }
}