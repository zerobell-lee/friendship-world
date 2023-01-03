package xyz.zerobell.webgameproject.domain.skill.entity

import xyz.zerobell.webgameproject.domain.element.entity.ElementalType
import xyz.zerobell.webgameproject.domain.player.entity.Player

class Skill(
    val id: Long,
    val name: String,
    val type: SkillType,
    val cost: SkillCost,
    val element: ElementalType,
    private val damageFormula: DamageFormula
) {
    companion object {
        val ATTACK: Skill = Skill(
            0L,
            "Attack",
            SkillType.COMBAT,
            SkillCost.NONE,
            ElementalType.PHY,
            DamageFormulaParser.parse("a.atk * 2 - b.def")
        )
    }

    fun calcEffect(playerA: Player, playerB: Player): Double {
        return damageFormula.exec(playerA, playerB)
    }
}