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
        playerDao.insert(convertToEntity(player))
     }

    suspend fun update(player: Player) {
        playerDao.update(convertToEntity(player))
    }

    suspend fun delete(player: Player) {
        playerDao.delete(convertToEntity(player))
    }

    suspend fun getPlayerById(id: Int): Player? {
        val playerEntity = playerDao.getPlayerById(id)
        if (playerEntity != null) {
            return convertToClass(playerEntity)
        }
        return null
    }

    suspend fun listAll(): List<Player> {
        val playersEntity: List<PlayerEntity> = playerDao.getAllPlayers()
        val players: MutableList<Player> = mutableListOf()

        for (playerE in playersEntity) {
            players.add(convertToClass(playerE))
        }

        return players
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // Private functions
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Convert Player object to PlayerEntity object
     */
    private fun convertToEntity(player: Player): PlayerEntity {
        return PlayerEntity(
            id = player.id,
            name = player.name,
            race = convertRace(player.race),
            abilities = convertAbilities(player.abilities),
            healthPoints = player.healthPoints,
            hitDie = player.hitDie
        )
    }

    /**
     * Convert PlayerEntity object to Player object
     */
    private fun convertToClass(playerEntity: PlayerEntity): Player {
        return Player(
            id = playerEntity.id,
            name = playerEntity.name,
            race = convertRace(playerEntity.race),
            abilities = convertAbilities(playerEntity.abilities),
            healthPoints = playerEntity.healthPoints,
            hitDie = playerEntity.hitDie
        )
    }

    /**
     * Convert Race object to Json String
     */
    private fun convertRace(race: Race?): String {
        return gson.toJson(race)
    }

    /**
     * Convert Json String to Race object
     */
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

    /**
     * Convert Abilities Map to Json String
     */
    private fun convertAbilities(abilities: MutableMap<String, Int>?): String {
        return gson.toJson(abilities)
    }

    /**
     * Convert Json String to Abilities Map
     */
    private fun convertAbilities(json: String): MutableMap<String, Int>? {
        return try {
            gson.fromJson(json, object : TypeToken<MutableMap<String, Int>>() {}.type)
        } catch (e: Exception) {
            null
        }
    }
}