package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Safe equation evaluator using BigDecimal with full operator precedence.
 */
fun evaluateSimpleExpression(expr: String): BigDecimal? {
    val trimmed = expr.trim()
    if (trimmed.isEmpty()) return null
    val lastChar = trimmed.last()
    if (lastChar in "+-*/×÷") return null

    return runCatching {
        val numbers = ArrayList<BigDecimal>()
        val operators = ArrayList<Char>()
        val sb = StringBuilder(16)

        for (i in 0 until trimmed.length) {
            val c = trimmed[i]
            if (c.isDigit() || c == '.') {
                sb.append(c)
            } else if (c in "+-*/×÷") {
                if (sb.isEmpty()) return null
                numbers.add(BigDecimal(sb.toString()))
                sb.setLength(0)
                operators.add(when (c) {
                    '×' -> '*'
                    '÷' -> '/'
                    else -> c
                })
            }
        }
        if (sb.isNotEmpty()) {
            numbers.add(BigDecimal(sb.toString()))
        }

        if (numbers.isEmpty() || numbers.size != operators.size + 1) return null

        // Pass 1: Handle multiplication and division
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

        // Pass 2: Handle addition and subtraction
        var result = numbers2[0]
        for (i in 0 until operators2.size) {
            val op = operators2[i]
            val nextNum = numbers2[i + 1]
            result = if (op == '+') result.add(nextNum) else result.subtract(nextNum)
        }
        result
    }.getOrNull()
}


