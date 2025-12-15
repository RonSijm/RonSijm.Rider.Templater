package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser

class SelectionRequestParser : RequestParser<SelectionRequest> {
    override fun parse(args: List<Any?>): SelectionRequest {
        return SelectionRequest()
    }
}

