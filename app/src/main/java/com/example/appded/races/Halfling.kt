package races

class Halfling(
    override val name: String = "Halfling",
    override val modifiers: Map<String, Int> = mapOf(
        "Dexterity" to 2
    ),
    override val skills: Array<String> = arrayOf(
        "Lucky",
        "Brave",
        "Halfling Nimbleness"
    )
) : Race
