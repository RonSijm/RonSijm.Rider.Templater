package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class RenameRequestParser : RequestParser<RenameRequest> {
    override fun parse(args: List<Any?>): RenameRequest {
        return RenameRequest(
            newName = ArgumentParser.getString(args, 0)
        )
    }
}

