package ronsijm.templater.script.builtins

import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.truncate


object MathObject {


    fun execute(methodName: String, args: List<Any?>): Any? {
        return when (methodName) {
            "max" -> {
                val numbers = args.mapNotNull { toNumber(it) }
                if (numbers.isEmpty()) Double.NEGATIVE_INFINITY
                else numbers.maxOf { it.toDouble() }
            }
            "min" -> {
                val numbers = args.mapNotNull { toNumber(it) }
                if (numbers.isEmpty()) Double.POSITIVE_INFINITY
                else numbers.minOf { it.toDouble() }
            }
            "floor" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                floor(num.toDouble()).toInt()
            }
            "ceil" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                ceil(num.toDouble()).toInt()
            }
            "round" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                round(num.toDouble()).toInt()
            }
            "abs" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                abs(num.toDouble()).let { if (it == it.toLong().toDouble()) it.toLong().toInt() else it }
            }
            "sqrt" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                sqrt(num.toDouble())
            }
            "pow" -> {
                val base = toNumber(args.getOrNull(0)) ?: return Double.NaN
                val exp = toNumber(args.getOrNull(1)) ?: return Double.NaN
                base.toDouble().pow(exp.toDouble())
            }
            "random" -> Math.random()
            "sin" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                sin(num.toDouble())
            }
            "cos" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                cos(num.toDouble())
            }
            "tan" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                tan(num.toDouble())
            }
            "log" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                ln(num.toDouble())
            }
            "log10" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                log10(num.toDouble())
            }
            "exp" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                exp(num.toDouble())
            }
            "trunc" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                truncate(num.toDouble()).toInt()
            }
            "sign" -> {
                val num = toNumber(args.getOrNull(0)) ?: return Double.NaN
                sign(num.toDouble()).toInt()
            }
            else -> null
        }
    }


    fun getConstant(name: String): Any? {
        return when (name) {
            "PI" -> PI
            "E" -> E
            "LN2" -> ln(2.0)
            "LN10" -> ln(10.0)
            "LOG2E" -> 1.0 / ln(2.0)
            "LOG10E" -> log10(E)
            "SQRT2" -> sqrt(2.0)
            "SQRT1_2" -> sqrt(0.5)
            else -> null
        }
    }


    fun isSupported(methodName: String): Boolean {
        return methodName in SUPPORTED_METHODS
    }

    private fun toNumber(value: Any?): Number? {
        return when (value) {
            is Number -> value
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    private val SUPPORTED_METHODS = setOf(
        "max", "min", "floor", "ceil", "round", "abs", "sqrt", "pow",
        "random", "sin", "cos", "tan", "log", "log10", "exp", "trunc", "sign"
    )
}
