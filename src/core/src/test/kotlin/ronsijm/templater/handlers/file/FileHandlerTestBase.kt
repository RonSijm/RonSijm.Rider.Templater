package ronsijm.templater.handlers.file

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.ServiceContainer


abstract class FileHandlerTestBase {

    protected fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["file"]?.get(name)
            ?: throw IllegalArgumentException("Command file.$name not found")
    }

    protected fun createContext(
        fileName: String = "test.md",
        filePath: String = "/path/to/test.md",
        fileContent: String? = null,
        services: ServiceContainer = ServiceContainer()
    ): TemplateContext {
        return TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = fileName,
            filePath = filePath,
            fileContent = fileContent,
            services = services
        )
    }
}
