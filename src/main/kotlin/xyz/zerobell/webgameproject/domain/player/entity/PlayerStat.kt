package xyz.zerobell.webgameproject.domain.player.entity

data class PlayerStat(
    val attack: Int,
    val defense: Int,
    val speed: Int,
    val luck: Int
) {
    operator fun plus(other: PlayerStat): PlayerStat {
        return PlayerStat(
            attack + other.attack,
            defense + other.defense,
            speed + other.speed,
            luck + other.luck
        )
    }
}
