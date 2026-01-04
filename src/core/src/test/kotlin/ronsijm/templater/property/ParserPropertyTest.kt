package ronsijm.templater.property

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import ronsijm.templater.TestContextFactory
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.ServiceContainer


class ParserPropertyTest : FunSpec({
    tags(SlowTag)

    fun createParser(): TemplateParser {
        return TemplateParser(validateSyntax = false, services = ServiceContainer.createForTesting())
    }

    fun createContext(): TemplateContext {
        return TestContextFactory.create()
    }


    val safeStringArb = Arb.stringPattern("[a-z]{0,50}")
    val nonEmptySafeStringArb = Arb.stringPattern("[a-z]{1,20}")

    context("Plain text passthrough") {

        test("text without template markers passes through unchanged") {
            checkAll(safeStringArb) { text: String ->
                val parser = createParser()
                val result = parser.parse(text, createContext())
                result shouldBe text
            }
        }

        test("text with only spaces passes through unchanged") {
            checkAll(Arb.int(0..50)) { count ->
                val spaces = " ".repeat(count)
                val parser = createParser()
                val result = parser.parse(spaces, createContext())
                result shouldBe spaces
            }
        }

        test("text with newlines passes through unchanged") {
            checkAll(nonEmptySafeStringArb) { text: String ->
                val withNewlines = "$text\n$text\n$text"
                val parser = createParser()
                val result = parser.parse(withNewlines, createContext())
                result shouldBe withNewlines
            }
        }
    }

    context("Template block structure") {

        test("empty template block returns empty string") {
            val parser = createParser()
            val result = parser.parse("<%  %>", createContext())
            result shouldBe ""
        }

        test("template with only whitespace in block returns empty") {
            checkAll(Arb.int(1..10)) { spaces ->
                val whitespace = " ".repeat(spaces)
                val parser = createParser()
                val result = parser.parse("<%$whitespace%>", createContext())
                result shouldBe ""
            }
        }

        test("text before and after template block is preserved") {
            checkAll(nonEmptySafeStringArb, nonEmptySafeStringArb) { before: String, after: String ->
                val parser = createParser()
                val template = "$before<% \"middle\" %>$after"
                val result = parser.parse(template, createContext())

                result shouldContain before
                result shouldContain after
                result shouldContain "middle"
            }
        }
    }

    context("String literal handling") {

        test("double-quoted string literals are evaluated") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val parser = createParser()
                val result = parser.parse("<% \"$s\" %>", createContext())
                result shouldBe s
            }
        }

        test("single-quoted string literals are evaluated") {
            checkAll(nonEmptySafeStringArb) { s: String ->
                val parser = createParser()
                val result = parser.parse("<% '$s' %>", createContext())
                result shouldBe s
            }
        }
    }

    context("Number literal handling") {

        test("integer literals are evaluated correctly") {
            checkAll(Arb.int(0..10000)) { n ->
                val parser = createParser()
                val result = parser.parse("<% $n %>", createContext())
                result shouldBe n.toString()
            }
        }

        test("negative integer literals are evaluated correctly") {
            checkAll(Arb.int(1..10000)) { n ->
                val parser = createParser()
                val result = parser.parse("<% -$n %>", createContext())
                result shouldBe (-n).toString()
            }
        }
    }

    context("Multiple blocks") {

        test("multiple template blocks are all processed") {
            checkAll(nonEmptySafeStringArb, nonEmptySafeStringArb) { a: String, b: String ->
                val parser = createParser()
                val template = "<% \"$a\" %> and <% \"$b\" %>"
                val result = parser.parse(template, createContext())

                result shouldContain a
                result shouldContain b
                result shouldContain " and "
            }
        }

        test("adjacent template blocks work correctly") {
            checkAll(nonEmptySafeStringArb, nonEmptySafeStringArb) { a: String, b: String ->
                val parser = createParser()
                val template = "<% \"$a\" %><% \"$b\" %>"
                val result = parser.parse(template, createContext())

                result shouldBe "$a$b"
            }
        }
    }

    context("Arithmetic in templates") {

        test("addition in template blocks") {
            checkAll(Arb.int(0..1000), Arb.int(0..1000)) { a, b ->
                val parser = createParser()
                val result = parser.parse("<% $a + $b %>", createContext())
                result shouldBe (a + b).toString()
            }
        }

        test("multiplication in template blocks") {
            checkAll(Arb.int(0..100), Arb.int(0..100)) { a, b ->
                val parser = createParser()
                val result = parser.parse("<% $a * $b %>", createContext())
                result shouldBe (a * b).toString()
            }
        }
    }

    context("Boolean expressions") {

        test("true literal evaluates to true") {
            val parser = createParser()
            val result = parser.parse("<% true %>", createContext())
            result shouldBe "true"
        }

        test("false literal evaluates to false") {
            val parser = createParser()
            val result = parser.parse("<% false %>", createContext())
            result shouldBe "false"
        }

        test("comparison expressions evaluate correctly") {
            checkAll(Arb.int(0..100), Arb.int(0..100)) { a, b ->
                val parser = createParser()
                val result = parser.parse("<% $a < $b %>", createContext())
                result shouldBe (a < b).toString()
            }
        }
    }
})

