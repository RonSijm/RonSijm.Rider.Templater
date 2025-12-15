package ronsijm.templater

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

