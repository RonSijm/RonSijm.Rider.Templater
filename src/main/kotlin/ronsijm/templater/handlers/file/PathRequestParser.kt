package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class PathRequestParser : RequestParser<PathRequest> {
    override fun parse(args: List<Any?>): PathRequest {
        return PathRequest(
            relative = ArgumentParser.getBoolean(args, 0, false)
        )
    }
}

