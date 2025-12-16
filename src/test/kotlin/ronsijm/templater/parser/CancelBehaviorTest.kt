package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.MockSystemOperationsService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.SimpleTemplaterSettings

/**
 * Tests for the CancelBehavior setting.
 * 
 * When a user cancels a dialog (prompt, suggester, multiSuggester), the behavior
 * depends on the cancelBehavior setting:
 * - REMOVE_EXPRESSION: The template expression is replaced with empty string (default)
 * - KEEP_EXPRESSION: The original template expression is preserved
 */
class CancelBehaviorTest {

    // ==================== PROMPT TESTS ====================

    @Test
    fun `prompt cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.REMOVE_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Enter name') %> World"
        val result = parser.parse(template, context)

        assertEquals("Hello  World", result)
    }

    @Test
    fun `prompt cancelled with KEEP_EXPRESSION keeps the expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Enter name') %> World"
        val result = parser.parse(template, context)

        assertEquals("Hello <% tp.system.prompt('Enter name') %> World", result)
    }

    @Test
    fun `prompt with value replaces expression regardless of cancel behavior`() {
        val mockService = MockSystemOperationsService(promptResponse = "John")
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Enter name') %> World"
        val result = parser.parse(template, context)

        assertEquals("Hello John World", result)
    }

    // ==================== SUGGESTER TESTS ====================

    @Test
    fun `suggester cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(suggesterResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.REMOVE_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Choice: <% tp.system.suggester(['A', 'B'], ['a', 'b']) %>"
        val result = parser.parse(template, context)

        assertEquals("Choice: ", result)
    }

    @Test
    fun `suggester cancelled with KEEP_EXPRESSION keeps the expression`() {
        val mockService = MockSystemOperationsService(suggesterResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Choice: <% tp.system.suggester(['A', 'B'], ['a', 'b']) %>"
        val result = parser.parse(template, context)

        assertEquals("Choice: <% tp.system.suggester(['A', 'B'], ['a', 'b']) %>", result)
    }

    @Test
    fun `suggester with selection replaces expression regardless of cancel behavior`() {
        val mockService = MockSystemOperationsService(suggesterResponse = "selected_value")
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Choice: <% tp.system.suggester(['A', 'B'], ['a', 'b']) %>"
        val result = parser.parse(template, context)

        assertEquals("Choice: selected_value", result)
    }

    // ==================== MULTI_SUGGESTER TESTS ====================

    @Test
    fun `multiSuggester cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(multiSuggesterResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.REMOVE_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Selected: <% tp.system.multi_suggester(['X', 'Y', 'Z'], ['x', 'y', 'z']) %>"
        val result = parser.parse(template, context)

        assertEquals("Selected: ", result)
    }

    @Test
    fun `multiSuggester cancelled with KEEP_EXPRESSION keeps the expression`() {
        val mockService = MockSystemOperationsService(multiSuggesterResponse = null) // null = cancelled
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Selected: <% tp.system.multi_suggester(['X', 'Y', 'Z'], ['x', 'y', 'z']) %>"
        val result = parser.parse(template, context)

        assertEquals("Selected: <% tp.system.multi_suggester(['X', 'Y', 'Z'], ['x', 'y', 'z']) %>", result)
    }

    @Test
    fun `multiSuggester with selection replaces expression regardless of cancel behavior`() {
        val mockService = MockSystemOperationsService(multiSuggesterResponse = listOf("x", "z"))
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Selected: <% tp.system.multi_suggester(['X', 'Y', 'Z'], ['x', 'y', 'z']) %>"
        val result = parser.parse(template, context)

        assertEquals("Selected: [x, z]", result)
    }

    // ==================== MULTIPLE EXPRESSIONS TESTS ====================

    @Test
    fun `multiple cancelled prompts with KEEP_EXPRESSION keeps all expressions`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Name: <% tp.system.prompt('Name') %>, Age: <% tp.system.prompt('Age') %>"
        val result = parser.parse(template, context)

        assertEquals("Name: <% tp.system.prompt('Name') %>, Age: <% tp.system.prompt('Age') %>", result)
    }

    @Test
    fun `multiple cancelled prompts with REMOVE_EXPRESSION removes all expressions`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.REMOVE_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Name: <% tp.system.prompt('Name') %>, Age: <% tp.system.prompt('Age') %>"
        val result = parser.parse(template, context)

        assertEquals("Name: , Age: ", result)
    }

    // ==================== DEFAULT BEHAVIOR TESTS ====================

    @Test
    fun `default cancel behavior is REMOVE_EXPRESSION`() {
        val settings = SimpleTemplaterSettings()
        assertEquals(CancelBehavior.REMOVE_EXPRESSION, settings.cancelBehavior)
    }

    @Test
    fun `cancelled prompt with default settings removes expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        // Use default settings (no explicit cancelBehavior)
        val services = ServiceContainer(systemOperationsService = mockService)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Name') %>"
        val result = parser.parse(template, context)

        assertEquals("Hello ", result)
    }

    // ==================== WHITESPACE CONTROL TESTS ====================

    @Test
    fun `cancelled prompt with whitespace trim and KEEP_EXPRESSION preserves expression but trims whitespace`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        // Note: whitespace control markers (<%- and -%>) still trim surrounding whitespace
        // but the expression itself is preserved
        val template = "Hello\n<%- tp.system.prompt('Name') -%>\nWorld"
        val result = parser.parse(template, context)

        // The expression is preserved, but whitespace trimming still applies
        // This is the expected behavior - whitespace control is separate from cancel behavior
        assertEquals("Hello<%- tp.system.prompt('Name') -%>World", result)
    }

    @Test
    fun `cancelled prompt without whitespace trim and KEEP_EXPRESSION preserves everything`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        // Without whitespace control markers, everything is preserved
        val template = "Hello\n<% tp.system.prompt('Name') %>\nWorld"
        val result = parser.parse(template, context)

        assertEquals("Hello\n<% tp.system.prompt('Name') %>\nWorld", result)
    }
}

