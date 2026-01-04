package ronsijm.templater.property

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptEvaluator


class StringPropertyTest : FunSpec({
    tags(SlowTag)

    fun createEvaluator(): Pair<ScriptEvaluator, ScriptContext> {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        return ScriptEvaluator(context, registry) to context
    }


    val safeStringArb = Arb.stringPattern("[a-z]{0,30}")
    val nonEmptySafeStringArb = Arb.stringPattern("[a-z]{1,20}")

    context("String concatenation properties") {

        test("concatenation is associative: (a + b) + c == a + (b + c)") {
            checkAll(safeStringArb, safeStringArb, safeStringArb) { a: String, b: String, c: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("a", a)
                context.setVariable("b", b)
                context.setVariable("c", c)

                val result1 = evaluator.evaluateExpression("(a + b) + c")
                val result2 = evaluator.evaluateExpression("a + (b + c)")
                result1 shouldBe result2
            }
        }

        test("empty string is identity: a + '' == a") {
            checkAll(safeStringArb) { a: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("a", a)

                val result = evaluator.evaluateExpression("a + \"\"")
                result shouldBe a
            }
        }

        test("concatenation length: (a + b).length == a.length + b.length") {
            checkAll(safeStringArb, safeStringArb) { a: String, b: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("a", a)
                context.setVariable("b", b)

                val concat = evaluator.evaluateExpression("a + b") as String
                concat.length shouldBe (a.length + b.length)
            }
        }
    }

    context("String method properties") {

        test("toUpperCase is idempotent: s.toUpperCase().toUpperCase() == s.toUpperCase()") {
            checkAll(safeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val once = evaluator.evaluateExpression("s.toUpperCase()")
                context.setVariable("upper", once)
                val twice = evaluator.evaluateExpression("upper.toUpperCase()")

                once shouldBe twice
            }
        }

        test("toLowerCase is idempotent: s.toLowerCase().toLowerCase() == s.toLowerCase()") {
            checkAll(safeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val once = evaluator.evaluateExpression("s.toLowerCase()")
                context.setVariable("lower", once)
                val twice = evaluator.evaluateExpression("lower.toLowerCase()")

                once shouldBe twice
            }
        }

        test("trim is idempotent: s.trim().trim() == s.trim()") {
            checkAll(safeStringArb) { s: String ->
                val withSpaces = "  $s  "
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", withSpaces)

                val once = evaluator.evaluateExpression("s.trim()")
                context.setVariable("trimmed", once)
                val twice = evaluator.evaluateExpression("trimmed.trim()")

                once shouldBe twice
            }
        }

        test("length is non-negative") {
            checkAll(safeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val length = evaluator.evaluateExpression("s.length") as Int
                (length >= 0) shouldBe true
            }
        }

        test("startsWith self: s.startsWith(s) is true for non-empty strings") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val result = evaluator.evaluateExpression("s.startsWith(s)")
                result shouldBe true
            }
        }

        test("endsWith self: s.endsWith(s) is true for non-empty strings") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val result = evaluator.evaluateExpression("s.endsWith(s)")
                result shouldBe true
            }
        }

        test("includes self: s.includes(s) is true for non-empty strings") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val result = evaluator.evaluateExpression("s.includes(s)")
                result shouldBe true
            }
        }

        test("indexOf self is 0: s.indexOf(s) == 0 for non-empty strings") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", s)

                val result = evaluator.evaluateExpression("s.indexOf(s)")
                result shouldBe 0
            }
        }
    }

    context("String split and join roundtrip") {

        test("split then join with same delimiter returns original (for simple cases)") {
            checkAll(nonEmptySafeStringArb, nonEmptySafeStringArb, nonEmptySafeStringArb) { a: String, b: String, c: String ->
                val original = "$a-$b-$c"
                val (evaluator, context) = createEvaluator()
                context.setVariable("s", original)

                val parts = evaluator.evaluateExpression("s.split(\"-\")") as List<*>
                context.setVariable("parts", parts)
                val rejoined = evaluator.evaluateExpression("parts.join(\"-\")")

                rejoined shouldBe original
            }
        }
    }
})

