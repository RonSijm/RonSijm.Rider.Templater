package ronsijm.templater.parser

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.services.mock.MockSystemOperationsService
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.services.mock.NullClipboardService
import ronsijm.templater.services.mock.NullFileOperationService
import ronsijm.templater.services.mock.NullHttpService
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.SimpleTemplaterSettings


class CancelBehaviorTest {



    private fun createServices(mockService: MockSystemOperationsService, settings: SimpleTemplaterSettings): ServiceContainer {
        return ServiceContainer(
            clipboardService = NullClipboardService,
            httpService = NullHttpService,
            fileOperationService = NullFileOperationService,
            systemOperationsService = mockService,
            settings = settings
        )
    }

    @Test
    fun `prompt cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.REMOVE_EXPRESSION)
        val services = createServices(mockService, settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Enter name') %> World"
        val result = parser.parse(template, context, NullAppModuleProvider)

        assertEquals("Hello  World", result)
    }

    @Test
    fun `prompt cancelled with KEEP_EXPRESSION keeps the expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = createServices(mockService, settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Enter name') %> World"
        val result = parser.parse(template, context, NullAppModuleProvider)

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



    @Test
    fun `suggester cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(suggesterResponse = null)
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
        val mockService = MockSystemOperationsService(suggesterResponse = null)
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



    @Test
    fun `multiSuggester cancelled with REMOVE_EXPRESSION removes the expression`() {
        val mockService = MockSystemOperationsService(multiSuggesterResponse = null)
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
        val mockService = MockSystemOperationsService(multiSuggesterResponse = null)
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



    @Test
    fun `default cancel behavior is REMOVE_EXPRESSION`() {
        val settings = SimpleTemplaterSettings()
        assertEquals(CancelBehavior.REMOVE_EXPRESSION, settings.cancelBehavior)
    }

    @Test
    fun `cancelled prompt with default settings removes expression`() {
        val mockService = MockSystemOperationsService(promptResponse = null)

        val services = ServiceContainer(systemOperationsService = mockService)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)

        val template = "Hello <% tp.system.prompt('Name') %>"
        val result = parser.parse(template, context)

        assertEquals("Hello ", result)
    }



    @Test
    fun `cancelled prompt with whitespace trim and KEEP_EXPRESSION preserves expression but trims whitespace`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)



        val template = "Hello\n<%- tp.system.prompt('Name') -%>\nWorld"
        val result = parser.parse(template, context)



        assertEquals("Hello<%- tp.system.prompt('Name') -%>World", result)
    }

    @Test
    fun `cancelled prompt without whitespace trim and KEEP_EXPRESSION preserves everything`() {
        val mockService = MockSystemOperationsService(promptResponse = null)
        val settings = SimpleTemplaterSettings(cancelBehavior = CancelBehavior.KEEP_EXPRESSION)
        val services = ServiceContainer(systemOperationsService = mockService, settings = settings)
        val parser = TemplateParser(validateSyntax = false, services = services)
        val context = TestContextFactory.create(services = services)


        val template = "Hello\n<% tp.system.prompt('Name') %>\nWorld"
        val result = parser.parse(template, context)

        assertEquals("Hello\n<% tp.system.prompt('Name') %>\nWorld", result)
    }
}
