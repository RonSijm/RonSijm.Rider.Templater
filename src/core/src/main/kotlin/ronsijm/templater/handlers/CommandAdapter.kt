package ronsijm.templater.handlers

import ronsijm.templater.common.CancelledResult
import ronsijm.templater.common.CommandResult
import ronsijm.templater.common.ErrorResult
import ronsijm.templater.common.OkResult
import ronsijm.templater.common.OkValueResult
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.utils.ErrorMessages
import ronsijm.templater.utils.Logging


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
        return try {
            val request = registration.parser.parse(args)
            val response = registration.handler.handle(request, context)


            if (response is CommandResult) {

                if (response is CancelledResult && registration.handler !is CancellableHandler) {
                    LOG?.warn(
                        "Handler ${registration.handler::class.simpleName} returned CancelledResult " +
                            "but does not implement CancellableHandler. This is likely a bug."
                    )
                }
                return response
            }


            if (response != null) {
                OkValueResult(response.toString())
            } else {
                OkResult
            }
        } catch (e: Exception) {
            LOG?.error("Error executing handler ${registration.metadata.name}: ${e.message}", e)
            ErrorResult(ErrorMessages.commandError(e.message ?: "Unknown error"))
        }
    }
}
