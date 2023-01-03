package xyz.zerobell.webgameproject.domain.player.entity

class Player(
    val id: Long,
    val name: String,
    basicStat: PlayerStat,
    additionalStat: PlayerStat,
    var profileImage: String
) {
    var basicStat: PlayerStat = basicStat
        private set
    var additionalStat: PlayerStat = additionalStat
        private set

    val stat: PlayerStat get() = basicStat + additionalStat
}