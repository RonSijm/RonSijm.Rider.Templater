package ronsijm.templater.handlers


interface RequestParser<TRequest : CommandRequest> {
    fun parse(args: List<Any?>): TRequest
}
