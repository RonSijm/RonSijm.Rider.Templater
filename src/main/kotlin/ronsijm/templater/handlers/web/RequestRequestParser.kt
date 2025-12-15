package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class RequestRequestParser : RequestParser<RequestRequest> {
    override fun parse(args: List<Any?>): RequestRequest {
        return RequestRequest(
            url = ArgumentParser.getString(args, 0),
            jsonPath = ArgumentParser.getString(args, 1)
        )
    }
}

