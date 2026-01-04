package ronsijm.templater.handlers

import ronsijm.templater.parser.TemplateContext


interface CommandHandler<TRequest : CommandRequest, TResponse> {
    fun handle(request: TRequest, context: TemplateContext): TResponse
}
