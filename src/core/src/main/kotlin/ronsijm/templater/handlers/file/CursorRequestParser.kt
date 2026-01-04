package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class CursorRequestParser : RequestParser<CursorRequest> {
    override fun parse(args: List<Any?>): CursorRequest {
        return CursorRequest(
            order = ArgumentParser.getInt(args, 0)
        )
    }
}
