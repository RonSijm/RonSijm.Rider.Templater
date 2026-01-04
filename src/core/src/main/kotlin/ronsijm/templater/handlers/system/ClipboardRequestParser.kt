package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.RequestParser

class ClipboardRequestParser : RequestParser<ClipboardRequest> {
    override fun parse(args: List<Any?>): ClipboardRequest {
        return ClipboardRequest()
    }
}
