package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class FolderRequestParser : RequestParser<FolderRequest> {
    override fun parse(args: List<Any?>): FolderRequest {
        return FolderRequest(
            relative = ArgumentParser.getBoolean(args, 0, false)
        )
    }
}

