package races

class Elf(
    override val name: String = "Elf",
    override val modifiers: Map<String, Int> = mapOf(
        "Dexterity" to 2
    ),
    override val skills: Array<String> = arrayOf(
        "Darkvision",
        "Keen Senses",
        "Fey Ancestry",
        "Trance"
    )
) : Race