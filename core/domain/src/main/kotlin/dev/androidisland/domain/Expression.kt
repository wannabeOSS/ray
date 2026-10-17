package dev.androidisland.domain

import kotlin.math.*

/** Bounded arithmetic parser. No eval, scripting engine, reflection or I/O. */
object Expression {
    fun evaluate(input: String, x: Double? = null): Result<Double> = runCatching {
        require(input.length in 1..512) { "Use an expression of 1–512 characters" }
        Parser(input.lowercase(), x).parse().also { require(it.isFinite()) { "Result is undefined" } }
    }

    private class Parser(val text: String, val x: Double?) {
        var index = 0
        var depth = 0
        fun parse(): Double {
            val value = sum()
            spaces()
            require(index == text.length) { "Unexpected input at ${index + 1}" }
            return value
        }
        fun spaces() { while (index < text.length && text[index].isWhitespace()) index++ }
        fun take(c: Char): Boolean {
            spaces()
            return if (index < text.length && text[index] == c) { index++; true } else false
        }
        fun sum(): Double {
            var value = product()
            while (true) value = when {
                take('+') -> value + product()
                take('-') -> value - product()
                else -> return value
            }
        }
        fun product(): Double {
            var value = unary()
            while (true) value = when {
                take('*') -> value * unary()
                take('/') -> value / unary()
                else -> return value
            }
        }
        fun unary(): Double {
            require(++depth <= 48) { "Expression is too deeply nested" }
            try {
                if (take('+')) return unary()
                if (take('-')) return -unary()
                val value = primary()
                return if (take('^')) value.pow(unary()) else value
            } finally { depth-- }
        }
        fun primary(): Double {
            spaces()
            if (take('(')) {
                val value = sum()
                require(take(')')) { "Missing closing parenthesis" }
                return value
            }
            val start = index
            while (index < text.length && text[index].isLetter()) index++
            if (index > start) {
                return when (val name = text.substring(start, index)) {
                    "pi" -> PI
                    "e" -> E
                    "x" -> requireNotNull(x) { "x is only available in a graph" }
                    else -> {
                        require(name in setOf("sin", "cos", "tan", "sqrt", "abs", "floor", "ceil", "ln", "log")) { "Unknown function" }
                        require(take('(')) { "Function needs parentheses" }
                        val value = sum()
                        require(take(')')) { "Missing closing parenthesis" }
                        when (name) {
                            "sin" -> sin(value); "cos" -> cos(value); "tan" -> tan(value)
                            "sqrt" -> sqrt(value); "abs" -> abs(value); "floor" -> floor(value)
                            "ceil" -> ceil(value); "ln" -> ln(value); else -> log10(value)
                        }
                    }
                }
            }
            while (index < text.length && (text[index].isDigit() || text[index] == '.')) index++
            require(index > start) { "Expected a number" }
            return text.substring(start, index).toDouble()
        }
    }
}

data class GraphPoint(val x: Double, val y: Double?)
data class GraphSeries(val expression: String, val points: List<GraphPoint>)

object GraphSampler {
    fun sample(query: String, count: Int = 241): List<GraphSeries> {
        require(count in 2..501)
        if (query.length > 512) return emptyList()
        return query.split(',').take(4).mapNotNull { part ->
            val match = Regex("^\\s*y\\d*\\s*=\\s*(.+)$").matchEntire(part) ?: return@mapNotNull null
            val expression = match.groupValues[1]
            // Validate grammar separately from the domain of a particular sample.
            val points = (0 until count).map { i ->
                val x = -10.0 + 20.0 * i / (count - 1)
                GraphPoint(x, Expression.evaluate(expression, x).getOrNull()?.takeIf { abs(it) <= 1000 })
            }
            if (points.none { it.y != null }) null else GraphSeries(expression, points)
        }
    }
}
