package ronsijm.templater.handlers

/** Converts raw List<Any?> arguments into a typed request object */
interface RequestParser<TRequest : CommandRequest> {
    fun parse(args: List<Any?>): TRequest
}



