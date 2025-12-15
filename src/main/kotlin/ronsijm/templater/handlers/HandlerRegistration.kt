package ronsijm.templater.handlers

/** Bundles a handler with its argument parser and metadata */
data class HandlerRegistration<TRequest : CommandRequest, TResponse>(
    val handler: CommandHandler<TRequest, TResponse>,
    val parser: RequestParser<TRequest>,
    val metadata: HandlerMetadata
)



