package ronsijm.templater.property

import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptEvaluator


object SlowTag : Tag()


class ArithmeticPropertyTest : FunSpec({
    tags(SlowTag)

    fun createEvaluator(): ScriptEvaluator {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        return ScriptEvaluator(context, registry)
    }

    context("Integer arithmetic properties") {

        test("addition is commutative: a + b == b + a") {
            checkAll(Arb.int(-10000..10000), Arb.int(-10000..10000)) { a, b ->
                val evaluator = createEvaluator()
                val result1 = evaluator.evaluateExpression("$a + $b")
                val result2 = evaluator.evaluateExpression("$b + $a")
                result1 shouldBe result2
            }
        }

        test("multiplication is commutative: a times b equals b times a") {
            checkAll(Arb.int(-1000..1000), Arb.int(-1000..1000)) { a, b ->
                val evaluator = createEvaluator()
                val result1 = evaluator.evaluateExpression("$a * $b")
                val result2 = evaluator.evaluateExpression("$b * $a")
                result1 shouldBe result2
            }
        }

        test("addition has identity element 0: a + 0 == a") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a + 0")
                result shouldBe a
            }
        }

        test("multiplication has identity element 1: a times 1 equals a") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a * 1")
                result shouldBe a
            }
        }

        test("multiplication by 0 yields 0: a times 0 equals 0") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a * 0")
                result shouldBe 0
            }
        }

        test("subtraction is inverse of addition: (a + b) - b == a") {
            checkAll(Arb.int(-5000..5000), Arb.int(-5000..5000)) { a, b ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("($a + $b) - $b")
                result shouldBe a
            }
        }

        test("negation: a - a == 0") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a - $a")
                result shouldBe 0
            }
        }
    }

    context("Comparison properties") {

        test("equality is reflexive: a == a is always true") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a == $a")
                result shouldBe true
            }
        }

        test("inequality is irreflexive: a != a is always false") {
            checkAll(Arb.int(-10000..10000)) { a ->
                val evaluator = createEvaluator()
                val result = evaluator.evaluateExpression("$a != $a")
                result shouldBe false
            }
        }

        test("less than is asymmetric: if a < b then not (b < a)") {
            checkAll(Arb.int(-10000..10000), Arb.int(-10000..10000)) { a, b ->
                if (a != b) {
                    val evaluator = createEvaluator()
                    val aLtB = evaluator.evaluateExpression("$a < $b") as Boolean
                    val bLtA = evaluator.evaluateExpression("$b < $a") as Boolean
                    if (aLtB) bLtA shouldBe false
                    if (bLtA) aLtB shouldBe false
                }
            }
        }

        test("trichotomy: exactly one of a < b, a == b, a > b is true") {
            checkAll(Arb.int(-10000..10000), Arb.int(-10000..10000)) { a, b ->
                val evaluator = createEvaluator()
                val lt = evaluator.evaluateExpression("$a < $b") as Boolean
                val eq = evaluator.evaluateExpression("$a == $b") as Boolean
                val gt = evaluator.evaluateExpression("$a > $b") as Boolean

                val trueCount = listOf(lt, eq, gt).count { it }
                trueCount shouldBe 1
            }
        }

        test("less than or equal: a <= b iff a < b or a == b") {
            checkAll(Arb.int(-10000..10000), Arb.int(-10000..10000)) { a, b ->
                val evaluator = createEvaluator()
                val leq = evaluator.evaluateExpression("$a <= $b") as Boolean
                val lt = evaluator.evaluateExpression("$a < $b") as Boolean
                val eq = evaluator.evaluateExpression("$a == $b") as Boolean

                leq shouldBe (lt || eq)
            }
        }
    }

    context("Double arithmetic properties") {

        test("division and multiplication are inverse: (a times b) div b equals a (for non-zero b)") {

            checkAll(Arb.double(-100.0..100.0), Arb.double(1.0..100.0)) { a, b ->

                if (a.isFinite() && b.isFinite() && kotlin.math.abs(a) > 1e-10) {
                    val evaluator = createEvaluator()
                    val result = evaluator.evaluateExpression("($a * $b) / $b")
                    if (result != null) {
                        val resultDouble = (result as Number).toDouble()
                        resultDouble shouldBe (a plusOrMinus 0.001)
                    }
                }
            }
        }
    }
})

