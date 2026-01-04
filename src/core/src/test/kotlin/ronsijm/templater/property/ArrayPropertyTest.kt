package ronsijm.templater.property

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import ronsijm.templater.TestContextFactory
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptEvaluator


class ArrayPropertyTest : FunSpec({
    tags(SlowTag)

    fun createEvaluator(): Pair<ScriptEvaluator, ScriptContext> {
        val context = ScriptContext()
        val registry = TestContextFactory.createModuleRegistry()
        return ScriptEvaluator(context, registry) to context
    }

    context("Array length properties") {

        test("length is non-negative") {
            checkAll(Arb.list(Arb.int(-100..100), 0..20)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val length = evaluator.evaluateExpression("arr.length") as Int
                (length >= 0) shouldBe true
            }
        }

        test("length matches actual list size") {
            checkAll(Arb.list(Arb.int(-100..100), 0..20)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val length = evaluator.evaluateExpression("arr.length") as Int
                length shouldBe list.size
            }
        }
    }

    context("Array reverse properties") {

        test("reverse is involutory: arr.reverse().reverse() == arr") {
            checkAll(Arb.list(Arb.int(-100..100), 0..20)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val reversed = evaluator.evaluateExpression("arr.reverse()") as List<*>
                context.setVariable("reversed", reversed)
                val doubleReversed = evaluator.evaluateExpression("reversed.reverse()") as List<*>

                doubleReversed shouldBe list
            }
        }

        test("reverse preserves length") {
            checkAll(Arb.list(Arb.int(-100..100), 0..20)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val reversed = evaluator.evaluateExpression("arr.reverse()") as List<*>
                reversed.size shouldBe list.size
            }
        }
    }

    context("Array concat properties") {

        test("concat with empty array is identity: arr.concat([]) == arr") {
            checkAll(Arb.list(Arb.int(-100..100), 0..10)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)
                context.setVariable("empty", emptyList<Int>())

                val result = evaluator.evaluateExpression("arr.concat(empty)") as List<*>
                result shouldBe list
            }
        }

        test("concat length: (a.concat(b)).length == a.length + b.length") {
            checkAll(
                Arb.list(Arb.int(-100..100), 0..10),
                Arb.list(Arb.int(-100..100), 0..10)
            ) { a, b ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("a", a)
                context.setVariable("b", b)

                val result = evaluator.evaluateExpression("a.concat(b)") as List<*>
                result.size shouldBe (a.size + b.size)
            }
        }
    }

    context("Array includes/indexOf properties") {

        test("includes returns true for elements in array") {
            checkAll(Arb.list(Arb.int(1..100), 1..10)) { list ->
                val element = list.first()
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)
                context.setVariable("elem", element)

                val result = evaluator.evaluateExpression("arr.includes(elem)")
                result shouldBe true
            }
        }

        test("indexOf returns valid index for elements in array") {
            checkAll(Arb.list(Arb.int(1..100), 1..10)) { list ->
                val element = list.first()
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)
                context.setVariable("elem", element)

                val index = evaluator.evaluateExpression("arr.indexOf(elem)") as Int
                (index >= 0) shouldBe true
                (index < list.size) shouldBe true
            }
        }

        test("indexOf returns -1 for elements not in array") {
            checkAll(Arb.list(Arb.int(1..50), 0..10)) { list ->
                val notInList = 1000
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val index = evaluator.evaluateExpression("arr.indexOf($notInList)")
                index shouldBe -1
            }
        }
    }

    context("Array slice properties") {

        test("slice(0) returns copy of array") {
            checkAll(Arb.list(Arb.int(-100..100), 0..10)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val result = evaluator.evaluateExpression("arr.slice(0)") as List<*>
                result shouldBe list
            }
        }

        test("slice length is bounded by original length") {
            checkAll(Arb.list(Arb.int(-100..100), 1..10), Arb.int(0..5)) { list, start ->
                val safeStart = start.coerceAtMost(list.size)
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val result = evaluator.evaluateExpression("arr.slice($safeStart)") as List<*>
                (result.size <= list.size) shouldBe true
            }
        }
    }

    context("Array filter properties") {

        test("filter result length is at most original length") {
            checkAll(Arb.list(Arb.int(-100..100), 0..20)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val result = evaluator.evaluateExpression("arr.filter(x => x > 0)") as List<*>
                (result.size <= list.size) shouldBe true
            }
        }

        test("filter with always-true predicate returns all elements") {
            checkAll(Arb.list(Arb.int(-100..100), 0..10)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val result = evaluator.evaluateExpression("arr.filter(x => true)") as List<*>
                result shouldBe list
            }
        }

        test("filter with always-false predicate returns empty array") {
            checkAll(Arb.list(Arb.int(-100..100), 0..10)) { list ->
                val (evaluator, context) = createEvaluator()
                context.setVariable("arr", list)

                val result = evaluator.evaluateExpression("arr.filter(x => false)") as List<*>
                result shouldBe emptyList<Any>()
            }
        }
    }
})

