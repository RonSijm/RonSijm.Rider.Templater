package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.RequestParser

class DailyQuoteRequestParser : RequestParser<DailyQuoteRequest> {
    override fun parse(args: List<Any?>): DailyQuoteRequest {
        return DailyQuoteRequest()
    }
}
