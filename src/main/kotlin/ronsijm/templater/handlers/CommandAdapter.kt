package ronsijm.templater.handlers

import ronsijm.templater.parser.TemplateContext

/** Wraps a Handler+Parser pair so it can be used as a Command */
class CommandAdapter<TRequest : CommandRequest, TResponse>(
    private val registration: HandlerRegistration<TRequest, TResponse>
) : Command, CommandMetadataProvider {

    override val metadata: CommandMetadata
        get() = CommandMetadata(
            name = registration.metadata.name,
            description = registration.metadata.description,
            example = registration.metadata.example,
            parameters = registration.metadata.parameters
        )

    override fun execute(args: List<Any?>, context: TemplateContext): String? {
        val request = registration.parser.parse(args)
        val response = registration.handler.handle(request, context)
        return response?.toString()
    }
}



