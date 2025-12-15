package ronsijm.templater.handlers

import ronsijm.templater.parser.TemplateContext

/**
 * Handler pattern: Request holds the data, Handler holds the logic.
 * Similar to MediatR in C#.
 */
interface CommandHandler<TRequest : CommandRequest, TResponse> {
    fun handle(request: TRequest, context: TemplateContext): TResponse
}



