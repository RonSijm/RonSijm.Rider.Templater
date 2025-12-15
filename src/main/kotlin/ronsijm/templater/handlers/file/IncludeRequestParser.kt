package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class IncludeRequestParser : RequestParser<IncludeRequest> {
    override fun parse(args: List<Any?>): IncludeRequest {
        return IncludeRequest(
            includeLink = ArgumentParser.getString(args, 0)
        )
    }
}

