
package com.example.appded

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import races.*

class Converters {

    private val gson = Gson()

    // Converte uma raça para String
    @TypeConverter
    fun fromRace(race: Race?): String? {
        return race?.javaClass?.simpleName
    }

    // Converte uma String para raça
    @TypeConverter
    fun toRace(raceName: String?): Race? {
        return when (raceName) {
            "Dragonborn" -> Dragonborn()
            "Dwarf" -> Dwarf()
            "Elf" -> Elf()
            "Gnome" -> Gnome()
            "HalfElf" -> HalfElf()
            "Halfling" -> Halfling()
            "HalfOrc" -> HalfOrc()
            "Human" -> Human()
            "Tiefling" -> Tiefling()
            else -> null
        }
    }

    // Converte o mapa de habilidades para String
    @TypeConverter
    fun fromAbilitiesMap(map: Map<String, Int>?): String? {
        return gson.toJson(map)
    }

    // Converte a String de volta para o mapa de habilidades
    @TypeConverter
    fun toAbilitiesMap(data: String?): Map<String, Int>? {
        return if (data == null) {
            null
        } else {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(data, type)
        }
    }

    class Converters {
        @TypeConverter
        fun fromAbilitiesMap(value: Map<String, Int>?): String? {
            return Gson().toJson(value)
        }

        @TypeConverter
        fun toAbilitiesMap(value: String?): Map<String, Int>? {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            return Gson().fromJson(value, type)
        }
    }

}
