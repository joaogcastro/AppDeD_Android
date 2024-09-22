package races

class Dragonborn(
    override val name: String = "Dragonborn",
    override val modifiers: Map<String, Int> = mapOf(
        "Strength" to 2,
        "Charisma" to 1
    ),
    override val skills: Array<String> = arrayOf(
        "Draconic Ancestry",
        "Breath Weapon",
        "Damage Resistance"
    )
) : Race