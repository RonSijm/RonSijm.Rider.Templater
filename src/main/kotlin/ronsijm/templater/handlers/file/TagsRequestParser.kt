package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser

class TagsRequestParser : RequestParser<TagsRequest> {
    override fun parse(args: List<Any?>): TagsRequest {
        return TagsRequest()
    }
}

