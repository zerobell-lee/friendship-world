package xyz.zerobell.webgameproject.domain.skill.entity

enum class SkillCostType {
    HP, MP, LV, GOLD
}

enum class CostUnit {
    PERCENT, EXACT
}

data class SkillCost(
    val type: SkillCostType,
    val value: Long,
    val unit: CostUnit
) {
    companion object {
        val NONE = SkillCost(
            SkillCostType.HP,
            0L,
            CostUnit.EXACT
        )
    }
}