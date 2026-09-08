package com.smartledger.aldaftar.domain

import java.math.BigDecimal
import java.math.RoundingMode

fun evaluateSimpleExpression(expr: String): BigDecimal? {
    val trimmed = expr.trim()
    if (trimmed.isEmpty()) return null

    return runCatching {
        val numbers = ArrayList<BigDecimal>()
        val operators = ArrayList<Char>()
        val sb = StringBuilder(16)

        var seenDotInCurrentToken = false

        for (i in 0 until trimmed.length) {
            val c = trimmed[i]
            when {
                c.isWhitespace() -> continue
                c in '0'..'9' -> sb.append(c)
                c in '٠'..'٩' -> sb.append((c - '٠' + '0'.code).toChar())
                c in '۰'..'۹' -> sb.append((c - '۰' + '0'.code).toChar())
                c == '.' || c == ',' || c == '٫' -> {
                    if (!seenDotInCurrentToken) {
                        sb.append('.')
                        seenDotInCurrentToken = true
                    } else {
                        return null
                    }
                }
                c in "+-*/×÷" -> {
                    if (sb.isEmpty()) {
                        if (c == '-' && numbers.isEmpty()) {
                            sb.append('-')
                            continue
                        }
                        return null
                    }
                    val numStr = sb.toString()
                    if (numStr == "-" || numStr == "." || numStr == "-.") return null
                    numbers.add(BigDecimal(numStr))
                    sb.setLength(0)
                    seenDotInCurrentToken = false

                    operators.add(when (c) {
                        '×' -> '*'
                        '÷' -> '/'
                        else -> c
                    })
                }
                else -> return null // محرف غير معروف يؤدي لرفض التعبير
            }
        }
        if (sb.isNotEmpty()) {
            val numStr = sb.toString()
            if (numStr == "-" || numStr == "." || numStr == "-.") return null
            numbers.add(BigDecimal(numStr))
        }

        if (numbers.isEmpty() || numbers.size != operators.size + 1) return null

        val numbers2 = ArrayList<BigDecimal>(numbers.size)
        val operators2 = ArrayList<Char>(operators.size)
        numbers2.add(numbers[0])

        for (i in 0 until operators.size) {
            val op = operators[i]
            val nextNum = numbers[i + 1]
            if (op == '*' || op == '/') {
                val prevNum = numbers2.removeAt(numbers2.size - 1)
                val res = if (op == '*') {
                    prevNum.multiply(nextNum)
                } else {
                    if (nextNum.compareTo(BigDecimal.ZERO) == 0) return null
                    prevNum.divide(nextNum, 10, RoundingMode.HALF_UP)
                }
                numbers2.add(res)
            } else {
                operators2.add(op)
                numbers2.add(nextNum)
            }
        }

        var result = numbers2[0]
        for (i in 0 until operators2.size) {
            val op = operators2[i]
            val nextNum = numbers2[i + 1]
            result = if (op == '+') result.add(nextNum) else result.subtract(nextNum)
        }
        result
    }.getOrNull()
}
