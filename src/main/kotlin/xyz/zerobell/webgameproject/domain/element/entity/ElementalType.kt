package xyz.zerobell.webgameproject.domain.element.entity

data class ElementalType(
    val name: String,
    val description: String,
    val icon: String
) {
    companion object {
        val PHY = ElementalType(
            "물리", "물리", ""
        )
    }
}
