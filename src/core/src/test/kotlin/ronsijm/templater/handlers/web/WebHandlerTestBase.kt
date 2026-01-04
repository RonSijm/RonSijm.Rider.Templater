package ronsijm.templater.handlers.web

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer


abstract class WebHandlerTestBase {

    protected fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["web"]?.get(name)
            ?: throw IllegalArgumentException("Command web.$name not found")
    }

    protected fun createContext(services: ServiceContainer = ServiceContainer()): TemplateContext {
        return TestContextFactory.create(services = services)
    }
}
