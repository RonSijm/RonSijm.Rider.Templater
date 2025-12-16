package ronsijm.templater.handlers

import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.Logging

/** Wraps a Handler+Parser pair so it can be used as a Command */
class CommandAdapter<TRequest : CommandRequest, TResponse>(
    private val registration: HandlerRegistration<TRequest, TResponse>
) : Command, CommandMetadataProvider {

    companion object {
        private val LOG = Logging.getLogger<CommandAdapter<*, *>>()
    }

    override val metadata: CommandMetadata
        get() = CommandMetadata(
            name = registration.metadata.name,
            description = registration.metadata.description,
            example = registration.metadata.example,
            parameters = registration.metadata.parameters,
            cancellable = registration.metadata.cancellable,
            pure = registration.metadata.pure,
            barrier = registration.metadata.barrier
        )

    override fun execute(args: List<Any?>, context: TemplateContext): CommandResult {
        val request = registration.parser.parse(args)
        val response = registration.handler.handle(request, context)

        // If the handler already returns a CommandResult, pass it through
        if (response is CommandResult) {
            // Validate: only CancellableHandler should return CancelledResult
            if (response is CancelledResult && registration.handler !is CancellableHandler) {
                LOG?.warn(
                    "Handler ${registration.handler::class.simpleName} returned CancelledResult " +
                    "but does not implement CancellableHandler. This is likely a bug."
                )
            }
            return response
        }

        // Otherwise wrap the response in an appropriate result type
        return if (response != null) {
            OkValueResult(response.toString())
        } else {
            OkResult
        }
    }
}



