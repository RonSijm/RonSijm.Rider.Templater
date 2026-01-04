package ronsijm.templater.handlers

import ronsijm.templater.common.CommandResult
import ronsijm.templater.parser.TemplateContext


interface HandlerModule {

    val moduleName: String


    fun executeCommand(
        commandName: String,
        args: List<Any?>,
        context: TemplateContext
    ): CommandResult


    fun getAvailableCommands(): List<HandlerMetadata>


    fun hasCommand(commandName: String): Boolean {
        return getAvailableCommands().any { it.name == commandName }
    }


    fun getCommandMetadata(commandName: String): HandlerMetadata? {
        return getAvailableCommands().find { it.name == commandName }
    }
}


abstract class AbstractHandlerModule : HandlerModule {
    private val commands = mutableMapOf<String, CommandEntry>()


    protected fun registerCommand(
        name: String,
        metadata: HandlerMetadata,
        handler: (List<Any?>, TemplateContext) -> CommandResult
    ) {
        commands[name] = CommandEntry(metadata, handler)
    }

    override fun executeCommand(
        commandName: String,
        args: List<Any?>,
        context: TemplateContext
    ): CommandResult {
        val entry = commands[commandName]
            ?: return ronsijm.templater.common.ErrorResult("Unknown command: $moduleName.$commandName")
        return entry.handler(args, context)
    }

    override fun getAvailableCommands(): List<HandlerMetadata> {
        return commands.values.map { it.metadata }
    }

    override fun hasCommand(commandName: String): Boolean {
        return commands.containsKey(commandName)
    }

    override fun getCommandMetadata(commandName: String): HandlerMetadata? {
        return commands[commandName]?.metadata
    }

    private data class CommandEntry(
        val metadata: HandlerMetadata,
        val handler: (List<Any?>, TemplateContext) -> CommandResult
    )
}

class HandlerModuleBuilder(private val moduleName: String) {
    private val commands = mutableListOf<CommandDefinition>()

    fun command(
        name: String,
        description: String = "",
        example: String = "",
        pure: Boolean = false,
        barrier: Boolean = false,
        handler: (List<Any?>, TemplateContext) -> CommandResult
    ) {
        commands.add(CommandDefinition(
            metadata = HandlerMetadata(
                module = moduleName,
                name = name,
                description = description,
                example = example,
                pure = pure,
                barrier = barrier
            ),
            handler = handler
        ))
    }

    internal fun build(): HandlerModule {
        return object : AbstractHandlerModule() {
            override val moduleName: String = this@HandlerModuleBuilder.moduleName

            init {
                for (cmd in commands) {
                    registerCommand(cmd.metadata.name, cmd.metadata, cmd.handler)
                }
            }
        }
    }

    private data class CommandDefinition(
        val metadata: HandlerMetadata,
        val handler: (List<Any?>, TemplateContext) -> CommandResult
    )
}

fun handlerModule(moduleName: String, block: HandlerModuleBuilder.() -> Unit): HandlerModule {
    return HandlerModuleBuilder(moduleName).apply(block).build()
}

