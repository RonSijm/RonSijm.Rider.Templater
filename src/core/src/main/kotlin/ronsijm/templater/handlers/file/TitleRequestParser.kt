package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser

class TitleRequestParser : RequestParser<TitleRequest> {
    override fun parse(args: List<Any?>): TitleRequest {
        return TitleRequest()
    }
}
