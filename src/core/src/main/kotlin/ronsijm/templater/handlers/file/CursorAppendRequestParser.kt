package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class CursorAppendRequestParser : RequestParser<CursorAppendRequest> {
    override fun parse(args: List<Any?>): CursorAppendRequest {
        return CursorAppendRequest(
            content = ArgumentParser.getString(args, 0)
        )
    }
}
