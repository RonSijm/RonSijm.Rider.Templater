package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser

class NameRequestParser : RequestParser<NameRequest> {
    override fun parse(args: List<Any?>): NameRequest {
        return NameRequest()
    }
}
