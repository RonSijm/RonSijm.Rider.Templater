package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class ExistsRequestParser : RequestParser<ExistsRequest> {
    override fun parse(args: List<Any?>): ExistsRequest {
        return ExistsRequest(
            filepath = ArgumentParser.getString(args, 0)
        )
    }
}

