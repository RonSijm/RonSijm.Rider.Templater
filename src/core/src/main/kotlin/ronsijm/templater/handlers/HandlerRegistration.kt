package ronsijm.templater.handlers


data class HandlerRegistration<TRequest : CommandRequest, TResponse>(
    val handler: CommandHandler<TRequest, TResponse>,
    val parser: RequestParser<TRequest>,
    val metadata: HandlerMetadata
)
