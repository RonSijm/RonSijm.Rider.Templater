package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser

class ContentRequestParser : RequestParser<ContentRequest> {
    override fun parse(args: List<Any?>): ContentRequest {
        return ContentRequest()
    }
}

