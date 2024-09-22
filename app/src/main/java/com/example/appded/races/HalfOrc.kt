package races

class HalfOrc(
    override val name: String = "Half-Orc",
    override val modifiers: Map<String, Int> = mapOf(
        "Strength" to 2,
        "Constitution" to 1
    ),
    override val skills: Array<String> = arrayOf(
        "Darkvision",
        "Menacing",
        "Relentless Endurance",
        "Savage Attacks"
    )
) : Race
