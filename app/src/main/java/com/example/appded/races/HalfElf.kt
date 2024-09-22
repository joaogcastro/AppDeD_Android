package races

class HalfElf(
    override val name: String = "Half-Elf",
    override val modifiers: Map<String, Int> = mapOf(
        "Charisma" to 2,
        "Dexterity" to 1,
        "Wisdom" to 1
    ),
    override val skills: Array<String> = arrayOf(
        "Darkvision",
        "Fey Ancestry",
        "Skill Versatility"
    )
) : Race
