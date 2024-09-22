package races

class Human(
    override val name: String = "Human",
    override val modifiers: Map<String, Int> = mapOf(
        "Strength" to 1,
        "Dexterity" to 1,
        "Constitution" to 1,
        "Intelligence" to 1,
        "Wisdom" to 1,
        "Charisma" to 1
    ),
    override val skills: Array<String> = arrayOf(
        "Extra Language"
    )
) : Race
