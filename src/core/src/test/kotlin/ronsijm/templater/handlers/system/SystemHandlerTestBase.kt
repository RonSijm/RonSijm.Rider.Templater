package ronsijm.templater.handlers.system

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.services.ServiceContainer


abstract class SystemHandlerTestBase {

    protected fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["system"]?.get(name)
            ?: throw IllegalArgumentException("Command system.$name not found")
    }

    protected fun createContext(services: ServiceContainer = ServiceContainer()): TemplateContext {
        return TemplateContext(
            frontmatter = emptyMap(),
            frontmatterParser = FrontmatterParser(),
            fileName = "test.md",
            filePath = "/test.md",
            services = services
        )
    }
}
