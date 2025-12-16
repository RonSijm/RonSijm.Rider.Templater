package ronsijm.templater.handlers.date

import ronsijm.templater.TestContextFactory
import ronsijm.templater.handlers.Command
import ronsijm.templater.handlers.generated.HandlerRegistry
import ronsijm.templater.parser.TemplateContext

/**
 * Base class with shared utilities for date handler tests
 */
abstract class DateHandlerTestBase {

    protected fun getCommand(name: String): Command {
        return HandlerRegistry.commandsByModule["date"]?.get(name)
            ?: throw IllegalArgumentException("Command date.$name not found")
    }

    protected fun createContext(): TemplateContext = TestContextFactory.create()
}

