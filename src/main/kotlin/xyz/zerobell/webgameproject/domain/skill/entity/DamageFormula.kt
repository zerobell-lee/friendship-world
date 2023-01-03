package xyz.zerobell.webgameproject.domain.skill.entity

import xyz.zerobell.webgameproject.domain.player.entity.Player
import java.util.Stack
import kotlin.IllegalStateException

class DamageFormula(private val reusableCalcQueue: List<CalcQueueItem>) {
    fun exec(a: Player, b: Player): Double {
        val stack = Stack<Double>()
        var cursor = 0

        while (cursor < reusableCalcQueue.size) {
            when (val item = reusableCalcQueue[cursor]) {
                DamageOperators.PLUS -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left + right)
                }
                DamageOperators.MINUS -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left - right)
                }
                DamageOperators.MULTIPLY -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left * right)
                }
                DamageOperators.DIVIDE -> {
                    val right = stack.pop()
                    val left = stack.pop()
                    stack.push(left / right)
                }
                is DamageOperand -> {
                    stack.push(getAttribute(item, a, b))
                }
                else -> throw IllegalArgumentException("Unknown operator")
            }
            cursor++
        }

        return stack.pop()
    }

    private fun getAttribute(operand: DamageOperand, a: Player, b: Player): Double {
        return when (operand.target) {
            OperandTarget.A -> operand.attribute(a)
            OperandTarget.B -> operand.attribute(b)
            OperandTarget.NUMBER -> operand.amount!!
        }
    }
}

interface CalcQueueItem

object DamageFormulaParser {
    internal enum class ParsingStatus {
        TARGET_PARSED, PARSING_PROPERTY, PROPERTY_PARSED, OPERATOR_PARSED
    }

    fun parse(formula: String): DamageFormula {
        val trimmedFormula = formula.replace(" ", "").uppercase()

        val resultQueue = mutableListOf<CalcQueueItem>()
        var cursor = 0
        var status = ParsingStatus.OPERATOR_PARSED
        var parsingTarget: OperandTarget? = null
        var propertyBuffer = ""
        val stack = Stack<CalcQueueItem>()
        while (cursor != trimmedFormula.length) {
            val charAtCursor = trimmedFormula[cursor]
            when (status) {
                ParsingStatus.OPERATOR_PARSED -> {
                    when (charAtCursor) {
                        '(' -> {
                            stack.push(DamageOperators.PARENTHESIS_START)
                        }
                        'A', 'B' -> {
                            parsingTarget = OperandTarget.valueOf(charAtCursor.toString())
                            status = ParsingStatus.TARGET_PARSED
                        }
                        in '0'..'9' -> {
                            parsingTarget = OperandTarget.NUMBER
                            propertyBuffer += charAtCursor
                            status = ParsingStatus.PARSING_PROPERTY
                        }
                        else -> throw IllegalStateException("Impossible character")
                    }
                    cursor++
                    continue
                }

                ParsingStatus.TARGET_PARSED -> {
                    if (charAtCursor != '.') throw IllegalStateException("dot(.) should come after target $parsingTarget")
                    status = ParsingStatus.PARSING_PROPERTY
                    cursor++
                    continue
                }

                ParsingStatus.PARSING_PROPERTY -> {
                    val isFormulaEnded = cursor == trimmedFormula.length - 1
                    var isParsingEnded: Boolean = isFormulaEnded

                    when(charAtCursor) {
                        in '0'..'9' -> {
                            if (parsingTarget != OperandTarget.NUMBER) throw IllegalStateException("Number cannot come while parsing property")
                            propertyBuffer += charAtCursor
                        }

                        in 'A'..'Z' -> {
                            if (parsingTarget == OperandTarget.NUMBER) throw IllegalStateException("Alphabet cannot come while parsing number")
                            propertyBuffer += charAtCursor
                        }

                        else -> {
                            isParsingEnded = true
                        }
                    }

                    if (isParsingEnded) {
                        if (parsingTarget == OperandTarget.NUMBER) {
                            val number = propertyBuffer.toDouble()
                            resultQueue.add(DamageOperand(parsingTarget, OperandProperty.CONST, number))
                        } else {
                            val property = OperandProperty.valueOf(propertyBuffer)
                            resultQueue.add(DamageOperand(parsingTarget!!, property))
                        }
                        parsingTarget = null
                        propertyBuffer = ""
                        status = ParsingStatus.PROPERTY_PARSED

                        if (isFormulaEnded) {
                            when(charAtCursor) {
                                '+', '-', '*', '/', '(' -> throw IllegalStateException("Formula cannot end with $charAtCursor")
                                ')' -> continue
                            }
                            cursor++
                        }
                        continue
                    }

                    cursor++
                    continue
                }
                ParsingStatus.PROPERTY_PARSED -> {
                    when (charAtCursor) {
                        '+', '-' -> {
                            while (stack.isNotEmpty() && stack.peek() != DamageOperators.PARENTHESIS_START) {
                                resultQueue.add(stack.pop())
                            }
                            stack.push(DamageOperators.bySymbol(charAtCursor))
                            status = ParsingStatus.OPERATOR_PARSED
                        }

                        '*', '/' -> {
                            stack.push(DamageOperators.bySymbol(charAtCursor))
                            status = ParsingStatus.OPERATOR_PARSED
                        }

                        ')' -> {
                            while (stack.peek() != DamageOperators.PARENTHESIS_START) {
                                resultQueue.add(stack.pop())
                            }
                            stack.pop()
                            status = ParsingStatus.PROPERTY_PARSED
                        }
                    }
                    cursor++
                }
            }
        }

        while (stack.isNotEmpty()) {
            resultQueue.add(stack.pop())
        }

        return DamageFormula(resultQueue.toList())
    }
}

data class DamageOperand(
    val target: OperandTarget,
    val property: OperandProperty,
    val amount: Double? = null
): CalcQueueItem {
    fun attribute(player: Player): Double {
        if (isNumber()) throw IllegalStateException("This operand is number. can't get an attribute")

        return when (property) {
            OperandProperty.ATK -> player.stat.attack.toDouble()
            OperandProperty.DEF -> player.stat.defense.toDouble()
            OperandProperty.SPD -> player.stat.speed.toDouble()
            OperandProperty.LUK -> player.stat.luck.toDouble()
            else -> TODO()
        }
    }

    private fun isNumber(): Boolean = target == OperandTarget.NUMBER
}

enum class OperandTarget {
    A, B, NUMBER;
}

enum class OperandProperty {
    ATK, DEF, SPD, LUK, HP, MHP, MP, MMP, LV, G, CONST;
}

enum class DamageOperators(val symbol: Char): CalcQueueItem {
    PLUS('+'), MINUS('-'), MULTIPLY('*'), DIVIDE('/'), PARENTHESIS_START('(');

    companion object {
        fun bySymbol(symbol: Char): DamageOperators {
            return values().find { it.symbol == symbol }!!
        }
    }
}