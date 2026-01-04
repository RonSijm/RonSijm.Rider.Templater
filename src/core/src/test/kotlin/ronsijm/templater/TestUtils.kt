package ronsijm.templater

import org.junit.jupiter.api.Assertions
import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ErrorResult
import ronsijm.templater.common.FrontmatterAccess
import ronsijm.templater.common.ModuleExecutor
import ronsijm.templater.common.OkResult
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.modules.FrontmatterModule
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.HandlerModuleExecutor
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.script.ModuleRegistry
import ronsijm.templater.script.ScriptContext
import ronsijm.templater.script.ScriptEngine
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.utils.CancellationChecker


object TestContextFactory {


    fun create(
        frontmatter: Map<String, Any> = emptyMap(),
        fileName: String = "test.md",
        filePath: String = "/test/path/test.md",
        fileContent: String? = null,
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): TemplateContext {
        return TemplateContext(
            frontmatter = frontmatter,
            frontmatterParser = FrontmatterParser(),
            fileName = fileName,
            filePath = filePath,
            fileContent = fileContent,
            services = services
        )
    }


    fun withFrontmatter(frontmatter: Map<String, Any>): TemplateContext {
        return create(frontmatter = frontmatter)
    }


    fun withContent(content: String): TemplateContext {
        return create(fileContent = content)
    }


    fun createMockFrontmatterAccess(frontmatter: Map<String, Any> = emptyMap()): FrontmatterAccess {
        return object : FrontmatterAccess {
            override fun getValue(parts: List<String>): Any? {

                if (parts.isEmpty() || parts[0] != "frontmatter") {
                    return null
                }


                var current: Any? = frontmatter
                for (i in 1 until parts.size) {
                    val part = parts[i]
                    current = when (current) {
                        is Map<*, *> -> current[part]
                        else -> null
                    }
                    if (current == null) break
                }
                return current
            }
            override fun getAll(): Map<String, Any> = frontmatter
        }
    }


    fun createMockModuleExecutor(): ModuleExecutor {
        return object : ModuleExecutor {
            override fun executeModuleFunction(module: String, function: String, args: List<Any?>): CommandResult {
                return OkValueResult("mock result for $module.$function")
            }
        }
    }


    fun createRealModuleExecutor(
        frontmatter: Map<String, Any> = emptyMap(),
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): ModuleExecutor {
        val context = create(frontmatter = frontmatter, services = services)
        return HandlerModuleExecutor(context)
    }


    fun createScriptContext(): ScriptContext {
        return ScriptContext()
    }


    fun createModuleRegistry(frontmatter: Map<String, Any> = emptyMap()): ModuleRegistry {
        return ModuleRegistry(createMockFrontmatterAccess(frontmatter), createMockModuleExecutor())
    }


    fun createRealModuleRegistry(
        frontmatter: Map<String, Any> = emptyMap(),
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): ModuleRegistry {
        val context = create(frontmatter = frontmatter, services = services)
        return ModuleRegistry(FrontmatterModule(context), HandlerModuleExecutor(context))
    }


    fun createScriptEngine(frontmatter: Map<String, Any> = emptyMap()): ScriptEngine {
        return ScriptEngine(
            createMockFrontmatterAccess(frontmatter),
            createMockModuleExecutor(),
            object : CancellationChecker {
                override fun checkCancelled() {}
                override fun isCancelled(): Boolean = false
            }
        )
    }


    fun createRealScriptEngine(
        frontmatter: Map<String, Any> = emptyMap(),
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): ScriptEngine {
        val context = create(frontmatter = frontmatter, services = services)
        return ScriptEngine(
            FrontmatterModule(context),
            HandlerModuleExecutor(context),
            object : CancellationChecker {
                override fun checkCancelled() {}
                override fun isCancelled(): Boolean = false
            }
        )
    }
}


object TestParserFactory {


    fun createWithMockServices(
        validateSyntax: Boolean = true,
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): TemplateParser {
        return TemplateParser(validateSyntax, services)
    }


    fun createDefault(validateSyntax: Boolean = true): TemplateParser {
        return TemplateParser(validateSyntax, ServiceContainer.createForTesting())
    }


    fun parse(
        content: String,
        context: TemplateContext,
        validateSyntax: Boolean = true
    ): String {
        val parser = createDefault(validateSyntax)
        return parser.parse(content, context, NullAppModuleProvider)
    }
}


object ResultAssertions {


    fun assertResultEquals(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertEquals(expected, actual.toString(), message)
    }


    fun <T> assertOkValue(expected: T, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is OkValueResult<*>, message ?: "Expected OkValueResult but got ${actual::class.simpleName}")
        @Suppress("UNCHECKED_CAST")
        val okResult = actual as OkValueResult<T>
        Assertions.assertEquals(expected, okResult.value, message)
    }


    fun assertOkResult(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is OkResult, message ?: "Expected OkResult but got ${actual::class.simpleName}")
    }


    fun assertCancelled(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is CancelledResult, message ?: "Expected CancelledResult but got ${actual::class.simpleName}")
    }


    fun assertError(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is ErrorResult, message ?: "Expected ErrorResult but got ${actual::class.simpleName}")
    }


    fun assertErrorContains(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is ErrorResult, message ?: "Expected ErrorResult but got ${actual::class.simpleName}")
        val errorResult = actual as ErrorResult
        Assertions.assertTrue(errorResult.message.contains(expected), message ?: "Expected error message to contain '$expected' but was '${errorResult.message}'")
    }


    fun assertResultMatches(actual: CommandResult, predicate: (String) -> Boolean, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(predicate(actual.toString()), message ?: "Result '${actual}' did not match predicate")
    }


    fun assertResultContains(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(actual.toString().contains(expected), message ?: "Expected result to contain '$expected' but was '${actual}'")
    }


    fun assertResultStartsWith(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(actual.toString().startsWith(expected), message ?: "Expected result to start with '$expected' but was '${actual}'")
    }
}




infix fun CommandResult.shouldEqual(expected: String) {
    ResultAssertions.assertResultEquals(expected, this)
}


fun CommandResult.shouldBeCancelled() {
    ResultAssertions.assertCancelled(this)
}


fun CommandResult.shouldBeSuccess() {
    Assertions.assertTrue(this.isSuccess, "Expected successful result but got $this")
}


infix fun CommandResult.shouldContain(expected: String) {
    ResultAssertions.assertResultContains(expected, this)
}


fun CommandResult.shouldBeError() {
    ResultAssertions.assertError(this)
}


infix fun CommandResult.shouldBeErrorContaining(expected: String) {
    ResultAssertions.assertErrorContains(expected, this)
}
