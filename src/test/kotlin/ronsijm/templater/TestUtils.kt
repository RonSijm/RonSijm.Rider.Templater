package ronsijm.templater

import org.junit.jupiter.api.Assertions
import ronsijm.templater.handlers.CancelledResult
import ronsijm.templater.handlers.CommandResult
import ronsijm.templater.handlers.ErrorResult
import ronsijm.templater.handlers.OkResult
import ronsijm.templater.handlers.OkValueResult
import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.ServiceContainer

/**
 * Test utilities for creating test contexts and common test data
 * Eliminates repeated createTestContext() functions across all test files
 */
object TestContextFactory {

    /**
     * Create a basic test context with sensible defaults
     */
    fun create(
        frontmatter: Map<String, Any> = emptyMap(),
        fileName: String = "test.md",
        filePath: String = "/test.md",
        fileContent: String? = null,
        services: ServiceContainer = ServiceContainer()
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

    /**
     * Create a test context with custom frontmatter
     */
    fun withFrontmatter(frontmatter: Map<String, Any>): TemplateContext {
        return create(frontmatter = frontmatter)
    }

    /**
     * Create a test context with file content
     */
    fun withContent(content: String): TemplateContext {
        return create(fileContent = content)
    }
}

/**
 * Factory for creating TemplateParser instances with test-friendly configurations
 */
object TestParserFactory {

    /**
     * Create a parser with mock services for testing
     */
    fun createWithMockServices(
        validateSyntax: Boolean = true,
        services: ServiceContainer = ServiceContainer.createForTesting()
    ): TemplateParser {
        return TemplateParser(validateSyntax, services)
    }

    /**
     * Create a parser with default services
     */
    fun createDefault(validateSyntax: Boolean = true): TemplateParser {
        return TemplateParser(validateSyntax)
    }
}

/**
 * Test assertion helpers for CommandResult
 */
object ResultAssertions {

    /**
     * Assert that the result is successful (OkResult or OkValueResult) and has the expected string value.
     */
    fun assertResultEquals(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertEquals(expected, actual.toString(), message)
    }

    /**
     * Assert that the result is an OkValueResult with the expected value.
     */
    fun <T> assertOkValue(expected: T, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is OkValueResult<*>, message ?: "Expected OkValueResult but got ${actual::class.simpleName}")
        @Suppress("UNCHECKED_CAST")
        val okResult = actual as OkValueResult<T>
        Assertions.assertEquals(expected, okResult.value, message)
    }

    /**
     * Assert that the result is an OkResult (void success).
     */
    fun assertOkResult(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is OkResult, message ?: "Expected OkResult but got ${actual::class.simpleName}")
    }

    /**
     * Assert that the result is a CancelledResult.
     */
    fun assertCancelled(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is CancelledResult, message ?: "Expected CancelledResult but got ${actual::class.simpleName}")
    }

    /**
     * Assert that the result is an ErrorResult.
     */
    fun assertError(actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is ErrorResult, message ?: "Expected ErrorResult but got ${actual::class.simpleName}")
    }

    /**
     * Assert that the result is an ErrorResult with a message containing the expected substring.
     */
    fun assertErrorContains(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual is ErrorResult, message ?: "Expected ErrorResult but got ${actual::class.simpleName}")
        val errorResult = actual as ErrorResult
        Assertions.assertTrue(errorResult.message.contains(expected), message ?: "Expected error message to contain '$expected' but was '${errorResult.message}'")
    }

    /**
     * Assert that the result is successful and contains a value matching the predicate.
     */
    fun assertResultMatches(actual: CommandResult, predicate: (String) -> Boolean, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(predicate(actual.toString()), message ?: "Result '${actual}' did not match predicate")
    }

    /**
     * Assert that the result string contains the expected substring.
     */
    fun assertResultContains(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(actual.toString().contains(expected), message ?: "Expected result to contain '$expected' but was '${actual}'")
    }

    /**
     * Assert that the result string starts with the expected prefix.
     */
    fun assertResultStartsWith(expected: String, actual: CommandResult, message: String? = null) {
        Assertions.assertTrue(actual.isSuccess, message ?: "Expected successful result but got $actual")
        Assertions.assertTrue(actual.toString().startsWith(expected), message ?: "Expected result to start with '$expected' but was '${actual}'")
    }
}

/**
 * Extension functions for more fluent test assertions on CommandResult
 */

/**
 * Assert this result equals the expected string value.
 */
infix fun CommandResult.shouldEqual(expected: String) {
    ResultAssertions.assertResultEquals(expected, this)
}

/**
 * Assert this result is cancelled.
 */
fun CommandResult.shouldBeCancelled() {
    ResultAssertions.assertCancelled(this)
}

/**
 * Assert this result is successful (not cancelled).
 */
fun CommandResult.shouldBeSuccess() {
    Assertions.assertTrue(this.isSuccess, "Expected successful result but got $this")
}

/**
 * Assert this result contains the expected substring.
 */
infix fun CommandResult.shouldContain(expected: String) {
    ResultAssertions.assertResultContains(expected, this)
}

/**
 * Assert this result is an error.
 */
fun CommandResult.shouldBeError() {
    ResultAssertions.assertError(this)
}

/**
 * Assert this result is an error containing the expected message.
 */
infix fun CommandResult.shouldBeErrorContaining(expected: String) {
    ResultAssertions.assertErrorContains(expected, this)
}
