package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class MoveRequestParser : RequestParser<MoveRequest> {
    override fun parse(args: List<Any?>): MoveRequest {
        return MoveRequest(
            newPath = ArgumentParser.getString(args, 0)
        )
    }
}
